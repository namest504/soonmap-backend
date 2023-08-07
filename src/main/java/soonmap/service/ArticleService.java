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
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;

    public Article save(Article article) {
        return articleRepository.save(article);
    }

    public Optional<Article> findOneById(Long id) {
        return articleRepository.findById(id);
    }

    public Page<Article> findAllPage(int page, int length) {
        PageRequest pageRequest = PageRequest.of(page, length, Sort.Direction.DESC, "createAt");
        return articleRepository.findAll(pageRequest);
    }

    public Page<Article> findAllByMember(Member member, int page, int length) {
        PageRequest pageRequest = PageRequest.of(page, length, Sort.Direction.DESC, "createAt");
        return articleRepository.findAllByMember(member, pageRequest);
    }

    public Long deleteById(Long id) {
        articleRepository.deleteById(id);
        return id;
    }

    public List<Article> findAllByArticleTypeId(Long id) {
        return articleRepository.findArticlesByArticleType_Id(id);
    }
}
