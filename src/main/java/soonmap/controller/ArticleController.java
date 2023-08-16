package soonmap.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import soonmap.dto.ArticleDto;
import soonmap.dto.ArticleDto.ArticlePageResponse;
import soonmap.dto.ArticleDto.ArticleResponse;
import soonmap.dto.ArticleDto.ArticleResponseWithOutContent;
import soonmap.entity.Article;
import soonmap.service.ArticleService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping("/article/main")
    public ResponseEntity<?> getMainArticle() {
        List<Article> mainArticlesPaging = articleService.findMainArticles();
        List<ArticleResponseWithOutContent> articleResponseList = mainArticlesPaging.stream()
                .map(ArticleResponseWithOutContent::of)
                .collect(Collectors.toList());

        return ResponseEntity.ok()
                .body(articleResponseList);

    }
}
