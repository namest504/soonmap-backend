package soonmap.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import soonmap.entity.Article;
import soonmap.entity.ArticleType;
import soonmap.entity.Member;

import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

    Page<Article> findAll(Pageable pageable);
//    Page<Article> findAllByMember(Member member, Pageable pageable);
    List<Article> findArticlesByArticleType_Id(Long articleType_id);
}
