package soonmap.dto;


import lombok.*;
import soonmap.entity.AccountType;
import soonmap.entity.Member;

import java.time.LocalDateTime;
import java.util.List;


public class MemberDto {

    @Getter
    @Setter
    @AllArgsConstructor
    public static class AdminResisterResponse {
        private boolean isAdmin;
        private boolean isWriter;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminResisterRequest {
        private String name;
        private String email;
        private String password;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class AdminLoginResponse {
        private boolean success;
        private boolean isAdmin;
        private boolean isWriter;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminLoginRequest {
        private String email;
        private String password;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class AccountListResponse {
        private int accountCount;
        private List<Account> memberList;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Account {
        private Long id;
        private String name;
        private String email;
        private boolean isAdmin;
        private boolean isWriter;
        private boolean isBan;
        private LocalDateTime createAt;


        public static Account of(Member member) {
            return new Account(member.getId(), member.getUsername(), member.getUserEmail(), member.isAdmin(), member.isWriter(), member.isBan(), member.getUserCreateAt());
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class NaverMemberResponse {
        private String userName;
        private String userEmail;
        private AccountType accountType;
        private String snsId; // naver 사용자마다 발급받는 개인식별 코드 ex) 주민등록번호
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class KakaoMemberResponse {
        private String userName;
        private String userEmail;
        private AccountType accountType;
        private String snsId; // naver 사용자마다 발급받는 개인식별 코드 ex) 주민등록번호
    }
}

