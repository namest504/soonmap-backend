package soonmap.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import soonmap.entity.AccountType;
import soonmap.entity.Member;


@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Memberdto {
    private String userName;
    private String userEmail;
    private AccountType accountType;

    private String sns_id; // naver 사용자마다 발급받는 개인식별 코드 ex) 주민등록번호

    public Member to_Entity() {
        return Member.builder()
                .userName(userName)
                .userEmail(userEmail)
                .accountType(accountType)
//                .sns_id(sns_id)
                .build();
    }

}
