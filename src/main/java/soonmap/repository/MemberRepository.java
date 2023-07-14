package soonmap.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import soonmap.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
