package soonmap.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import soonmap.entity.ArticleType;
import soonmap.exception.CustomException;
import soonmap.repository.ArticleTypeRepository;

@Service
@RequiredArgsConstructor
public class ArticleTypeService {

    private final ArticleTypeRepository articleTypeRepository;

    public ArticleType save(ArticleType articleType) {
        return articleTypeRepository.save(articleType);
    }

    public ArticleType findArticleType(String typeName) {
        return articleTypeRepository.findArticleTypeByTypeName(typeName)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "존재하지 않는 카테고리"));
    }
}
