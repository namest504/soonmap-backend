package soonmap.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import soonmap.entity.Article;
import soonmap.repository.ArticleQuerydslRepository;
import soonmap.repository.ArticleRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final ArticleQuerydslRepository articleQuerydslRepository;

    public Article save(Article article) {
        return articleRepository.save(article);
    }

    public Optional<Article> findOneById(Long id) {
        return articleRepository.findById(id);
    }

    public Page<Article> findAllPage(Pageable pageable) {
//        PageRequest pageRequest = PageRequest.of(page, length, Sort.Direction.DESC, "createAt");
        return articleRepository.findAll(pageable);
    }

    public Page<Article> findAllByMember(Long memberId, Pageable pageable) {
//        PageRequest pageRequest = PageRequest.of(page, length, Sort.Direction.DESC, "createAt");
        return articleQuerydslRepository.findAllByMemberId(memberId, pageable);
    }

    public Long deleteById(Long id) {
        articleRepository.deleteById(id);
        return id;
    }

    public List<Article> findAllByArticleTypeId(Long id) {
        return articleRepository.findArticlesByArticleType_Id(id);
    }

//    public Page<Article> findArticlesByConditionWithPaging(int page, int length ,String typeName, LocalDateTime startDate, LocalDateTime endDate, String title) {
//        PageRequest pageRequest = PageRequest.of(page, length, Sort.Direction.DESC, "createAt");
//        return articleQuerydslRepository.findArticlesByCondition(typeName,startDate,endDate,title,pageRequest);
//    }

    public Page<Article> findArticlesByConditionWithPaging(Pageable pageable, String typeName, LocalDateTime startDate, LocalDateTime endDate, String title) {
        return articleQuerydslRepository.findArticlesByCondition(typeName,startDate,endDate,title, pageable);
    }

    public List<Article> findMainArticles() {
        return articleQuerydslRepository.findMainArticles();
    }
}
