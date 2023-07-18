package soonmap.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import soonmap.entity.AccountType;



public class MemberDto {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class NaverMemberResponse {
        private String userName;
        private String userEmail;
        private AccountType accountType;
        private String snsId; // naver 사용자마다 발급받는 개인식별 코드 ex) 주민등록번호
    }
}

