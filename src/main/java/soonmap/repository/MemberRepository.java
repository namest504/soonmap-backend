package soonmap.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import soonmap.entity.Member;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findMemberByUserEmail(String email);

    Optional<Member> findMemberById(String id);

    Optional<Member> findBySnsId(String snsId);
}
