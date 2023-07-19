package soonmap.security.oauth.naver;



import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import soonmap.dto.SocialUserInfoDto;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

@Slf4j
public class NaverLoginBO {

    @Value("${spring.client.id}")
    private String CLIENT_ID;

    @Value("${spring.client.secret}")
    private String CLIENT_SECRET;

    @Value("${spring.client.redirect-uri}")
    private String REDIRECT_URI;

    @Value("${spring.client.session-state}")
    private String SESSION_STATE;

    @Value("${spring.profile-api-url}")
    private String PROFILE_API_URL;

    private NaverOAuthApi naverOAuthApi;
    public String getAuthorizationUrl(HttpSession session) {
        //세션 유효성 검증을 위하여 난수를 생성
        String state = generateRandomString();
        // 생성한 난수 값을 session에 저장
        setSession(session, state);
//        Scribe에서 제공하는 인증 URL 생성 기능을 이용하여 네아로 인증 URL 생성
        OAuth20Service oAuth20Service = new ServiceBuilder()
                .apiKey(CLIENT_ID)
                .apiSecret(CLIENT_SECRET)
                .callback(REDIRECT_URI)
                .state(state)
                .build(NaverOAuthApi.instance());
        return oAuth20Service.getAuthorizationUrl();
    }

//    네이버아이디로 Callback처리 및 AccessToken 획득 Method
    public OAuth2AccessToken getAccessToken(String code, String state)throws IOException {
        log.info(code);
        log.info(state);
        //Callback으로 전달받은 세션검증용 난수값과 저장되어있는 값이 일치하는지 확인

        OAuth20Service oAuth20Service = new ServiceBuilder()
                .apiKey(CLIENT_ID)
                .apiSecret(CLIENT_SECRET)
                .callback(REDIRECT_URI)
                .state(state)
                .build(NaverOAuthApi.instance());
        log.info(oAuth20Service.getAccessToken(code).toString());
        OAuth2AccessToken accessToken = oAuth20Service.getAccessToken(code);
        log.info(accessToken.toString());
        return accessToken;
    }

    public OAuth2AccessToken refreshAccessToken(String refreshToken) throws IOException {
        // RefreshToken을 사용하여 AccessToken을 갱신합니다.
        OAuth20Service oAuth20Service = new ServiceBuilder()
                .apiKey(CLIENT_ID)
                .apiSecret(CLIENT_SECRET)
                .callback(REDIRECT_URI)
                .build(NaverOAuthApi.instance());
        OAuth2AccessToken accessToken = oAuth20Service.refreshAccessToken(refreshToken);

        // AccessToken 갱신 성공 시 새로운 AccessToken을 반환합니다.
        if (accessToken != null) {
            return accessToken;
        }

        return null;
    }

    private String generateRandomString() {
        return UUID.randomUUID().toString();
    }

    private void setSession(HttpSession session, String state) {
        session.setAttribute(SESSION_STATE, state);
    }

    private String getSession(HttpSession session) {
        return (String) session.getAttribute(SESSION_STATE);
    }
//    AccessToken을 이용해 네이버 사용자프로필 API를 호출
    public SocialUserInfoDto getUserProfile(OAuth2AccessToken oAuth2AccessToken) throws  IOException {
        OAuth20Service oAuth20Service = new ServiceBuilder()
                .apiKey(CLIENT_ID)
                .apiSecret(CLIENT_SECRET)
                .callback(REDIRECT_URI).build(NaverOAuthApi.instance());
        OAuthRequest request = new OAuthRequest(Verb.GET, PROFILE_API_URL, oAuth20Service);
        oAuth20Service.signRequest(oAuth2AccessToken, request);
        Response response = request.send();

        JSONObject json = new JSONObject(response.getBody());
        String name = json.getJSONObject("response").getString("name");
        String email = json.getJSONObject("response").getString("email");
        String id = json.getJSONObject("response").getString("id");

        SocialUserInfoDto socialUserInfoDto = new SocialUserInfoDto(id, name, email);
        return socialUserInfoDto;
    }

    public String removeAccessToken(String accessToken) {
        String removeTokenApi = naverOAuthApi.deleteAccessToken();
        String apiURL = removeTokenApi + CLIENT_ID +
                "&client_secret=" + CLIENT_SECRET + "&access_token=" + accessToken.replaceAll("'", "") + "&service_provider=NAVER";
        return apiURL;
    }

    public boolean validateAccessToken(String accessToken) {
        try {
            // API 호출을 통해 AccessToken의 유효성을 검사합니다.
            // 네이버 API의 사용자 프로필 엔드포인트를 호출하여 응답 코드를 확인합니다.
            URL url = new URL("https://openapi.naver.com/v1/nid/me");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);

            int responseCode = connection.getResponseCode();
            // 200 OK 응답 코드인 경우에는 AccessToken이 유효합니다.
            return responseCode == 200;
        } catch (IOException e) {
            e.printStackTrace();
            // 예외 발생 시 AccessToken은 유효하지 않은 것으로 처리합니다.
            return false;
        }
    }
}
