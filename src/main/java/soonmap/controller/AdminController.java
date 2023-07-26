package soonmap.controller;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import soonmap.dto.MemberDto.*;
import soonmap.entity.Member;
import soonmap.exception.CustomException;
import soonmap.security.jwt.JwtProvider;
import soonmap.service.MemberService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final MemberService memberService;
    private final JwtProvider jwtProvider;

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
                .body(new AdminLoginResponse(true, member.isAdmin(), member.isWriter()));
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
                .body(new AdminResisterResponse(member.isAdmin(), member.isWriter()));
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
}
