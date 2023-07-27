package soonmap.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

public class NoticeDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateNoticeRequest {
        private String title;
        private String content;
        private LocalDateTime createAt;
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
