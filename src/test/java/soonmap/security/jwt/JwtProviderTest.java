package soonmap.security.jwt;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.util.Base64Utils;
import soonmap.entity.Member;
import soonmap.exception.CustomException;
import soonmap.repository.MemberRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtProviderTest {

    private static final String JWT_SECRET_KEY = "tempkeytempkeytempkeytempkeytempkeytempkeytempkeytempkey";
    private static final String USER_EMAIL = "tester@example.com";

    @InjectMocks
    private JwtProvider jwtProvider;

    @Mock
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(jwtProvider, "JWT_SECRET_KEY", JWT_SECRET_KEY);
    }

    @Test
    void createAccessTokenTest() {
        // given
        String accessToken = jwtProvider.createAccessToken(USER_EMAIL);
        // when

        // then
        assertNotNull(accessToken);

        Claims claims = Jwts.parser()
                .setSigningKey(Base64Utils.encodeToString(JWT_SECRET_KEY.getBytes()))
                .parseClaimsJws(accessToken)
                .getBody();

        assertEquals("access_token", claims.getSubject());
        assertEquals(USER_EMAIL, claims.get("userEmail"));
    }

    @Test
    void createRefreshToken() {
        // given
        String refreshToken = jwtProvider.createRefreshToken(USER_EMAIL);
        // when

        // then
        assertNotNull(refreshToken);

        Claims claims = Jwts.parser()
                .setSigningKey(Base64Utils.encodeToString(JWT_SECRET_KEY.getBytes()))
                .parseClaimsJws(refreshToken)
                .getBody();

        assertEquals("refresh_token", claims.getSubject());
        assertEquals(USER_EMAIL, claims.get("userEmail"));
    }
    @Test
    void getAuthenticationTest() {
        // given
        String accessToken = jwtProvider.createAccessToken(USER_EMAIL);
        Member member = mock(Member.class);
        when(memberRepository.findMemberByUserEmail(USER_EMAIL)).thenReturn(Optional.of(member));

        // when
        UsernamePasswordAuthenticationToken authentication = jwtProvider.getAuthentication(accessToken);

        // then
        assertEquals(accessToken, authentication.getCredentials().toString());
        assertTrue(authentication.getPrincipal() instanceof MemberPrincipal);
    }

    @Test
    void getEmailFromTokenTest() {
        // given
        String accessToken = jwtProvider.createAccessToken(USER_EMAIL);

        // when
        String email = jwtProvider.getEmailFromToken(accessToken);

        // then
        assertEquals(USER_EMAIL, email);
    }

    @Test
    void validateAccessTokenTest() {
        // given
        String accessToken = jwtProvider.createAccessToken(USER_EMAIL);

        // when
        boolean isValid = jwtProvider.validateAccessToken(accessToken);

        // then
        assertTrue(isValid);
    }

    @Test
    void validateAccessTokenWithInvalidTokenTest() {
        // given
        String invalidToken = "invalid.token.string";

        // then
        assertThrows(CustomException.class, () -> jwtProvider.validateAccessToken(invalidToken));
    }

    @Test
    void decodeJwtTokenTest() {
        // given
        String accessToken = jwtProvider.createAccessToken(USER_EMAIL);

        // when
        Claims claims = jwtProvider.decodeJwtToken(accessToken);

        // then
        assertEquals("access_token", claims.getSubject());
        assertEquals(USER_EMAIL, claims.get("userEmail"));
    }

    @Test
    void decodeJwtTokenWithInvalidTokenTest() {
        // given
        String invalidToken = "invalid.token.string";

        // then
        assertThrows(CustomException.class, () -> jwtProvider.decodeJwtToken(invalidToken));
    }
}
