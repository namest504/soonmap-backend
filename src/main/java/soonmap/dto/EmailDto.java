package soonmap.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class EmailDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FindIdEmailRequest {
        @Email
        private String receiver;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConfirmFindIdEmailRequest {
        @Size(min = 6, max = 6)
        private String code;
        @Email
        private String receiver;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConfirmFindPwEmailRequest {
        @Size(min = 6, max = 6)
        private String code;
        @Email
        private String receiver;
        @NotBlank
        private String pw;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConfirmFindIdEmailResponse {
        private String id;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConfirmFindPwEmailResponse {
        private String id;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FindPwEmailRequest {
        @Email
        private String receiver;
        private String id;
    }



    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmailMessage {
        private String to;
        private String subject;
        private String message;
    }
}
