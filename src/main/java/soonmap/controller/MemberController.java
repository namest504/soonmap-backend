package soonmap.controller;


import com.github.scribejava.core.model.OAuth2AccessToken;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import soonmap.dto.EmailDto;
import soonmap.dto.EmailDto.ConfirmFindIdEmailRequest;
import soonmap.dto.EmailDto.ConfirmFindIdEmailResponse;
import soonmap.dto.EmailDto.ConfirmFindPwEmailRequest;
import soonmap.dto.EmailDto.ConfirmFindPwEmailResponse;
import soonmap.dto.EmailDto.FindPwEmailRequest;
import soonmap.dto.MemberDto.LoginRequest;
import soonmap.dto.SocialUserInfoDto;
import soonmap.dto.TokenDto.RefreshTokenRequest;
import soonmap.entity.AccountType;
import soonmap.entity.Member;
import soonmap.exception.CustomException;
import soonmap.security.jwt.JwtProvider;

import soonmap.security.jwt.MemberPrincipal;
import soonmap.security.oauth.KakaoLoginBO;
import soonmap.security.oauth.NaverLoginBO;
import soonmap.service.MailService;
import soonmap.service.MemberService;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Optional;

import static soonmap.dto.MemberDto.*;

/**
 * Handles requests for the application home page.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final JwtProvider jwtProvider;
    private final NaverLoginBO naverLoginBO;
    private final KakaoLoginBO kakaoLoginBO;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;


    private String apiResult = null;

    /**
     * naver oauth
     */

    // Client가 Server에게 보낸 code, state를 받고 AccessToken을 생성 후 사용자 정보를 추출한다.
    @RequestMapping(value = "/naver/callback", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<String> callback(@RequestParam String code, @RequestParam String state) {
        OAuth2AccessToken oauthToken;
        try {
            oauthToken = naverLoginBO.getAccessToken(code, state);
            SocialUserInfoDto apiResult = naverLoginBO.getUserProfile(oauthToken);

            String id = apiResult.getId();
            String email = apiResult.getEmail();
            String name = apiResult.getNickname();

            // Member의 존재 여부에 따라 사용자 저장 여부 결정
            Optional<Member> member = memberService.findUserBySnsId(id);
            if (member.isEmpty()) {
                SocialMemberResponse naverMemberResponse = new SocialMemberResponse(name, email, AccountType.NAVER, id);
                memberService.SocialsaveUser(naverMemberResponse);
                log.info("이미 있는 유저입니다.");
            }
            log.info("새 유저입니다.");

            //AccessToken과 RefreshToken 분리 생성
            String AccessToken = jwtProvider.createAccessToken(member.get().getId());
            String RefreshToken = jwtProvider.createRefreshToken(member.get().getId());

            ResponseCookie responseCookie = memberService.createHttpOnlyCookie(RefreshToken);

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                    //.header("accessToken", AccessToken)
                    .body(AccessToken);
        } catch (IOException e) {
            // 예외 처리
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to get user profile.");
        }
    }

    //todo: jwt 토큰 갱신, 삭제 로직 구현 필요
    //RE: 밑의 코드는 네이버 토큰 관련 코드여서 저희에게는 필요 없다고 판단 후, 삭제하였습니다.

    /**
     * kakao oauth
     */
    @RequestMapping(value = "/kakao/callback", method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<String> kakaoLogin(@RequestParam("code") String code) {
        OAuth2AccessToken oAuthToken;
        try {
            log.info("인가 코드를 이용하여    토큰을 받습니다.");
            oAuthToken = kakaoLoginBO.getAccessToken(code);
            log.info("토큰에 대한 정보입니다.{}", oAuthToken);
            SocialUserInfoDto userInfo = kakaoLoginBO.getKakaoUserInfo(oAuthToken);
            String id = userInfo.getId();
            String name = userInfo.getNickname();
            String email = userInfo.getEmail();

            // Member의 존재 여부에 따라 사용자 저장 여부 결정
            Optional<Member> member = memberService.findUserBySnsId(id);
            if (member.isEmpty()) {
                SocialMemberResponse kakaoMemberResponse = new SocialMemberResponse(name, email, AccountType.KAKAO, id);
                memberService.SocialsaveUser(kakaoMemberResponse);
                log.info("이미 있는 유저입니다.");
            }
            log.info("새 유저입니다.");

            //AccessToken과 RefreshToken 분리 생성
            String AccessToken = jwtProvider.createAccessToken(member.get().getId());
            String RefreshToken = jwtProvider.createRefreshToken(member.get().getId());

            ResponseCookie responseCookie = memberService.createHttpOnlyCookie(RefreshToken);

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                    //.header("accessToken", AccessToken)
                    .body(AccessToken);

        } catch (HttpClientErrorException.BadRequest ex) {
            // HttpClientErrorException$BadRequest 처리

            log.error("Kakao API Bad Request: " + ex.getStatusCode() + " " + ex.getStatusText());
            return ResponseEntity.badRequest().body("Kakao API Bad Request: " + ex.getStatusCode() + " " + ex.getStatusText());
        } catch (Exception ex) {
            // 그 외 모든 예외 처리
            log.error("Error occurred during Kakao login: " + ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred during Kakao login: " + ex.getMessage());
        }
    }

    @PostMapping("/join/check")
    public ResponseEntity<Void> sendEmailWithAuthCode(@RequestBody @Valid MemberEmailRequest memberEmailRequest) {
        if (memberService.findUserByEmail(memberEmailRequest.getEmail()).isPresent()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "이미 가입된 이메일 입니다.");
        }
        if (!memberEmailRequest.getEmail().matches("^[a-z0-9]+$")) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "입력된 형식이 올바르지 않습니다.");
        }
        String userEmailWithSCH = memberEmailRequest.getEmail() + "@sch.ac.kr";
        String generatedAuthCode = generateAuthCode();
        mailService.mailSend(userEmailWithSCH, generatedAuthCode);
        memberService.saveJoinConfirmAuthCode(userEmailWithSCH, generatedAuthCode);
        return ResponseEntity.ok()
                .build();
    }
    private String generateAuthCode() {
        return String.valueOf((int) (Math.random() * 1000000) + 100000);
    }

    @PostMapping("/join/check/confirm")
    public ResponseEntity<MemberEmailConfirmResponse> confirmEmailWithAuthCode(@RequestBody @Valid MemberEmailConfirmRequest memberEmailConfirmRequest) {
        String joinConfirmAuthCode = memberService.findJoinConfirmAuthCode(memberEmailConfirmRequest.getEmail());
        if (!memberEmailConfirmRequest.getAuthCode().equals(joinConfirmAuthCode)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "올바르지 않은 인증 코드입니다.");
        }
        String authConfirmToken = jwtProvider.createAuthConfirmToken(memberEmailConfirmRequest.getEmail());

        return ResponseEntity.ok()
                .body(new MemberEmailConfirmResponse(authConfirmToken));
    }


    @PostMapping("/join")
    public ResponseEntity<Void> joinMember(@RequestBody @Valid MemberJoinRequest memberJoinRequest) {
        if (memberService.findUserByEmail(memberJoinRequest.getEmail()).isPresent()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "이미 가입된 이메일 입니다.");
        }
        if (memberService.findUserByUserId(memberJoinRequest.getId()).isPresent()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "이미 가입된 아이디 입니다.");
        }
        Claims claims = jwtProvider.decodeJwtToken(memberJoinRequest.getRegisterToken());
        Member member = memberService.saveUser(claims, memberJoinRequest);

        return ResponseEntity.ok()
                .build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> UserLogin(@RequestBody @Valid LoginRequest loginRequest) {
        Member member = memberService.loginUser(loginRequest);

        String accessToken = jwtProvider.createAccessToken(member.getId());
        String refreshToken = jwtProvider.createRefreshToken(member.getId());

        memberService.saveUserRefreshToken(member.getId(), refreshToken);
        return ResponseEntity.ok()
//                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body(new LoginResponse(true, member.getUsername(), accessToken, refreshToken));
    }

    @PostMapping("/refresh")
