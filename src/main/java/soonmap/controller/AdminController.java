package soonmap.controller;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import soonmap.dto.ArticleDto.CreateArticleRequest;
import soonmap.dto.MemberDto.*;
import soonmap.dto.NoticeDto.CreateNoticeRequest;
import soonmap.dto.NoticeDto.CreateNoticeResponse;
import soonmap.entity.Article;
import soonmap.entity.ArticleType;
import soonmap.entity.Member;
import soonmap.entity.Notice;
import soonmap.exception.CustomException;
import soonmap.security.jwt.JwtProvider;
import soonmap.security.jwt.MemberPrincipal;
import soonmap.service.*;

import javax.validation.Valid;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final MemberService memberService;
    private final JwtProvider jwtProvider;
    private final S3Service s3Service;
    private final NoticeService noticeService;
    private final ArticleService articleService;
    private final ArticleTypeService articleTypeService;

    @Value("${CLOUD_FRONT_URL}")
    private String CLOUD_FRONT_URL;

    @PostMapping("/login")
    public ResponseEntity<AdminLoginResponse> adminLogin(@RequestBody AdminLoginRequest adminLoginRequest) {
        Member member = memberService.loginAdmin(adminLoginRequest);
        String accessToken = jwtProvider.createAccessToken(member.getId());
        String refreshToken = jwtProvider.createRefreshToken(member.getId());

        ResponseCookie responseCookie = memberService.createHttpOnlyCookie(refreshToken);

        memberService.saveAdminRefreshToken(member.getId(), refreshToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .header("accessToken", accessToken)
                .body(new AdminLoginResponse(true, member.isAdmin(), member.isManager(), member.isStaff()));
    }

    @GetMapping("/refresh")
    public ResponseEntity<Boolean> refreshAdminToken(@CookieValue("refreshToken") String token) {
        Claims claims = jwtProvider.decodeJwtToken(token);

        if (!claims.getSubject().equals("refresh_token")) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "잘못된 요청입니다.");
        }

        Long uid = claims.get("uid", Long.class);
        Member member = memberService.findUserById(uid);

        if (memberService.getAdminRefreshToken(member.getId()) == null) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "잘못된 요청입니다.");
        }

        String accessToken = jwtProvider.createAccessToken(uid);
        return ResponseEntity.ok()
                .header("accessToken", accessToken)
                .body(true);
    }

    @PostMapping("/register")
    public ResponseEntity<AdminResisterResponse> resisterAdmin(@RequestBody AdminResisterRequest adminResisterRequest) {
        Member member = memberService.addAdmin(adminResisterRequest);
        return ResponseEntity.ok()
                .body(new AdminResisterResponse(member.isAdmin(), member.isManager(), member.isStaff()));
    }

    @Secured("ROLE_ADMIN")
    @GetMapping("/account/admin")
    public ResponseEntity<AccountListResponse> getAdminAccount() {
        List<Member> adminAccount = memberService.findAdminAccount();
        List<Account> collect = adminAccount.stream().map(Account::of).collect(Collectors.toList());
        return ResponseEntity.ok()
                .body(new AccountListResponse(adminAccount.size(), collect));
    }

    @Secured("ROLE_ADMIN")
    @GetMapping("/account/all")
    public ResponseEntity<AccountListResponse> getAllAccount() {
        List<Member> adminAccount = memberService.findAll();
        List<Account> collect = adminAccount.stream().map(Account::of).collect(Collectors.toList());
        return ResponseEntity.ok()
                .body(new AccountListResponse(adminAccount.size(), collect));
    }

    @Secured("ROLE_ADMIN")
    @PutMapping("/manage/ban")
    public ResponseEntity<Account> manageIsBanAccount(@RequestParam Long id) {
        Member member = memberService.findUserById(id);
        member.setBan(!member.isBan());
        Member savedUser = memberService.editUser(member);
        return ResponseEntity.ok()
                .body(Account.of(savedUser));
    }

    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    @PostMapping("/notice")
    public ResponseEntity<CreateNoticeResponse> writeNotice(
            @AuthenticationPrincipal MemberPrincipal memberPrincipal,
            @RequestBody @Valid CreateNoticeRequest createNoticeRequest) {

        Notice savedNotice = noticeService.save(Notice.builder()
                .title(createNoticeRequest.getTitle())
                .content(createNoticeRequest.getContent())
                .member(memberPrincipal.getMember())
                .createAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .isTop(createNoticeRequest.isTop())
                .view(0)
                .isExistImage(createNoticeRequest.isExistImage())
                .build());

        return ResponseEntity.ok()
                .body(new CreateNoticeResponse(true, savedNotice.getId(), savedNotice.getTitle()));
    }

    @Secured({"ROLE_ADMIN", "ROLE_MANAGER", "ROLE_STAFF"})
    @PostMapping("/image")
    public String uploadImage(@RequestParam("image") MultipartFile image) throws IOException {
        String upload = s3Service.upload(image, "/images");
        return CLOUD_FRONT_URL + upload;
    }

    @Secured({"ROLE_ADMIN", "ROLE_MANAGER", "ROLE_STAFF"})
    @PostMapping("/article/category")
    public ResponseEntity<?> createArticleCategory(@RequestParam("name") String name) {
        ArticleType save = articleTypeService.save(ArticleType.builder()
                .typeName(name)
                .build());

        return ResponseEntity.ok()
                .body(save);
    }

    @Secured({"ROLE_ADMIN", "ROLE_MANAGER", "ROLE_STAFF"})
    @PostMapping("/article")
    public ResponseEntity<?> writeArticle(
            @AuthenticationPrincipal MemberPrincipal memberPrincipal,
            @RequestBody @Valid CreateArticleRequest createArticleRequest) {

        ArticleType articleType = articleTypeService.findArticleType(createArticleRequest.getArticleType());

        Article save = articleService.save(Article.builder()
                .title(createArticleRequest.getTitle())
                .content(createArticleRequest.getContent())
                .articleType(articleType)
                .createAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .member(memberPrincipal.getMember())
                .view(0)
                .isExistImage(createArticleRequest.isExistImage())
                .build());


        return ResponseEntity.ok()
                .body(save);
    }
}
