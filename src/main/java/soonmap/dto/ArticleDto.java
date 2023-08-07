package soonmap.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import soonmap.entity.Article;

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
        private LocalDateTime createAt;
        private String writer;
        private String articleTypeName;
        private int view;

        public static ArticleResponse of(Article article) {
            return new ArticleResponse(article.getId(), article.getTitle(), article.getContent(), article.getCreateAt(), article.getMember().getUsername(), article.getArticleType().getTypeName(), article.getView());
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModifyArticleRequest {
        private String title;
        private String content;
        private String articleTypeName;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateArticleRequest {
        private String title;
        private String content;
        private String articleTypeName;
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
