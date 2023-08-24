package soonmap.service;


import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import soonmap.dto.MemberDto.LoginRequest;
import soonmap.dto.MemberDto.AdminResisterRequest;

import soonmap.entity.AccountType;
import soonmap.entity.Member;
import soonmap.exception.CustomException;
import soonmap.repository.MemberRepository;


import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static soonmap.dto.MemberDto.*;

@Service
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, String> redisTemplate;

    public void saveAdminRefreshToken(Long memberId, String refreshToken) {
        redisTemplate.opsForValue().set("RefreshToken-ADMIN-" + memberId, refreshToken, Duration.ofDays(7));
    }

    public void saveUserRefreshToken(Long memberId, String refreshToken) {
        redisTemplate.opsForValue().set("RefreshToken-User-" + memberId, refreshToken, Duration.ofDays(7));
    }

    public Boolean logoutAdminRefreshToken(Long memberId) {
        return redisTemplate.delete("RefreshToken-ADMIN-" + memberId);
    }

    public Boolean logoutUserRefreshToken(Long memberId) {
        return redisTemplate.delete("RefreshToken-User-" + memberId);
    }

    public String getAdminRefreshToken(Long memberId) {
        return redisTemplate.opsForValue().get("RefreshToken-ADMIN-" + memberId);
    }

    public String getUserRefreshToken(Long memberId) {
        return redisTemplate.opsForValue().get("RefreshToken-User-" + memberId);
    }

    public void saveFindIdConfirmAuthCode(String email, String code) {
        redisTemplate.opsForValue().set("FindIdCode-ADMIN-" + email, code, Duration.ofMinutes(3));
    }

    public String getFindIdConfirmAuthCode(String email) {
        return redisTemplate.opsForValue().get("FindIdCode-ADMIN-" + email);
    }

    public Boolean deleteFindIdConfirmAuthCode(String email) {
        return redisTemplate.delete("FindIdCode-ADMIN-" + email);
    }

    public void saveFindPwConfirmAuthCode(String email, String code) {
        redisTemplate.opsForValue().set("FindPwCode-ADMIN-" + email, code, Duration.ofMinutes(3));
    }

    public String getFindPwConfirmAuthCode(String email) {
        return redisTemplate.opsForValue().get("FindPwCode-ADMIN-" + email);
    }

    public Boolean deleteFindPwConfirmAuthCode(String email) {
        return redisTemplate.delete("FindPwCode-ADMIN-" + email);
    }

    public void saveJoinConfirmAuthCode(String email, String code) {
        redisTemplate.opsForValue().set("JoinCode-User-" + email, code, Duration.ofMinutes(10));
    }

    public String findJoinConfirmAuthCode(String email) {
        return redisTemplate.opsForValue().get("JoinCode-User-" + email);
    }

    public void validateDuplicatedId(String id) {
        memberRepository.findMemberByUserId(id)
                .ifPresent(p -> {
                    throw new CustomException(HttpStatus.BAD_REQUEST, "ID 문제");
                });
    }

    public List<Member> findAdminAccount() {
        List<Member> adminsByAccountTypeAdmin = memberRepository.findAdminsByAccountType(AccountType.ADMIN);
        return adminsByAccountTypeAdmin;
    }

    public Member addAdmin(AdminResisterRequest adminResisterRequest) {
        return memberRepository.save(Member.builder()
                .userName(adminResisterRequest.getName())
                .userId(adminResisterRequest.getUserId())
                .userEmail(adminResisterRequest.getEmail())
                .userPassword(passwordEncoder.encode(adminResisterRequest.getUserPw()))
                .accountType(AccountType.valueOf("ADMIN"))
                .snsId(null)
                .isBan(true)
                .isAdmin(false)
                .isManager(false)
                .isStaff(true)
                .userCreateAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .build());
    }

    public Member loginAdmin(LoginRequest loginRequest) {
        Member member = memberRepository.findMemberByUserId(loginRequest.getUserId())
                .orElseThrow(() -> new CustomException(HttpStatus.UNAUTHORIZED, "존재하지 않는 계정입니다."));

        if (member.isBan()) {
            throw new CustomException(HttpStatus.FORBIDDEN, "접근이 제한되었습니다.");
        }

        if (!passwordEncoder.matches(loginRequest.getUserPw(), member.getPassword())) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "존재하지 않는 계정입니다.");
        }

        if (member.isAdmin() || member.isManager() || member.isStaff()) {
            return member;
        } else {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "잘못된 정보입니다.");
        }
    }

    public Member editUser(Member member) {
        return memberRepository.save(Member.builder()
                .id(member.getId())
                .userId(member.getUserId())
                .userName(member.getUsername())
                .userEmail(member.getUserEmail())
                .userPassword(member.getUserPassword())
                .accountType(member.getAccountType())
                .snsId(member.getSnsId())
                .isBan(member.isBan())
                .isAdmin(member.isAdmin())
                .isManager(member.isManager())
                .isStaff(member.isStaff())
                .userCreateAt(member.getUserCreateAt())
                .build());
    }

    public Member SocialsaveUser(SocialMemberResponse socialMemberResponse) {
        Member member = Member.builder()
                .userName(socialMemberResponse.getUserName())
                .userEmail(socialMemberResponse.getUserEmail())
                .accountType(socialMemberResponse.getAccountType())
                .snsId(socialMemberResponse.getSnsId())
                .isBan(false)
                .isAdmin(false)
                .isManager(false)
                .isStaff(false)
                .userCreateAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .build();
        return memberRepository.save(member);
    }

    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    public Optional<Member> findUserById(Long id) {
        return memberRepository.findMemberById(id);
    }


