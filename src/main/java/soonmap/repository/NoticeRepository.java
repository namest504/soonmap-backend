package soonmap.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import soonmap.entity.Notice;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
}
