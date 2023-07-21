package soonmap.controller;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    public AdminLoginResponse adminLogin(@RequestBody AdminLoginRequest adminLoginRequest) {
        Member member = memberService.loginAdmin(adminLoginRequest);
        String accessToken = jwtProvider.createAccessToken(member.getUserEmail());
        String refreshToken = jwtProvider.createRefreshToken(member.getUserEmail());
        memberService.saveAdminRefreshToken(member.getId(), refreshToken);
        return new AdminLoginResponse(true, member.isAdmin(), member.isWriter(), accessToken, refreshToken);
    }

    @GetMapping("/refresh")
    public String refreshAdminToken(@RequestParam String token) {
        Claims claims = jwtProvider.decodeJwtToken(token);

        if (!claims.getSubject().equals("refresh_token")) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "잘못된 요청입니다.");
        }

        String userEmail = claims.get("userEmail", String.class);
        Member member = memberService.findUserByEmail(userEmail)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "잘못된 요청입니다."));

        if (memberService.getAdminRefreshToken(member.getId()) == null) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "잘못된 요청입니다.");
        }

        String accessToken = jwtProvider.createAccessToken(userEmail);
        return accessToken;
    }

    @PostMapping("/register")
    public AdminResisterResponse resisterAdmin(@RequestBody AdminResisterRequest adminResisterRequest) {
        Member member = memberService.addAdmin(adminResisterRequest);
        return new AdminResisterResponse(member.isAdmin(), member.isWriter());
    }

    @Secured("ROLE_ADMIN")
    @GetMapping("/account/admin")
    public AccountListResponse getAdminAccount() {
        List<Member> adminAccount = memberService.findAdminAccount();
        List<Account> collect = adminAccount.stream().map(Account::of).collect(Collectors.toList());
        return new AccountListResponse(adminAccount.size(), collect);
    }

    @Secured("ROLE_ADMIN")
    @GetMapping("/account/all")
    public AccountListResponse getAllAccount() {
        List<Member> adminAccount = memberService.findAll();
        List<Account> collect = adminAccount.stream().map(Account::of).collect(Collectors.toList());
        return new AccountListResponse(adminAccount.size(), collect);
    }

    @Secured("ROLE_ADMIN")
    @PutMapping("/manage/ban")
    public Account manageIsBanAccount(@RequestParam Long id) {
        Member member = memberService.findUserById(id);
        member.setBan(!member.isBan());
        Member savedUser = memberService.editUser(member);
        return Account.of(savedUser);
    }
}
