package soonmap.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import soonmap.entity.ArticleType;

import java.util.List;

public class ArticleTypeDto {

    @Getter
    @Setter
    @AllArgsConstructor
    public static class ArticleTypeResponse {
        private int totalPage;
        private List<ArticleType> articleTypeList;
    }
}
