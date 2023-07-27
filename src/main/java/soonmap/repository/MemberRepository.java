package soonmap.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import soonmap.entity.AccountType;
import soonmap.entity.Member;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findMemberByUserEmail(String email);

    Optional<Member> findMemberById(Long id);
//    Optional<Member> findMemberById(String id);

    Optional<Member> findMemberByUserId(String userId);
    List<Member> findAdminsByAccountType(AccountType accountType);

    Optional<Member> findBySnsId(String snsId);
}
