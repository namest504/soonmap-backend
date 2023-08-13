package soonmap.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import soonmap.entity.Notice;

import java.util.List;

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
}
