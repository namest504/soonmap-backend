package soonmap.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import soonmap.entity.AccountType;
import soonmap.entity.Member;

import java.util.List;

import static soonmap.entity.QMember.member;

@Repository
@RequiredArgsConstructor
public class MemberQuerydslRepository {

    private final JPAQueryFactory queryFactory;

    public Page<Member> findAdminAccount(Pageable pageable){
        List<Member> memberList = queryFactory.selectFrom(member)
                .where(member.accountType.eq(AccountType.ADMIN))
                .offset(pageable.getOffset())
                .offset(pageable.getPageSize())
                .orderBy(member.id.asc())
                .fetch();

        Long memberCount = queryFactory.select(member.count())
                .from(member)
                .where(member.accountType.eq(AccountType.ADMIN))
                .orderBy(member.id.asc())
                .fetchOne();

        if (memberCount == null) {
            memberCount = 0L;
        }

        return new PageImpl<>(memberList, pageable, memberCount);
    }

    public Page<Member> findUserAccount(Pageable pageable){
        List<Member> memberList = queryFactory.selectFrom(member)
                .where(member.accountType.eq(AccountType.BASIC))
                .offset(pageable.getOffset())
                .offset(pageable.getPageSize())
                .orderBy(member.id.asc())
                .fetch();

        Long memberCount = queryFactory.select(member.count())
                .from(member)
                .where(member.accountType.eq(AccountType.BASIC))
                .orderBy(member.id.asc())
                .fetchOne();

        if (memberCount == null) {
            memberCount = 0L;
        }

        return new PageImpl<>(memberList, pageable, memberCount);
    }
}
