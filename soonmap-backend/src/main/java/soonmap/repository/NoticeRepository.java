package soonmap.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import soonmap.entity.Notice;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {
    Page<Notice> findAll(Pageable pageable);

    Page<Notice> findNoticesByTitleContaining(Pageable pageable, String title);

    Page<Notice> findNoticesByCreateAtBetween(Pageable pageable, LocalDateTime startDate, LocalDateTime endDate);

    Page<Notice> findNoticesByCreateAtBetweenAndTitleContaining(Pageable pageable, LocalDateTime startDate, LocalDateTime endDate, String title);
}
