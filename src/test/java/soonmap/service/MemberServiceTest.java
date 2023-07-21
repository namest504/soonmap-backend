package soonmap.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import soonmap.dto.MemberDto.AdminLoginRequest;
import soonmap.entity.AccountType;
import soonmap.entity.Member;
import soonmap.exception.CustomException;
import soonmap.repository.MemberRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberService memberService;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private Member testMember;

    @Test
    void saveAdminRefreshTokenTest() {
        // given
        Long memberId = 1L;
        String refreshToken = "example_refresh_token";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // when
        memberService.saveAdminRefreshToken(memberId, refreshToken);

        // then
        verify(redisTemplate, times(1)).opsForValue();
        verify(valueOperations, times(1)).set("RefreshToken-ADMIN-" + memberId, refreshToken, Duration.ofDays(7));
    }

    @Test
    void getAdminRefreshToken() {
        // given
        Long memberId = 1L;
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        // when
        memberService.getAdminRefreshToken(memberId);

        // then
        verify(redisTemplate, times(1)).opsForValue();
        verify(valueOperations, times(1)).get("RefreshToken-ADMIN-" + memberId);

    }

    @Test
    public void adminLogin_Success() {
        testMember = new Member(1L, "test@email.com", "test", "testPassword", AccountType.ADMIN, false, true, true, "testSnsId", LocalDateTime.now());
        AdminLoginRequest request = new AdminLoginRequest(testMember.getUserEmail(), "test_password");

        when(memberRepository.findMemberByUserEmail(request.getEmail())).thenReturn(Optional.of(testMember));
        when(passwordEncoder.matches(any(),any())).thenReturn(true);

        Member result = memberService.loginAdmin(request);

        assertEquals(testMember, result);
        verify(memberRepository, times(1)).findMemberByUserEmail(request.getEmail());
        verify(passwordEncoder, times(1)).matches(request.getPassword(), testMember.getPassword());
    }

    @Test
    public void adminLogin_ForbiddenUserInfo() {
        testMember = new Member(1L, "forbidden@email.com", "test", "testPassword", AccountType.ADMIN, true, true, true, "testSnsId", LocalDateTime.now());
        AdminLoginRequest request = new AdminLoginRequest("forbidden@email.com", "test_password");

        when(memberRepository.findMemberByUserEmail(request.getEmail())).thenReturn(Optional.of(testMember));

        CustomException exception = assertThrows(CustomException.class, () -> memberService.loginAdmin(request));
        assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
        assertEquals("접근이 제한되었습니다.", exception.getInfo());
    }

    @Test
    public void adminLogin_InvalidUserInfo() {
        AdminLoginRequest request = new AdminLoginRequest("invalid@email.com", "test_password");

        when(memberRepository.findMemberByUserEmail(request.getEmail())).thenReturn(Optional.empty());

        CustomException exception = assertThrows(CustomException.class, () -> memberService.loginAdmin(request));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getHttpStatus());
        assertEquals("잘못된 정보입니다.", exception.getInfo());
    }

    @Test
    public void adminLogin_WrongPassword() {
        testMember = new Member(1L, "test@email.com", "test", "testPassword", AccountType.ADMIN, false, true, true, "testSnsId", LocalDateTime.now());
        AdminLoginRequest request = new AdminLoginRequest(testMember.getUserEmail(), "wrong_password");

        when(memberRepository.findMemberByUserEmail(request.getEmail())).thenReturn(Optional.of(testMember));
        when(passwordEncoder.matches(request.getPassword(), testMember.getPassword())).thenReturn(false);

        CustomException exception = assertThrows(CustomException.class, () -> memberService.loginAdmin(request));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getHttpStatus());
        assertEquals("잘못된 정보입니다.", exception.getInfo());
    }

    @Test
    public void adminLogin_InvalidUserType() {
        Member invalidMember = new Member(1L, "test@email.com", "test", "testPassword", AccountType.ADMIN, false, false, false, "testSnsId", LocalDateTime.now());
        AdminLoginRequest request = new AdminLoginRequest(invalidMember.getUserEmail(), "test_password");

        when(memberRepository.findMemberByUserEmail(request.getEmail())).thenReturn(Optional.of(invalidMember));
        when(passwordEncoder.matches(request.getPassword(), invalidMember.getPassword())).thenReturn(true);

        CustomException exception = assertThrows(CustomException.class, () -> memberService.loginAdmin(request));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getHttpStatus());
        assertEquals("잘못된 정보입니다.", exception.getInfo());
    }

}