package soonmap.service;


import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import soonmap.dto.MemberDto.AdminLoginRequest;
import soonmap.dto.MemberDto.AdminResisterRequest;
import soonmap.dto.MemberDto.NaverMemberResponse;
import soonmap.dto.MemberDto.KakaoMemberResponse;
import soonmap.dto.TokenDto;
import soonmap.entity.AccountType;
import soonmap.entity.Member;
import soonmap.exception.CustomException;
import soonmap.repository.MemberRepository;


import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, String> redisTemplate;

    public void saveAdminRefreshToken(Long memberId, String refreshToken) {
        redisTemplate.opsForValue().set("RefreshToken-ADMIN-" + memberId, refreshToken, Duration.ofDays(7));
    }

    public String getAdminRefreshToken(Long memberId) {
        return redisTemplate.opsForValue().get("RefreshToken-ADMIN-" + memberId);
    }

    public List<Member> findAdminAccount() {
        List<Member> adminsByAccountTypeAdmin = memberRepository.findAdminsByAccountType(AccountType.ADMIN);
        return adminsByAccountTypeAdmin;
    }

    public Member addAdmin(AdminResisterRequest adminResisterRequest) {
        return memberRepository.save(Member.builder()
                .userName(adminResisterRequest.getName())
                .userEmail(adminResisterRequest.getEmail())
                .userPassword(passwordEncoder.encode(adminResisterRequest.getPassword()))
                .accountType(AccountType.valueOf("ADMIN"))
                .snsId(null)
                .isBan(true)
                .isAdmin(false)
                .isWriter(true)
                .userCreateAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .build());
    }

    public Member loginAdmin(AdminLoginRequest adminLoginRequest) {
        Member member = memberRepository.findMemberByUserEmail(adminLoginRequest.getEmail())
                .orElseThrow(() -> new CustomException(HttpStatus.UNAUTHORIZED, "잘못된 정보입니다."));

        if (member.isBan()) {
            throw new CustomException(HttpStatus.FORBIDDEN, "접근이 제한되었습니다.");
        }

        if (!passwordEncoder.matches(adminLoginRequest.getPassword(), member.getPassword())) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "잘못된 정보입니다.");
        }

        if (member.isAdmin() || member.isWriter()) {
            return member;
        } else {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "잘못된 정보입니다.");
        }
    }

    public Member editUser(Member member) {
        return memberRepository.save(Member.builder()
                .id(member.getId())
                .userName(member.getUsername())
                .userEmail(member.getUserEmail())
                .userPassword(member.getUserPassword())
                .accountType(member.getAccountType())
                .snsId(member.getSnsId())
                .isBan(member.isBan())
                .isAdmin(member.isAdmin())
                .isWriter(member.isWriter())
                .userCreateAt(member.getUserCreateAt())
                .build());
    }

    public Member saveUser_naver(NaverMemberResponse naverMemberResponse) {
        Member member = Member.builder()
                .userName(naverMemberResponse.getUserName())
                .userEmail(naverMemberResponse.getUserEmail())
                .accountType(naverMemberResponse.getAccountType())
                .snsId(naverMemberResponse.getSnsId())
                .isBan(false)
                .isAdmin(false)
                .isWriter(false)
                .userCreateAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .build());
    }

    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    public Member findUserById(Long id) {
        return memberRepository.findMemberById(id).orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "존재하지 않는 유저입니다."));
    }

    public Member saveUser_kakao(KakaoMemberResponse kakaoMemberResponse) {
        Member member = Member.builder()
                .userName(kakaoMemberResponse.getUserName())
                .userEmail(kakaoMemberResponse.getUserEmail())
                .accountType(kakaoMemberResponse.getAccountType())
                .snsId(kakaoMemberResponse.getSnsId()) // kakaoId는 Long으로 반환을 받아야돼서 toString 메소드를 이용해 string으로 변경하였습니다.
                .isBan(false)
                .isAdmin(false)
                .build();
        return memberRepository.save(member);
    }

    public Optional<Member> findUserById(String id) {
        return memberRepository.findMemberById(id);
    }

    public Optional<Member> findUserByEmail(String email) {
        return memberRepository.findMemberByUserEmail(email);

    }
    public Optional<Member> findUserBySnsId(String sns_id) {
        return memberRepository.findBySnsId(sns_id);
    }

    public ResponseCookie createHttpOnlyCookie(TokenDto tokenDto) {
        //HTTPONLY 쿠키에 RefreshToken 생성후 전달
        String refreshToken = tokenDto.getRefreshToken();
        String accessToken = tokenDto.getAccessToken();
        String cookieValue = refreshToken + "|" + accessToken;

        ResponseCookie responseCookie = ResponseCookie.from("tokens", cookieValue)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(3600000)
                .build();

        return responseCookie;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return memberRepository.findMemberByUserEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
    }
}
