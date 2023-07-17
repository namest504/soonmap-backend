package soonmap.security.oauth.naver;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Appconfig {
    @Bean
    public NaverLoginBO naverLoginBO() {
        return new NaverLoginBO();
    }
}
