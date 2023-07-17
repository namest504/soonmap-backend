package soonmap.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import soonmap.security.oauth.naver.NaverLoginBO;

@Configuration
public class Appconfig {
    @Bean
    public NaverLoginBO naverLoginBO() {
        return new NaverLoginBO();
    }
}
