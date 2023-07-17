package soonmap.security.jwt;

import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import soonmap.entity.Member;
import soonmap.exception.CustomException;
import soonmap.repository.MemberRepository;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    @Value("${JWT_SECRET_KET}")
    private String JWT_SECRET_KET;

    private final MemberRepository memberRepository;

    public String createToken(String email, String TokenType, Long Token_Expire_Time) {
        Claims claims = Jwts.claims().setSubject("access_token");
        claims.put("userEmail", email);
        Date currentTime = new Date();

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(currentTime)
                .setExpiration(new Date(currentTime.getTime() + Token_Expire_Time))
                .signWith(SignatureAlgorithm.HS256, JWT_SECRET_KET)
                .compact();
    }

    public UsernamePasswordAuthenticationToken getAuthentication(String token) {
        Long uid = getUidFromToken(token);
        Member member = memberRepository.findById(uid)
                .orElseThrow(() -> new RuntimeException("Member 를 찾지 못했습니다."));
        MemberPrincipal memberPrincipal = new MemberPrincipal(member);
        return new UsernamePasswordAuthenticationToken(memberPrincipal, token,
                member.getAuthorities());
    }

    public Long getUidFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(JWT_SECRET_KET).build().parseClaimsJws(token).getBody()
                .get("uid",
                        Long.class);
    }

    public Boolean validateAccessToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(JWT_SECRET_KET).build()
                    .parseClaimsJws(token).getBody();

            return true;
        } catch (Exception e) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "잘못된 토큰입니다.");
        }
    }

    public Claims decodeJwtToken(String token) {
        try {
            Claims claim = Jwts.parserBuilder().setSigningKey(JWT_SECRET_KET).build()
                    .parseClaimsJws(token).getBody();
            return claim;
        } catch (SecurityException | MalformedJwtException | ExpiredJwtException
                 | UnsupportedJwtException
                 | IllegalArgumentException | SignatureException e) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "잘못된 접근입니다.");
        }
    }
}
