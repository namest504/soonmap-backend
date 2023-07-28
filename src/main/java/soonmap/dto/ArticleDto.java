package soonmap.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import soonmap.entity.Article;
import soonmap.entity.ArticleType;
import soonmap.entity.Member;

import java.time.LocalDateTime;

public class ArticleDto {

    @Getter
    @Setter
    @AllArgsConstructor
    public static class ArticleResponse {
        private Long id;
        private String title;
        private String content;
        private LocalDateTime createAt;
        private Member member;
        private ArticleType articleType;
        private int view;
        private boolean isExistImage;

        public static ArticleResponse of(Article article) {
            return new ArticleResponse(article.getId(), article.getTitle(), article.getContent(), article.getCreateAt(), article.getMember(), article.getArticleType(), article.getView(), article.isExistImage());
        }
    }

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
