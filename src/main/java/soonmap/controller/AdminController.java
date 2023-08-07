package soonmap.controller;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import soonmap.dto.ArticleDto.*;
import soonmap.dto.ArticleTypeDto;
import soonmap.dto.ArticleTypeDto.ArticleTypePageResponse;
import soonmap.dto.ArticleTypeDto.ArticleTypeRequest;
import soonmap.dto.ArticleTypeDto.ArticleTypeResponse;
import soonmap.dto.BuildingInfoDto.*;
import soonmap.dto.MemberDto.*;
import soonmap.dto.NoticeDto.*;
import soonmap.dto.TokenDto.RefreshTokenRequest;
import soonmap.entity.*;
import soonmap.exception.CustomException;
import soonmap.security.jwt.JwtProvider;
import soonmap.security.jwt.MemberPrincipal;
import soonmap.service.*;

import javax.validation.Valid;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final S3Service s3Service;
    private final MemberService memberService;
    private final JwtProvider jwtProvider;
    private final NoticeService noticeService;
    private final ArticleService articleService;
    private final ArticleTypeService articleTypeService;
    private final BuildingInfoService buildingInfoService;
    private final FloorService floorService;

    @PostMapping("/login")
    public ResponseEntity<AdminLoginResponse> adminLogin(
            @RequestBody @Valid AdminLoginRequest adminLoginRequest) {
        Member member = memberService.loginAdmin(adminLoginRequest);
        String accessToken = jwtProvider.createAccessToken(member.getId());
        String refreshToken = jwtProvider.createRefreshToken(member.getId());

        /*
        SSL 적용 전까지 refresh 쿠키 전송방식 사용 불가
        ResponseCookie responseCookie = memberService.createHttpOnlyCookie(refreshToken);
        */
        memberService.saveAdminRefreshToken(member.getId(), refreshToken);
        return ResponseEntity.ok()
//                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body(new AdminLoginResponse(true, member.getUsername(), member.isAdmin(), member.isManager(), member.isStaff(), accessToken, refreshToken));
    }

    @PostMapping("/refresh")
//    public ResponseEntity<?> refreshAdminToken(@CookieValue("refreshToken") String token) {
    public ResponseEntity<?> refreshAdminToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        Claims claims = jwtProvider.decodeJwtToken(refreshTokenRequest.getRefreshToken());

        if (!claims.getSubject().equals("refresh_token")) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "잘못된 요청입니다.");
        }

        Long uid = claims.get("uid", Long.class);
        Member member = memberService.findUserById(uid)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "존재하지 않는 유저입니다."));
        ;

        if (memberService.getAdminRefreshToken(member.getId()) == null) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "잘못된 요청입니다.");
        }

        String accessToken = jwtProvider.createAccessToken(uid);
        return ResponseEntity.ok()
