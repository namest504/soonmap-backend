package soonmap.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import soonmap.entity.Article;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

public class ArticleDto {

    @Getter
    @Setter
    @AllArgsConstructor
    public static class ArticlePageResponse {
        private int totalPage;
        private List<ArticleResponse> articleList;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class ArticleResponse {
        private Long id;
        private String title;
        private String content;
        private String thumbnail;
        private LocalDateTime createAt;
        private String writer;
        private String articleTypeName;
        private int view;

        public static ArticleResponse of(Article article) {
            return new ArticleResponse(
                    article.getId(),
                    article.getTitle(), article.getContent(),
                    article.getThumbnail(),
                    article.getCreateAt(),
                    article.getMember().getUsername(),
                    article.getArticleType().getTypeName(),
                    article.getView());
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class ArticlePageResponseWithOutContent {
        private int totalPage;
        private List<ArticleResponseWithOutContent> articleList;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class ArticleResponseWithOutContent {
        private Long id;
        private String title;
        private String thumbnail;
        private LocalDateTime createAt;
        private String writer;
        private String articleTypeName;
        private int view;

        public static ArticleResponseWithOutContent of(Article article) {
            return new ArticleResponseWithOutContent(
                    article.getId(),
                    article.getTitle(),
                    article.getThumbnail(),
                    article.getCreateAt(),
                    article.getMember().getUsername(),
                    article.getArticleType().getTypeName(),
                    article.getView());
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class ArticlePageResponseWithOutContent {
        private int totalPage;
        private List<ArticleResponseWithOutContent> articleList;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class ArticleResponseWithOutContent {
        private Long id;
        private String title;
        private LocalDateTime createAt;
        private String writer;
        private String articleTypeName;
        private int view;

        public static ArticleResponseWithOutContent of(Article article) {
            return new ArticleResponseWithOutContent(article.getId(), article.getTitle(), article.getCreateAt(), article.getMember().getUsername(), article.getArticleType().getTypeName(), article.getView());
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModifyArticleRequest {
        @NotBlank
        private String title;
        @NotBlank
        private String content;
        private String thumbnail;
        @NotBlank
        private String articleTypeName;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateArticleRequest {
        @NotBlank
        private String title;
        @NotBlank
        private String content;
        private String thumbnail;
        @NotBlank
        private String articleTypeName;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class CreateArticleResponse {
        private boolean success;
        private Long id;
        private String title;
        private String thumbnail;
    }

}
