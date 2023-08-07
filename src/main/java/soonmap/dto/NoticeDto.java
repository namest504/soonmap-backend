package soonmap.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import soonmap.entity.Notice;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

public class NoticeDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModifyNoticeRequest {
        @NotBlank
        private String title;
        @NotBlank
        private String content;
        @NotNull
        private boolean isTop;
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
        @NotBlank
        private String title;
        @NotBlank
        private String content;
        @NotNull
        private boolean isTop;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class CreateNoticeResponse {
        private boolean success;
        private Long id;
        private String title;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class NoticePageResponse {
        private int totalPage;
        private List<NoticeResponse> articleTypeList;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class NoticeResponse {
        private Long id;
        private String title;
        private String content;
        private String writer;
        private LocalDateTime createAt;
        private boolean isTop;
        private int view;

        public static NoticeResponse of(Notice notice) {
            return new NoticeResponse(notice.getId(), notice.getTitle(), notice.getContent(), notice.getMember().getUsername(),notice.getCreateAt(), notice.isTop(), notice.getView());
        }
    }
}
