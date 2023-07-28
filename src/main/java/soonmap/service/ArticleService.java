package soonmap.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import soonmap.entity.Article;
import soonmap.entity.Member;
import soonmap.repository.ArticleRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;

    public Article save(Article article) {
        return articleRepository.save(article);
    }

    public List<Article> getArticles(int page, int length) {
        PageRequest pageRequest = PageRequest.of(page, length, Sort.Direction.DESC, "createAt");
        Page<Article> pagingArticle = articleRepository.findAll(pageRequest);
        return pagingArticle.toList();
    }

    public List<Article> getMemberArticles(Member member, int page, int length) {
        PageRequest pageRequest = PageRequest.of(page, length, Sort.Direction.DESC, "createAt");
        Page<Article> pagingArticle = articleRepository.findAllByMember(member, pageRequest);
        return pagingArticle.toList();
    }
}
