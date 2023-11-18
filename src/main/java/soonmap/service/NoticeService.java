package soonmap.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import soonmap.entity.Notice;
import soonmap.exception.CustomException;
import soonmap.repository.NoticeQueryDslRepository;
import soonmap.repository.NoticeRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final NoticeQueryDslRepository noticeQueryDslRepository;

    public Notice save(Notice notice) {
        return noticeRepository.save(notice);
    }

    public Notice findById(Long id) {
        return noticeRepository.findById(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "존재하지 않는 공지사항"));
    }

    public Page<Notice> findAll(Pageable pageable) {
//        PageRequest pageRequest = PageRequest.of(page, length, Sort.Direction.DESC, "id");
        return noticeRepository.findAll(pageable);
    }

    public Long deleteById(Long id) {
        noticeRepository.deleteById(id);

        // todo: 공지사항 삭제 시 관련 이미지 S3 삭제 로직 필요한가?

        return id;
    }

    public Page<Notice> findByTitle(Pageable pageable, String title) {
//        PageRequest pageRequest = PageRequest.of(page, length, Sort.Direction.DESC, "createAt");
        return noticeRepository.findNoticesByTitleContaining(pageable, title);
    }

    public Page<Notice> findByDate(Pageable pageable, LocalDateTime startDate, LocalDateTime endDate) {
//        PageRequest pageRequest = PageRequest.of(page, length, Sort.Direction.DESC, "createAt");
        return noticeRepository.findNoticesByCreateAtBetween(pageable, startDate, endDate);
    }

    public Page<Notice> findByDateAndTitle(Pageable pageable, LocalDateTime startDate, LocalDateTime endDate, String title) {
//        PageRequest pageRequest = PageRequest.of(page, length, Sort.Direction.DESC, "createAt");
        return noticeRepository.findNoticesByCreateAtBetweenAndTitleContaining(pageable, startDate, endDate, title);
    }

    public List<Notice> findAllByTop() {
        return noticeQueryDslRepository.findAllByTop();
    }

    public List<Notice> findMain() {
        return noticeQueryDslRepository.findMainNotice();
    }

    public Page<Notice> findNormal(Pageable pageable, String title) {
//        PageRequest pageRequest = PageRequest.of(page, length, Sort.Direction.DESC, "createAt");
        return noticeQueryDslRepository.findNormalNotice(pageable, title);
    }

    public Page<Notice> findAllByMember(Pageable pageable, Long memberId) {
//        PageRequest pageRequest = PageRequest.of(page, length, Sort.Direction.DESC, "createAt");
        return noticeQueryDslRepository.findAllByMemberId(memberId, pageable);
    }
}
