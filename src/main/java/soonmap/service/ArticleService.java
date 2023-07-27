package soonmap.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import soonmap.entity.Article;
import soonmap.repository.ArticleRepository;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;

    public Article save(Article article) {
        return articleRepository.save(article);
    }
}
