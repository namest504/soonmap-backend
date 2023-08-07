package soonmap.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import soonmap.dto.ArticleDto.ArticleResponse;
import soonmap.entity.Article;
import soonmap.entity.ArticleType;

import java.util.List;

public class ArticleTypeDto {

    @Getter
    @Setter
    @AllArgsConstructor
    public static class ArticleTypePageResponse {
        private int totalPage;
        private List<ArticleTypeResponse> articleTypeResponseList;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class ArticleTypeRequest {
        private String typeName;
        private String description;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class ArticleTypeResponse {
        private String typeName;
        private String description;

        public static ArticleTypeResponse of(ArticleType articleType) {
            return new ArticleTypeResponse(articleType.getTypeName(), articleType.getDescription());
        }
    }
}