//    public Optional<Member> findUserById(String id) {
//        return memberRepository.findMemberById(id);
//    }

    public Optional<Member> findUserByEmail(String email) {
        return memberRepository.findMemberByUserEmail(email);

    }

    public Optional<Member> findUserByUserId(String uid) {
        return memberRepository.findMemberByUserId(uid);
    }

    public Optional<Member> findUserByName(String name) {
        return memberRepository.findMemberByUserName(name);
    }

    public Optional<Member> findUserBySnsId(String sns_id) {
        return memberRepository.findBySnsId(sns_id);
    }

    public ResponseCookie createHttpOnlyCookie(String refreshToken) {
        //HTTPONLY 쿠키에 RefreshToken 생성후 전달

        ResponseCookie responseCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
//                .secure(true)
                .sameSite("Lax")
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

    public Member save(Member member) {
        return memberRepository.save(member);
    }

    public Member saveUser(Claims registerInfo, MemberJoinRequest memberJoinRequest) {
        return memberRepository.save(Member.builder()
                .userName(memberJoinRequest.getId())
                .userId(memberJoinRequest.getId())
                .userEmail(registerInfo.get("email", String.class))
                .userPassword(passwordEncoder.encode(memberJoinRequest.getPw()))
                .accountType(AccountType.valueOf("BASIC"))
                .snsId(null)
                .isBan(false)
                .isAdmin(false)
                .isManager(false)
                .isStaff(false)
                .userCreateAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .build());
    }

    public Member loginUser(LoginRequest loginRequest) {
        Member member = memberRepository.findMemberByUserId(loginRequest.getUserId())
                .orElseThrow(() -> new CustomException(HttpStatus.UNAUTHORIZED, "존재하지 않는 계정입니다."));

        if (member.isBan()) {
            throw new CustomException(HttpStatus.FORBIDDEN, "접근이 제한되었습니다.");
        }

        if (!passwordEncoder.matches(loginRequest.getUserPw(), member.getPassword())) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "존재하지 않는 계정입니다.");
        }

        if (!member.isAdmin() && !member.isManager() && !member.isStaff()) {
            return member;
        } else {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "잘못된 접근입니다.");
        }
    }

    public Boolean matchPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public Boolean withdrawUser(Long uid) {
        memberRepository.deleteById(uid);
        return logoutUserRefreshToken(uid);
    }
}
