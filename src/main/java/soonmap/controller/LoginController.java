package soonmap.controller;


import com.github.scribejava.core.model.OAuth2AccessToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import soonmap.dto.MemberDto.NaverMemberResponse;
import soonmap.dto.TokenDto;
import soonmap.entity.AccountType;
import soonmap.entity.Member;
import soonmap.security.jwt.JwtProvider;
import soonmap.security.oauth.naver.NaverLoginBO;
import soonmap.service.MemberService;

import java.io.IOException;
import java.util.Optional;

/**
 * Handles requests for the application home page.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class LoginController {

    private final MemberService memberService;
    private final JwtProvider jwtProvider;
    private final NaverLoginBO naverLoginBO;
    private String apiResult = null;


    // Client가 Server에게 보낸 code, state를 받고 AccessToken을 생성 후 사용자 정보를 추출한다.
    @RequestMapping(value = "/callback", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<String> callback(@RequestParam String code, @RequestParam String state) {
        OAuth2AccessToken oauthToken;
        try {
            oauthToken = naverLoginBO.getAccessToken(code, state);
            apiResult = naverLoginBO.getUserProfile(oauthToken);

            JSONObject json = new JSONObject(apiResult);
            String name = json.getJSONObject("response").getString("name");
            String email = json.getJSONObject("response").getString("email");
            String id = json.getJSONObject("response").getString("id");

            // Member의 존재 여부에 따라 사용자 저장 여부 결정
            Optional<Member> member = Optional.ofNullable(memberService.findUserByEmail(email));
            if (member.isEmpty()) {
                NaverMemberResponse naverMemberResponse = new NaverMemberResponse(name, email, AccountType.NAVER, id);
                memberService.saveUser(naverMemberResponse);
                log.info("이미 있는 유저입니다.");
            }
            log.info("새 유저입니다.");

            //AccessToken과 RefreshToken 분리 생성
            String AccessToken = jwtProvider.createAccessToken(email);
            String RefreshToken = jwtProvider.createRefreshToken(email);

            TokenDto tokenDto = new TokenDto(AccessToken, RefreshToken);

            ResponseCookie responseCookie = memberService.createHttpOnlyCookie(tokenDto);
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                    .body("Name: " + name + ", Email: " + email+ ", id: " + id);
        } catch (IOException e) {
            // 예외 처리
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to get user profile.");
        }
    }

    //todo: jwt 토큰 갱신, 삭제 로직 구현 필요
    //RE: 밑의 코드는 네이버 토큰 관련 코드여서 저희에게는 필요 없다고 판단 후, 삭제하였습니다.

}