//    public ResponseEntity<?> refreshAdminToken(@CookieValue("refreshToken") String token) {
    public ResponseEntity<?> refreshUserToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        Claims claims = jwtProvider.decodeJwtToken(refreshTokenRequest.getRefreshToken());

        if (!claims.getSubject().equals("refresh_token")) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "잘못된 요청입니다.");
        }

        Long uid = claims.get("uid", Long.class);
        Member member = memberService.findUserById(uid)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "존재하지 않는 유저입니다."));

        if (memberService.getUserRefreshToken(member.getId()) == null) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "잘못된 요청입니다.");
        }

        String accessToken = jwtProvider.createAccessToken(uid);
        return ResponseEntity.ok()
//                .header("accessToken", accessToken)
                .body(accessToken);
    }
    @PostMapping("/find/id")
    public ResponseEntity<?> sendFindIdMail(@RequestBody @Valid EmailDto.FindIdEmailRequest findIdEmailRequest) {
        Member member = memberService.findUserByEmail(findIdEmailRequest.getReceiver())
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "존재하지 않는 계정입니다."));

        String authCode = generateAuthCode();
        memberService.saveFindIdConfirmAuthCode(findIdEmailRequest.getReceiver(), authCode);
        mailService.mailSend(findIdEmailRequest.getReceiver(), authCode);
        return ResponseEntity.ok()
                .body(findIdEmailRequest.getReceiver());
    }

    @PostMapping("/find/id/confirm")
    public ResponseEntity<?> confirmFindIdMail(@RequestBody @Valid ConfirmFindIdEmailRequest confirmFindIdEmailRequest) {
        String findIdConfirmAuthCode = memberService.getFindIdConfirmAuthCode(confirmFindIdEmailRequest.getReceiver());

        if (findIdConfirmAuthCode == null || !findIdConfirmAuthCode.equals(confirmFindIdEmailRequest.getCode())) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "인증에 문제가 발생하였습니다.");
        }

        Member member = memberService.findUserByEmail(confirmFindIdEmailRequest.getReceiver())
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "존재하지 않는 계정입니다."));

        if (!memberService.deleteFindIdConfirmAuthCode(confirmFindIdEmailRequest.getReceiver())) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "인증에 문제가 발생하였습니다.");
        }

        return ResponseEntity.ok()
                .body(new ConfirmFindIdEmailResponse(member.getUserId()));
    }

    @PostMapping("/find/pw")
    public ResponseEntity<?> sendFindPwMail(@RequestBody FindPwEmailRequest findPwEmailRequest) {
        Member member = memberService.findUserByEmail(findPwEmailRequest.getReceiver())
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "존재하지 않는 계정입니다."));

        if (!member.getUserId().equals(findPwEmailRequest.getId())) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "존재하지 않는 계정입니다.");
        }

        String authCode = generateAuthCode();
        memberService.saveFindPwConfirmAuthCode(findPwEmailRequest.getReceiver(), authCode);
        mailService.mailSend(findPwEmailRequest.getReceiver(), authCode);

        return ResponseEntity.ok()
                .body(findPwEmailRequest.getReceiver());
    }

    @PostMapping("/find/pw/confirm")
    public ResponseEntity<?> confirmFindPwMail(@RequestBody @Valid ConfirmFindPwEmailRequest confirmFindPwEmailRequest) {
        Member member = memberService.findUserByEmail(confirmFindPwEmailRequest.getReceiver())
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "존재하지 않는 계정입니다."));

        String findIdConfirmAuthCode = memberService.getFindPwConfirmAuthCode(confirmFindPwEmailRequest.getReceiver());

        if (findIdConfirmAuthCode == null || !findIdConfirmAuthCode.equals(confirmFindPwEmailRequest.getCode())) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "인증에 문제가 발생하였습니다.");
        }

        member.setUserPassword(passwordEncoder.encode(confirmFindPwEmailRequest.getPw()));
        Member save = memberService.save(member);

        if (!memberService.deleteFindIdConfirmAuthCode(confirmFindPwEmailRequest.getReceiver())) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "인증에 문제가 발생하였습니다.");
        }

        return ResponseEntity.ok()
                .body(new ConfirmFindPwEmailResponse(save.getUserId()));
    }

    @GetMapping("/logout")
    public ResponseEntity<Boolean> logoutUser(@AuthenticationPrincipal MemberPrincipal memberPrincipal) {
        Boolean result = memberService.logoutUserRefreshToken(memberPrincipal.getMember().getId());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<Boolean> withdrawUser(@AuthenticationPrincipal MemberPrincipal memberPrincipal) {
        Boolean result = memberService.withdrawUser(memberPrincipal.getMember().getId());
        return ResponseEntity.ok()
                .body(result);
    }
}
