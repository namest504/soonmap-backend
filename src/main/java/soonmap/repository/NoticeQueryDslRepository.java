package soonmap.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.ast.Not;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import soonmap.entity.Article;
import soonmap.entity.Notice;

import java.util.List;

import static soonmap.entity.QArticle.article;
import static soonmap.entity.QNotice.notice;

@Repository
@RequiredArgsConstructor
public class NoticeQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    public List<Notice> findMainNotice() {

        List<Notice> topNotice = queryFactory.selectFrom(notice)
                .where(notice.isTop.isTrue())
                .orderBy(notice.createAt.desc())
                .offset(0)
                .limit(1)
                .fetch();

        List<Notice> notTopNotice;
        if (topNotice.isEmpty()) {
            notTopNotice = queryFactory.selectFrom(notice)
                    .where(notice.isTop.isFalse())
                    .orderBy(notice.createAt.desc())
                    .offset(0)
                    .limit(3)
                    .fetch();

        } else {
            notTopNotice = queryFactory.selectFrom(notice)
                    .where(notice.isTop.isFalse())
                    .orderBy(notice.createAt.desc())
                    .offset(0)
                    .limit(2)
                    .fetch();

        }
        topNotice.addAll(notTopNotice);

        return topNotice;
    }

    public List<Notice> findAllByTop() {
        return queryFactory.selectFrom(notice)
                .where(notice.isTop.isTrue())
                .orderBy(notice.createAt.desc())
                .fetch();
    }

    public Page<Notice> findNormalNotice(Pageable pageable, String title) {
        List<Notice> noticeList = queryFactory.selectFrom(notice)
                .where(notice.isTop.isFalse(), titleContains(title))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return new PageImpl<>(noticeList, pageable, noticeList.size());
    }

    private BooleanExpression titleContains(String title) {
        return title != null ? notice.title.contains(title) : null;
    }

    public Page<Notice> findAllByMemberId(Long memberId, Pageable pageable) {
        List<Notice> noticeList = queryFactory
                .selectFrom(notice)
                .where(notice.member.id.eq(memberId))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(notice.createAt.desc())
                .fetch();

        Long countNotice = queryFactory.select(notice.count())
                .from(notice)
                .where(notice.member.id.eq(memberId))
                .fetchOne();

        return new PageImpl<>(noticeList, pageable, countNotice);
    }
}
