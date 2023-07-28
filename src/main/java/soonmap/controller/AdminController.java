package soonmap.controller;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import soonmap.dto.ArticleDto.ArticleResponse;
import soonmap.dto.ArticleDto.CreateArticleRequest;
import soonmap.dto.MemberDto.*;
import soonmap.dto.NoticeDto.CreateNoticeRequest;
import soonmap.dto.NoticeDto.CreateNoticeResponse;
import soonmap.dto.NoticeDto.ModifyNoticeRequest;
import soonmap.dto.NoticeDto.ModifyNoticeResponse;
import soonmap.entity.Article;
import soonmap.entity.ArticleType;
import soonmap.entity.Member;
import soonmap.entity.Notice;
import soonmap.exception.CustomException;
import soonmap.security.jwt.JwtProvider;
import soonmap.security.jwt.MemberPrincipal;
import soonmap.service.*;

import javax.validation.Valid;
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
    private final NoticeService noticeService;
    private final ArticleService articleService;
    private final ArticleTypeService articleTypeService;

    @PostMapping("/login")
    public ResponseEntity<AdminLoginResponse> adminLogin(@RequestBody @Valid AdminLoginRequest adminLoginRequest) {
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
    public ResponseEntity<AdminResisterResponse> resisterAdmin(@RequestBody @Valid AdminResisterRequest adminResisterRequest) {

        memberService.validateDuplicatedId(adminResisterRequest.getUserId());

        Member member = memberService.addAdmin(adminResisterRequest);
        return ResponseEntity.ok()
                .body(new AdminResisterResponse(true, member.isAdmin(), member.isManager(), member.isStaff()));
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
    @PatchMapping("/manage/ban")
    public ResponseEntity<Account> modifyIsBanAccount(@RequestParam Long id) {
        Member member = memberService.findUserById(id);
        member.setBan(!member.isBan());
        Member savedUser = memberService.editUser(member);
        return ResponseEntity.ok()
                .body(Account.of(savedUser));
    }

    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    @PostMapping("/notice")
    public ResponseEntity<CreateNoticeResponse> uploadNotice(
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

    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    @PatchMapping("/notice/{id}")
    public ResponseEntity<?> modifyNotice(
            @RequestBody ModifyNoticeRequest modifyNoticeRequest,
            @PathVariable Long id) {
        Notice notice = noticeService.findById(id);

        Notice savedNotice = noticeService.save(Notice.builder()
                .id(notice.getId())
                .title(modifyNoticeRequest.getTitle())
                .content(modifyNoticeRequest.getContent())
                .member(notice.getMember())
                .createAt(notice.getCreateAt())
                .isTop(modifyNoticeRequest.isTop())
                .view(notice.getView())
                .isExistImage(modifyNoticeRequest.isExistImage())
                .build());

        return ResponseEntity.ok()
                .body(new ModifyNoticeResponse(true, savedNotice.getId(), savedNotice.getTitle()));
    }

    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    @DeleteMapping("/notice/{id}")
    public ResponseEntity<?> deleteNotice(@PathVariable Long id) {

        Long deleteById = noticeService.deleteById(id);

        return ResponseEntity.ok()
                .body(deleteById);
    }

    @Secured({"ROLE_ADMIN", "ROLE_MANAGER", "ROLE_STAFF"})
    @GetMapping("/article/category")
    public ResponseEntity<?> getArticleCategory() {

        List<ArticleType> all = articleTypeService.findAll();

        return ResponseEntity.ok()
                .body(all);
    }

    @Secured({"ROLE_ADMIN", "ROLE_MANAGER", "ROLE_STAFF"})
    @PostMapping("/article/category")
    public ResponseEntity<?> uploadArticleCategory(@RequestParam("name") String name) {
        ArticleType save = articleTypeService.save(ArticleType.builder()
                .typeName(name)
                .build());

        return ResponseEntity.ok()
                .body(save);
    }

    @Secured({"ROLE_ADMIN", "ROLE_MANAGER", "ROLE_STAFF"})
    @PatchMapping("/article/category/{target}")
    public ResponseEntity<?> modifyArticleCategory(
            @PathVariable String target,
            @RequestParam("name") String name) {

        ArticleType articleType = articleTypeService.findArticleType(target);

        ArticleType save = articleTypeService.save(ArticleType.builder()
                .id(articleType.getId())
                .typeName(name)
                .build());

        return ResponseEntity.ok()
                .body(save);
    }

    @Secured({"ROLE_ADMIN", "ROLE_MANAGER", "ROLE_STAFF"})
    @DeleteMapping("/article/category/{target}")
    public ResponseEntity<?> deleteArticleCategory(@PathVariable String target) {

        ArticleType articleType = articleTypeService.findArticleType(target);
        Long deleteById = articleTypeService.deleteById(articleType.getId());

        return ResponseEntity.ok()
                .body(deleteById);
    }

    @Secured({"ROLE_ADMIN", "ROLE_MANAGER", "ROLE_STAFF"})
    @PostMapping("/article")
    public ResponseEntity<?> uploadArticle(
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

    @Secured({"ROLE_ADMIN", "ROLE_MANAGER", "ROLE_STAFF"})
    @GetMapping("/article/all/{page}")
    public ResponseEntity<?> getArticle(@PathVariable int page, @RequestParam int length) {

        List<Article> articles = articleService.getArticles(page, length);
        List<ArticleResponse> articleResponseList = articles.stream()
                .map(ArticleResponse::of)
                .collect(Collectors.toList());

        return ResponseEntity.ok()
                .body(articleResponseList);
    }

    @Secured({"ROLE_ADMIN", "ROLE_MANAGER", "ROLE_STAFF"})
    @GetMapping("/article/my/{page}")
    public ResponseEntity<?> getMemberArticle(
            @AuthenticationPrincipal MemberPrincipal memberPrincipal,
            @PathVariable int page,
            @RequestParam int length) {

        Member member = memberService.findUserById(memberPrincipal.getMember().getId());

        List<Article> articles = articleService.getMemberArticles(member, page, length);
        List<ArticleResponse> articleResponseList = articles.stream()
                .map(ArticleResponse::of)
                .collect(Collectors.toList());

        return ResponseEntity.ok()
                .body(articleResponseList);
    }
}
