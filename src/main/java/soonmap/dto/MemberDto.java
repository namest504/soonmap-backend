package soonmap.dto;


import lombok.*;
import soonmap.entity.AccountType;
import soonmap.entity.Member;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;


public class MemberDto {

    @Getter
    @Setter
    @AllArgsConstructor
    public static class AdminResisterResponse {
        private boolean success;
        private boolean isAdmin;
        private boolean isManager;
        private boolean isStaff;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminResisterRequest {
        @Email
        private String email;
        @NotBlank
        private String name;
        @NotBlank
        private String userId;
        @NotBlank
        private String userPw;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class AdminLoginResponse {
        private boolean success;
        private String name;
        private boolean isAdmin;
        private boolean isManager;
        private boolean isStaff;
        private String accessToken;
        private String refreshToken;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminLoginRequest {
        @NotBlank
        private String userId;
        @NotBlank
        private String userPw;
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
        private boolean isManager;
        private boolean isStaff;
        private boolean isBan;
        private LocalDateTime createAt;


        public static Account of(Member member) {
            return new Account(member.getId(), member.getUsername(), member.getUserEmail(), member.isAdmin(), member.isManager(), member.isStaff(), member.isBan(), member.getUserCreateAt());
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SocialMemberResponse {
        private String userName;
        private String userEmail;
        private AccountType accountType;
        private String snsId; // naver 사용자마다 발급받는 개인식별 코드 ex) 주민등록번호
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModifyPassWordMemberRequest {
        @NotBlank
        private String pw;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberEmailRequest {
        @NotBlank
        private String emailId;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberEmailConfirmRequest {
        @NotNull
        private String authCode;
        @NotBlank
        private String emailId;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberEmailConfirmResponse {
        private String registerToken;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberJoinRequest {
        @NotNull
        private String registerToken;
        @Email
        private String email;
        @NotBlank
        private String id;
        @NotBlank
        private String pw;
    }

}

