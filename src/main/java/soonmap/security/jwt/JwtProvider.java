package soonmap.security.jwt;

import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;
import soonmap.entity.Member;
import soonmap.exception.CustomException;
import soonmap.repository.MemberRepository;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    @Value("${JWT_SECRET_KEY}")
    private String JWT_SECRET_KEY;

    private final Long accessTokenValidTime = 1000 * 60L * 5L; // 5분
    private final Long refreshTokenValidTime = 1000 * 60 * 60 * 24 * 7L; // 1주

    private final MemberRepository memberRepository;

    public String createAccessToken(Long uid) {
        Claims claims = Jwts.claims().setSubject("access_token");
        claims.put("uid", uid);
        Date currentTime = new Date();

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(currentTime)
                .setExpiration(new Date(currentTime.getTime() + accessTokenValidTime))
                .signWith(SignatureAlgorithm.HS256, Base64Utils.encodeToString(JWT_SECRET_KEY.getBytes()))
                .compact();
    }

    public String createRefreshToken(Long uid) {
        Claims claims = Jwts.claims().setSubject("refresh_token");
        claims.put("uid", uid);
        Date currentTime = new Date();

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(currentTime)
                .setExpiration(new Date(currentTime.getTime() + refreshTokenValidTime))
                .signWith(SignatureAlgorithm.HS256, Base64Utils.encodeToString(JWT_SECRET_KEY.getBytes()))
                .compact();
    }

    public UsernamePasswordAuthenticationToken getAuthentication(String token) {
        Long uid = getUidFromToken(token);
        Member member = memberRepository.findMemberById(uid)
                .orElseThrow(() -> new RuntimeException("Member 를 찾지 못했습니다."));
        MemberPrincipal memberPrincipal = new MemberPrincipal(member);
        return new UsernamePasswordAuthenticationToken(memberPrincipal, token,
                member.getAuthorities());
    }

//    public String getEmailFromToken(String token) {
//        return Jwts.parserBuilder()
//                .setSigningKey(Base64Utils.encodeToString(JWT_SECRET_KEY.getBytes()))
//                .build().parseClaimsJws(token)
//                .getBody()
//                .get("userEmail",
//                        String.class);
//    }

    public Long getUidFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Base64Utils.encodeToString(JWT_SECRET_KEY.getBytes()))
                .build().parseClaimsJws(token)
                .getBody()
                .get("uid",
                        Long.class);
    }

    public Boolean validateAccessToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(Base64Utils.encodeToString(JWT_SECRET_KEY.getBytes()))
                    .build()
                    .parseClaimsJws(token).getBody();

            return true;
        } catch (Exception e) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "잘못된 토큰입니다.");
        }
    }

    public Claims decodeJwtToken(String token) {
        try {
            Claims claim = Jwts.parserBuilder().setSigningKey(Base64Utils.encodeToString(JWT_SECRET_KEY.getBytes())).build()
                    .parseClaimsJws(token).getBody();
            return claim;
        } catch (SecurityException | MalformedJwtException | ExpiredJwtException
                 | UnsupportedJwtException
                 | IllegalArgumentException | SignatureException e) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "잘못된 접근입니다.");
        }
    }
}
