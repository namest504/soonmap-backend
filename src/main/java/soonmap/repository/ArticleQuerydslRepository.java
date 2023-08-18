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
import java.util.ArrayList;
import java.util.List;

import static soonmap.entity.QArticle.article;

@Repository
@RequiredArgsConstructor
public class ArticleQuerydslRepository {

    private final JPAQueryFactory queryFactory;

    public Page<Article> findArticlesByCondition(String typeName, LocalDateTime startDate, LocalDateTime endDate, String title, Pageable pageable) {

        List<Article> articleList = queryFactory
                .selectFrom(article)
                .where(typeNameContains(typeName),
                        dateBetween(startDate, endDate),
                        titleContains(title))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long countArticle = queryFactory.select(article.count())
                .from(article)
                .where(typeNameContains(typeName),
                        dateBetween(startDate, endDate),
                        titleContains(title))
                .fetchOne();

        return new PageImpl<>(articleList, pageable, countArticle);
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

    public List<Article> findMainArticles() {
        List<Article> result = new ArrayList<>();

        Article recentArticle = queryFactory.selectFrom(article)
                .where(article.createAt.before(LocalDateTime.now().minusDays(1)))
                .fetchFirst();

        if (recentArticle != null) {
            result.add(recentArticle);
            List<Article> articleList = queryFactory.selectFrom(article)
                    .where(article.ne(recentArticle))
                    .offset(0)
                    .limit(5)
                    .fetch();
            result.addAll(articleList);
        } else {
            List<Article> articleList = queryFactory.selectFrom(article)
                    .offset(0)
                    .limit(6)
                    .fetch();
            result.addAll(articleList);
        }

        return result;
    }

    public Page<Article> findAllByMemberId(Long memberId, Pageable pageable) {
        List<Article> articleList = queryFactory
                .selectFrom(article)
                .where(article.member.id.eq(memberId))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(article.createAt.desc())
                .fetch();

        Long countArticle = queryFactory.select(article.count())
                .from(article)
                .where(article.member.id.eq(memberId))
                .fetchOne();

        return new PageImpl<>(articleList, pageable, countArticle);
    }
}
