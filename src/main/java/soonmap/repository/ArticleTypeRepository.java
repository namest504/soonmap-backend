package soonmap.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import soonmap.entity.ArticleType;

import java.util.Optional;

@Repository
public interface ArticleTypeRepository extends JpaRepository<ArticleType, Long> {

    Optional<ArticleType> findArticleTypeById(Long id);

    Page<ArticleType> findAll(Pageable pageable);

    Optional<ArticleType> findByTypeName(String typeName);
}
