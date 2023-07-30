package soonmap.dto;

import lombok.*;

public class TokenDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefreshTokenRequest {
        private String refreshToken;
    }

}