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
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static soonmap.entity.QArticle.article;

@Repository
@RequiredArgsConstructor
public class ArticleQuerydslRepository {

    private final JPAQueryFactory queryFactory;
    private final int MAIN_ARTICLE_COUNT = 6;

    public Page<Article> findArticlesByCondition(String typeName, LocalDateTime startDate, LocalDateTime endDate, String title, Pageable pageable) {

        List<Article> articleList = queryFactory
                .selectFrom(article)
                .where(typeNameContains(typeName),
                        dateBetween(startDate, endDate),
                        titleContains(title))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(article.createAt.desc())
                .fetch();

        Long countArticle = queryFactory.select(article.count())
                .from(article)
                .where(typeNameContains(typeName),
                        dateBetween(startDate, endDate),
                        titleContains(title))
                .fetchOne();

        if (countArticle == null) {
            countArticle = 0L;
        }

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

        List<Article> recentArticle = queryFactory.selectFrom(article)
                .where(dateBetween(LocalDateTime.now().minusDays(1), LocalDateTime.now(ZoneId.of("Asia/Seoul"))))
                .offset(0)
                .limit(2)
                .orderBy(article.view.asc())
                .fetch();

        if (recentArticle != null) {
            result.addAll(recentArticle);
            List<Article> articleList = queryFactory.selectFrom(article)
                    .where(article.notIn(recentArticle),
                            dateBetween(LocalDateTime.now().minusDays(7), LocalDateTime.now(ZoneId.of("Asia/Seoul"))))
                    .offset(0)
                    .limit(MAIN_ARTICLE_COUNT - recentArticle.size())
                    .orderBy(article.view.desc())
                    .fetch();
            result.addAll(articleList);
        } else {
            List<Article> articleList = queryFactory.selectFrom(article)
                    .where(dateBetween(LocalDateTime.now().minusDays(7), LocalDateTime.now(ZoneId.of("Asia/Seoul"))))
                    .offset(0)
                    .limit(MAIN_ARTICLE_COUNT)
                    .orderBy(article.view.desc())
                    .fetch();

            if (articleList.size() < MAIN_ARTICLE_COUNT) {
                articleList = queryFactory.selectFrom(article)
                        .offset(0)
                        .limit(MAIN_ARTICLE_COUNT)
                        .orderBy(article.view.desc())
                        .fetch();
            }
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

        if (countArticle == null) {
            countArticle = 0L;
        }

        return new PageImpl<>(articleList, pageable, countArticle);
    }
}
