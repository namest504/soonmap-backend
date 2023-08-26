package soonmap.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import soonmap.entity.ArticleType;
import soonmap.service.ArticleTypeService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ArticleTypeController {

    private final ArticleTypeService articleTypeService;

    @GetMapping("/article/type")
    public ResponseEntity<?> getAllArticleType(){
        List<ArticleType> all = articleTypeService.findAll();
        return ResponseEntity.ok()
                .body(all);
    }
}
