package soonmap.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

public class ArticleDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateArticleRequest {
        private String title;
        private String content;
        private LocalDateTime createAt;
        private String articleType;
        private boolean isExistImage;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class CreateArticleResponse {
        private boolean success;
        private Long id;
        private String title;
    }

}
