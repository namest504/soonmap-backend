package soonmap.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import soonmap.entity.Article;

import java.time.LocalDateTime;
import java.util.List;

import static soonmap.entity.QArticle.article;

@Repository
@RequiredArgsConstructor
public class ArticleQuerydslRepository {

    private final JPAQueryFactory queryFactory;

    public Page<Article> findArticlesByCondition(String typeName, LocalDateTime startDate, LocalDateTime endDate, String title, Pageable pageable) {

        List<Article> articleList = queryFactory
                .selectFrom(article)
                .where(typeNameContains(typeName), dateBetween(startDate, endDate), titleContains(title))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return new PageImpl<>(articleList, pageable, articleList.size());
    }

    private BooleanExpression typeNameContains(String typeName) {
        return typeName != null ? article.articleType.typeName.contains(typeName) : null;
    }

    private BooleanExpression dateBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return (startDate != null && endDate != null) ? article.createAt.between(startDate, endDate) : null;
    }

    private BooleanExpression titleContains(String title) {
        return title != null ? article.title.contains(title) : null;
    }
}
