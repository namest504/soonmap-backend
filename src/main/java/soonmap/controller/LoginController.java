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
import soonmap.Dto.MemberDto;
import soonmap.Dto.TokenDto;
import soonmap.entity.AccountType;
import soonmap.entity.Member;
import soonmap.security.oauth.naver.NaverLoginBO;
import soonmap.service.MemberService;
import soonmap.service.TokenService;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
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
    private final TokenService tokenService;
    private final NaverLoginBO naverLoginBO;
    private String apiResult = null;



    @RequestMapping(value = "/naver/code/{code}/state/{state}", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<String> callBack(@PathVariable String code, @PathVariable String state) {

        log.info(code);
        log.info(state);
        OAuth2AccessToken oauthToken;
        try {
            oauthToken = naverLoginBO.getAccessToken(code, state);

            apiResult = naverLoginBO.getUserProfile(oauthToken);

            JSONObject json = new JSONObject(apiResult);
            String name = json.getJSONObject("response").getString("name");
            String email = json.getJSONObject("response").getString("email");
            String id = json.getJSONObject("response").getString("id");

            Optional<Member> member = memberService.findUserById(id);
            if (member.isEmpty()) {
                MemberDto memberDto = new MemberDto(name, email, AccountType.NAVER, id);
                memberService.saveUser(memberDto);
            }
            TokenDto tokenDto = tokenService.createTokens(email);

            ResponseCookie responseCookie = memberService.createHttpOnlyCookie(tokenDto);
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                    .body("Name: " + name + ", Email: " + email+ ", id: " + id);
        } catch (IOException e) {
            // 예외 처리
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to get user profile.");
        }
    }

    @RequestMapping(value = "/refresh", method = RequestMethod.GET)
    public ResponseEntity<String> refresh(@RequestParam String refreshToken) {
        try {
            OAuth2AccessToken newAccessToken = naverLoginBO.refreshAccessToken(refreshToken);
            if (newAccessToken != null) {
                String newAccessTokenValue = newAccessToken.getAccessToken();

                // 갱신된 AccessToken을 사용하여 추가 작업 수행
                // 예시: naverLoginBO.getUserProfile(newAccessToken);

                return ResponseEntity.ok("New Access Token: " + newAccessTokenValue);
            } else {
                // AccessToken을 갱신할 수 없는 경우
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to refresh access token.");
            }
        } catch (IOException e) {
            // 예외 처리
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to refresh access token.");
        }
    }
    @ResponseBody
    @GetMapping("/remove") //token = access_token임
    public String remove(@RequestParam String token, HttpSession session, HttpServletRequest request) {

        String deleteURL = naverLoginBO.removeAccessToken(token);
        session.invalidate();

        return "deleteURL";
    }

}
