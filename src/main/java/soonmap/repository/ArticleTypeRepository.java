package soonmap.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import soonmap.entity.ArticleType;

import java.util.Optional;

@Repository
public interface ArticleTypeRepository extends JpaRepository<ArticleType, Long> {

    Optional<ArticleType> findArticleTypeByTypeName(String typeName);
}
