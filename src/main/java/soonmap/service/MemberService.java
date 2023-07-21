package soonmap.service;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import soonmap.dto.MemberDto;
import soonmap.dto.MemberDto.NaverMemberResponse;
import soonmap.dto.MemberDto.KakaoMemberResponse;
import soonmap.dto.TokenDto;
import soonmap.entity.Member;
import soonmap.repository.MemberRepository;


import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {

    private final MemberRepository memberRepository;

    public Member saveUser_naver(NaverMemberResponse naverMemberResponse) {
        Member member = Member.builder()
                .userName(naverMemberResponse.getUserName())
                .userEmail(naverMemberResponse.getUserEmail())
                .accountType(naverMemberResponse.getAccountType())
                .snsId(naverMemberResponse.getSnsId())
                .isBan(false)
                .isAdmin(false)
                .build();
        return memberRepository.save(member);
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