//                .header("accessToken", accessToken)
                .body(accessToken);
    }

    @PostMapping("/register")
    public ResponseEntity<AdminResisterResponse> registerAdmin(@RequestBody @Valid AdminResisterRequest adminResisterRequest) {

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
        Member member = memberService.findUserById(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "존재하지 않는 유저입니다."));
        ;
        member.setBan(!member.isBan());
        Member savedUser = memberService.editUser(member);
        return ResponseEntity.ok()
                .body(Account.of(savedUser));
    }

    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    @GetMapping("/notice")
    public ResponseEntity<?> getPageNotice(@RequestParam("page") int page) {
        Page<Notice> all = noticeService.findAll(page, 9);
        List<NoticeResponse> result = all.getContent()
                .stream()
                .map(NoticeResponse::of)
                .collect(Collectors.toList());
        return ResponseEntity.ok()
                .body(new NoticePageResponse(all.getTotalPages(), result));
    }

    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    @GetMapping("/notice/{id}")
    public ResponseEntity<?> getPageNotice(@PathVariable Long id) {

        Notice notice = noticeService.findById(id);

        return ResponseEntity.ok()
                .body(NoticeResponse.of(notice));
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
                .build());

        return ResponseEntity.ok()
                .body(new CreateNoticeResponse(true, savedNotice.getId(), savedNotice.getTitle()));
    }

    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    @PatchMapping("/notice/{id}")
    public ResponseEntity<?> modifyNotice(
            @RequestBody @Valid ModifyNoticeRequest modifyNoticeRequest,
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
        List<ArticleTypeResponse> result = all.stream()
                .map(ArticleTypeResponse::of)
                .collect(Collectors.toList());

        return ResponseEntity.ok()
                .body(result);
    }

    @Secured({"ROLE_ADMIN", "ROLE_MANAGER", "ROLE_STAFF"})
    @GetMapping("/article/category/{name}")
    public ResponseEntity<?> getArticleCategory(@PathVariable String name) {

        ArticleType articleType = articleTypeService.findByTypeName(name)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "존재하지 않는 카테고리입니다."));

        return ResponseEntity.ok()
                .body(ArticleTypeResponse.of(articleType));
    }

    @Secured({"ROLE_ADMIN", "ROLE_MANAGER", "ROLE_STAFF"})
    @GetMapping("/article/category/page")
    public ResponseEntity<?> getPageArticleCategory(@RequestParam("page") int page) {

        Page<ArticleType> articleTypePage = articleTypeService.findAll(page, 10);
        int totalPages = articleTypePage.getTotalPages();
        List<ArticleType> list = articleTypePage.getContent();

        return ResponseEntity.ok()
                .body(new ArticleTypePageResponse(totalPages, list));
    }

    @Secured({"ROLE_ADMIN", "ROLE_MANAGER", "ROLE_STAFF"})
    @PostMapping("/article/category")
    public ResponseEntity<?> uploadArticleCategory(@RequestBody @Valid ArticleTypeRequest ArticleTypeRequest) {

        if (articleTypeService.findByTypeName(ArticleTypeRequest.getTypeName()).isPresent()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "중복된 이름의 카테고리입니다.");
        }

        ArticleType save = articleTypeService.save(ArticleType.builder()
                .typeName(ArticleTypeRequest.getTypeName())
                .description(ArticleTypeRequest.getDescription())
                .build());

        return ResponseEntity.ok()
                .body(save);
    }

    @Secured({"ROLE_ADMIN", "ROLE_MANAGER", "ROLE_STAFF"})
    @PatchMapping("/article/category/{id}")
    public ResponseEntity<?> modifyArticleCategory(
            @PathVariable Long id,
            @RequestBody @Valid ArticleTypeRequest articleTypeRequest) {

        ArticleType articleType = articleTypeService.findOneById(id);

        ArticleType save = articleTypeService.save(ArticleType.builder()
                .id(articleType.getId())
                .typeName(articleTypeRequest.getTypeName())
                .description(articleTypeRequest.getDescription())
                .build());

        return ResponseEntity.ok()
                .body(save);
    }

    @Secured({"ROLE_ADMIN", "ROLE_MANAGER", "ROLE_STAFF"})
    @DeleteMapping("/article/category/{id}")
    public ResponseEntity<?> deleteArticleCategory(@PathVariable Long id) {

        ArticleType articleType = articleTypeService.findOneById(id);

        Long deleteById = articleTypeService.deleteById(articleType.getId());

        return ResponseEntity.ok()
                .body(deleteById);
    }

    @Secured({"ROLE_ADMIN", "ROLE_MANAGER", "ROLE_STAFF"})
    @PostMapping("/article")
    public ResponseEntity<?> uploadArticle(
            @AuthenticationPrincipal MemberPrincipal memberPrincipal,
            @RequestBody @Valid CreateArticleRequest createArticleRequest) {

        ArticleType articleType = articleTypeService.findByTypeName(createArticleRequest.getArticleTypeName())
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "존재하지 않는 카테고리 입니다."));

        Article save = articleService.save(Article.builder()
                .title(createArticleRequest.getTitle())
                .content(createArticleRequest.getContent())
                .articleType(articleType)
                .createAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .member(memberPrincipal.getMember())
                .view(0)
                .build());

        return ResponseEntity.ok()
                .body(new CreateNoticeResponse(true, save.getId(), save.getTitle()));
    }

    @Secured({"ROLE_ADMIN", "ROLE_MANAGER", "ROLE_STAFF"})
    @GetMapping("/article/{id}")
    public ResponseEntity<?> getArticle(@PathVariable Long id) {

        Article article = articleService.findOneById(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "존재하지 않는 게시글 입니다."));

        return ResponseEntity.ok()
                .body(ArticleResponse.of(article));

    }

    @Secured({"ROLE_ADMIN", "ROLE_MANAGER", "ROLE_STAFF"})
    @GetMapping("/article/all")
    public ResponseEntity<?> getArticle(@RequestParam int page) {

        Page<Article> articles = articleService.findAllPage(page, 9);
        List<ArticleResponse> articleResponseList = articles.getContent()
                .stream()
                .map(ArticleResponse::of)
                .collect(Collectors.toList());

        return ResponseEntity.ok()
                .body(new ArticlePageResponse(articles.getTotalPages(), articleResponseList));
    }

    @Secured({"ROLE_ADMIN", "ROLE_MANAGER", "ROLE_STAFF"})
    @GetMapping("/article/my")
    public ResponseEntity<?> getMemberArticle(
            @AuthenticationPrincipal MemberPrincipal memberPrincipal,
            @RequestParam int page) {

        Member member = memberService.findUserById(memberPrincipal.getMember().getId())
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "인증되지 않은 유저 입니다."));
        Page<Article> articles = articleService.findAllByMember(member, page, 9);
        List<ArticleResponse> articleResponseList = articles
                .getContent()
                .stream()
                .map(ArticleResponse::of)
                .collect(Collectors.toList());

        return ResponseEntity.ok()
                .body(new ArticlePageResponse(articles.getTotalPages(), articleResponseList));
    }

    @Secured({"ROLE_ADMIN", "ROLE_MANAGER", "ROLE_STAFF"})
    @PatchMapping("/article/{id}")
    public ResponseEntity<?> modifyArticle(
            @AuthenticationPrincipal MemberPrincipal memberPrincipal,
            @RequestBody @Valid ModifyArticleRequest modifyArticleRequest,
            @PathVariable Long id) {

        Member member = memberService.findUserById(memberPrincipal.getMember().getId())
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "인증되지 않은 유저 입니다."));

        Article article = articleService.findOneById(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "존재하지 않는 게시글입니다."));

        if (article.getMember().getId() != member.getId()) {
            if (!member.isAdmin() || !member.isManager()) {
                throw new CustomException(HttpStatus.UNAUTHORIZED, "게시글 수정 권한이 없습니다.");
            }
        }
        ArticleType articleType = articleTypeService.findByTypeName(modifyArticleRequest.getArticleTypeName())
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "존재하지 않는 카테고리 입니다."));

        Article save = articleService.save(Article.builder()
                .id(id)
                .title(modifyArticleRequest.getTitle())
                .content(modifyArticleRequest.getContent())
                .articleType(articleType)
                .createAt(article.getCreateAt())
                .view(article.getView())
                .build());

        return ResponseEntity.ok()
                .body(ArticleResponse.of(save));
    }

    @Secured({"ROLE_ADMIN", "ROLE_MANAGER", "ROLE_STAFF"})
    @DeleteMapping("/article/{id}")
    public ResponseEntity<?> deleteArticle(
            @AuthenticationPrincipal MemberPrincipal memberPrincipal,
            @PathVariable Long id) {

        Member member = memberService.findUserById(memberPrincipal.getMember().getId())
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "인증되지 않은 유저 입니다."));

        Article article = articleService.findOneById(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "존재하지 않는 게시글입니다."));

        if (article.getMember().getId() != member.getId()) {
            if (!member.isAdmin() || !member.isManager()) {
                throw new CustomException(HttpStatus.UNAUTHORIZED, "게시글 삭제 권한이 없습니다.");
            }
        }
        Long deleteById = articleService.deleteById(article.getId());

        return ResponseEntity.ok()
                .body(deleteById);
    }

    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    @PostMapping("/building")
    public ResponseEntity<?> uploadBuildingInfo(
            @RequestBody @Valid BuildingRequest buildingRequest) {

        Building save = buildingInfoService.save(Building.builder()
                .name(buildingRequest.getName())
                .floors(buildingRequest.getFloors())
                .latitude(buildingRequest.getLatitude())
                .longitude(buildingRequest.getLongitude())
                .uniqueNumber(buildingRequest.getUniqueNumber())
                .description(buildingRequest.getDescription())
                .build());

        return ResponseEntity.ok()
                .body(save);
    }

    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    @PatchMapping("/building/{id}")
    public ResponseEntity<?> modifyBuildingInfo(
            @PathVariable("id") Long id,
            @RequestBody @Valid BuildingRequest buildingRequest) {

        Building save = buildingInfoService.save(Building.builder()
                .id(id)
                .name(buildingRequest.getName())
                .floors(buildingRequest.getFloors())
                .latitude(buildingRequest.getLatitude())
                .longitude(buildingRequest.getLongitude())
                .uniqueNumber(buildingRequest.getUniqueNumber())
                .description(buildingRequest.getDescription())
                .build());

        return ResponseEntity.ok()
                .body(save);
    }

    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    @DeleteMapping("/building/{id}")
    public ResponseEntity<?> deleteBuildingInfo(
            @PathVariable("id") Long id) {

        Long deleteById = buildingInfoService.deleteById(id);

        return ResponseEntity.ok()
                .body(deleteById);
    }

    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    @GetMapping("/building")
    public ResponseEntity<?> getPageBuildingInfo(
            @RequestParam int page
    ) {

        Page<Building> pageAll = buildingInfoService.findPageAll(page, 10);
        List<BuildingResponseDto> result = pageAll.getContent()
                .stream()
                .map(BuildingResponseDto::of)
                .collect(Collectors.toList());

        return ResponseEntity.ok()
                .body(new BuildingPageResponse(pageAll.getTotalPages(), result));
    }

    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    @GetMapping("/building/{id}")
    public ResponseEntity<?> getPageBuildingInfo(
            @PathVariable Long id
    ) {

        Building building = buildingInfoService.findOneById(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "존재하지 않는 건물입니다."));

        return ResponseEntity.ok()
                .body(BuildingResponseDto.of(building));
    }


    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    @GetMapping("/floor")
    public ResponseEntity<?> getFloors(
            @RequestParam Long buildingId) {

        Building building = buildingInfoService.findOneById(buildingId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "건물 정보가 없습니다."));

        List<Floor> floorByBuilding = floorService.findFloorByBuilding(building.getId());

        return ResponseEntity.ok()
                .body(floorByBuilding);
    }

    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    @PostMapping("/floor/{id}")
    public ResponseEntity<?> uploadFloor(
            @RequestParam("image") List<MultipartFile> image,
            @RequestBody @Valid FloorRequest floorRequest,
            @PathVariable("id") Long buildingId
    ) throws IOException {

        Building building = buildingInfoService.findOneById(buildingId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "건물 정보가 없습니다."));

        ArrayList<Floor> floorArrayList = new ArrayList<>();

        for (MultipartFile multipartFile : image) {
            if (!multipartFile.isEmpty()) {

                String uploadedDir = "/floorplan/" + building.getUniqueNumber();
                String upload = s3Service.upload(multipartFile, uploadedDir);

                Floor save = floorService.save(Floor.builder()
                        .description(floorRequest.getDescription())
                        .dir(upload)
                        .building(building)
                        .floorValue(floorRequest.getFloorValue())
                        .build());

                floorArrayList.add(save);
            }
        }

        List<FloorResponse> result = floorArrayList
                .stream()
                .map(FloorResponse::of)
                .collect(Collectors.toList());

        return ResponseEntity.ok()
                .body(result);
    }

    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    @PatchMapping("/floor/{id}")
    public ResponseEntity<?> modifyFloor(
            @RequestParam("image") MultipartFile image,
            @PathVariable("id") Long floorId,
            @RequestBody @Valid FloorRequest floorRequest
    ) throws IOException {

        Floor floor = floorService.findOneById(floorId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "층 정보가 없습니다."));

        String uploadedDir = "floorplan/" + floor.getBuilding().getUniqueNumber();
        String upload = s3Service.upload(image, uploadedDir);

        Floor save = floorService.save(Floor.builder()
                .id(floor.getId())
                .description(floorRequest.getDescription())
                .dir(upload)
                .building(floor.getBuilding())
                .floorValue(floorRequest.getFloorValue())
                .build());

        return ResponseEntity.ok()
                .body(FloorResponse.of(save));
    }

    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    @DeleteMapping("/floor/{id}")
    public ResponseEntity<?> deleteFloor(@PathVariable("id") Long id) {

        Floor floor = floorService.findOneById(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "층 정보가 없습니다."));

        Long deleteById = floorService.deleteById(floor.getId());

        return ResponseEntity.ok()
                .body(deleteById);
    }
}
