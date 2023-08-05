package soonmap.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import soonmap.entity.Article;
import soonmap.entity.ArticleType;
import soonmap.exception.CustomException;
import soonmap.repository.ArticleTypeRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ArticleTypeService {

    private final ArticleTypeRepository articleTypeRepository;

    public ArticleType save(ArticleType articleType) {
        return articleTypeRepository.save(articleType);
    }

    public ArticleType findOneById(Long id) {
        return articleTypeRepository.findArticleTypeById(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "존재하지 않는 카테고리"));
    }

    public List<ArticleType> findAll() {
        return articleTypeRepository.findAll();
    }

    public Page<ArticleType> findAll(int page, int length) {
        PageRequest pageRequest = PageRequest.of(page, length);
        return articleTypeRepository.findAll(pageRequest);
    }

    public Long deleteById(Long id) {
        articleTypeRepository.deleteById(id);
        return id;
    }

    public Optional<ArticleType> findByTypeName(String typeName) {
        return articleTypeRepository.findByTypeName(typeName);
    }
}
