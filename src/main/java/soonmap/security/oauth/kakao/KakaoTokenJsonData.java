package soonmap.security.oauth.kakao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

// 인가코드를 이용하여 Token ( Access , Refresh )를 받는다.
@Component
@RequiredArgsConstructor
@Slf4j
public class KakaoTokenJsonData {
    private static final String TOKEN_URI = "https://kauth.kakao.com/oauth/token";
    @Value("${spring.kakao.redirect_uri}")
    private String REDIRECT_URI;
    private static final String GRANT_TYPE = "authorization_code";
    @Value("${spring.kakao.client_id}")
    private String CLIENT_ID;



    // 1. "인가 코드"로 "액세스 토큰" 요청
    public String getToken(String code) {
        try {
            // HTTP Header 생성
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

            // HTTP Body 생성
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", GRANT_TYPE);
            body.add("client_id", CLIENT_ID);
            body.add("redirect_uri", REDIRECT_URI);
            body.add("code", code);

            // HTTP 요청 보내기
            HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest = new HttpEntity<>(body, headers);
            RestTemplate rt = new RestTemplate();
            ResponseEntity<String> response = rt.exchange(
                    "https://kauth.kakao.com/oauth/token",
                    HttpMethod.POST,
                    kakaoTokenRequest,
                    String.class
            );

            // HTTP 응답 (JSON) -> 액세스 토큰 파싱
            String responseBody = response.getBody();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            return jsonNode.get("access_token").asText();
        } catch (IOException e) {
            // IOException 처리
            log.error("Error occurred while getting Kakao access token: " + e.getMessage(), e);
            // 예외를 던지는 대신, null이나 빈 문자열 등의 기본 값을 리턴하거나,
            // 애플리케이션의 요구에 맞게 적절한 에러 처리 방법을 선택할 수 있습니다.
            // 여기서는 null을 리턴하도록 처리합니다.
            return null;
        }
    }

}



