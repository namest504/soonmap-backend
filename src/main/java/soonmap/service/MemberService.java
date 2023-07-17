package soonmap.service;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import soonmap.dto.MemberDto.NaverMemberResponse;
import soonmap.dto.TokenDto;
import soonmap.entity.Member;
import soonmap.repository.MemberRepository;


import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {
    private final MemberRepository memberRepository;

    public Member saveUser(NaverMemberResponse naverMemberResponse) {
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

    public Optional<Member> findUserById(String id) {
        return memberRepository.findMemberById(id);
    }

    public Member findUserByEmail(String email) {
        return memberRepository.findMemberByUserEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
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
