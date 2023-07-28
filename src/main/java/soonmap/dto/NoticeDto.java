package soonmap.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class NoticeDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModifyNoticeRequest {
        private String title;
        private String content;
        private boolean isTop;
        private boolean isExistImage;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class ModifyNoticeResponse {
        private boolean success;
        private Long id;
        private String title;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateNoticeRequest {
        private String title;
        private String content;
        private boolean isTop;
        private boolean isExistImage;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class CreateNoticeResponse {
        private boolean success;
        private Long id;
        private String title;
    }
}
