package soonmap.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import soonmap.dto.TokenDto;
import soonmap.entity.TokenType;
import soonmap.security.jwt.JwtProvider;


@Service
@RequiredArgsConstructor
public class TokenService {

    private final Long AccessexpireTimeMs = 300000l; // 5분
    private final Long RefreshExpireTimeMs = 1000 * 60 * 60 * 60L; // 3시간

    private final JwtProvider jwtProvider;

//    public TokenDto createTokens(String userEmail) {
//        TokenDto tokenDto = new TokenDto();
//        String accessToken = jwtProvider.createToken(userEmail, TokenType.ACCESS_TOKEN.getValue(), AccessexpireTimeMs);
//        String refreshToken = jwtProvider.createToken(userEmail, TokenType.REFRESH_TOKEN.getValue(), RefreshExpireTimeMs);
//
//
//        tokenDto.setAccessToken(accessToken);
//        tokenDto.setRefreshToken(refreshToken);
//
//        return tokenDto;
//    }
}
