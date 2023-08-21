package soonmap.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import soonmap.dto.ArticleDto;
import soonmap.dto.ArticleDto.ArticlePageResponse;
import soonmap.dto.ArticleDto.ArticlePageResponseWithOutContent;
import soonmap.dto.ArticleDto.ArticleResponse;
import soonmap.dto.ArticleDto.ArticleResponseWithOutContent;
import soonmap.entity.Article;
import soonmap.entity.Member;
import soonmap.exception.CustomException;
import soonmap.security.jwt.MemberPrincipal;
import soonmap.service.ArticleService;
import soonmap.service.MemberService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;
    private final MemberService memberService;

    @GetMapping("/article/main")
    public ResponseEntity<?> getMainArticle() {
        List<Article> mainArticlesPaging = articleService.findMainArticles();
        List<ArticleResponseWithOutContent> articleResponseList = mainArticlesPaging.stream()
                .map(ArticleResponseWithOutContent::of)
                .collect(Collectors.toList());

        return ResponseEntity.ok()
                .body(articleResponseList);

    }
    @GetMapping("/article")
    public ResponseEntity<?> getArticleWithCondition(@PageableDefault(size = 5, sort = "createAt", direction = Sort.Direction.DESC) Pageable pageable,
                                                     @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime startDate,
                                                     @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime endDate,
                                                     @RequestParam(required = false) String title,
                                                     @RequestParam(required = false) String type) {

        Page<Article> articlesByConditionWithPaging = articleService.findArticlesByConditionWithPaging(pageable, type, startDate, endDate, title);
        List<ArticleResponseWithOutContent> articleResponseList = articlesByConditionWithPaging.getContent()
                .stream()
                .map(ArticleResponseWithOutContent::of)
                .collect(Collectors.toList());

        return ResponseEntity.ok()
                .body(new ArticlePageResponseWithOutContent(articlesByConditionWithPaging.getTotalPages(), articleResponseList));

    }

    @GetMapping("/article/{id}")
    public ResponseEntity<?> getDetailArticle(@AuthenticationPrincipal MemberPrincipal memberPrincipal,
                                              @PathVariable("id") Long articleId) {

        Member member = memberService.findUserById(memberPrincipal.getMember().getId())
                .orElseThrow(() -> new CustomException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."));
        Article article = articleService.findOneById(articleId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "존재하지 않는 게시글입니다."));
        article.updateView();
        articleService.save(article);
        return ResponseEntity.ok()
                .body(ArticleResponse.of(article));

    }
}
