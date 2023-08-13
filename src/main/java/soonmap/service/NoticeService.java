package soonmap.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

    public Page<Notice> findAll(int page, int length) {
        PageRequest pageRequest = PageRequest.of(page, length, Sort.Direction.DESC, "id");
        return noticeRepository.findAll(pageRequest);
    }

    public Long deleteById(Long id) {
        noticeRepository.deleteById(id);

        // todo: 공지사항 삭제 시 관련 이미지 S3 삭제 로직 필요한가?

        return id;
    }

    public Page<Notice> findByTitle(int page, int length, String title) {
        PageRequest pageRequest = PageRequest.of(page, length, Sort.Direction.DESC, "id");
        return noticeRepository.findNoticesByTitleContaining(pageRequest, title);
    }

    public Page<Notice> findByDate(int page, int length, LocalDateTime startDate, LocalDateTime endDate) {
        PageRequest pageRequest = PageRequest.of(page, length, Sort.Direction.DESC, "id");
        return noticeRepository.findNoticesByCreateAtBetween(pageRequest, startDate, endDate);
    }

    public Page<Notice> findByDateAndTitle(int page, int length, LocalDateTime startDate, LocalDateTime endDate, String title) {
        PageRequest pageRequest = PageRequest.of(page, length, Sort.Direction.DESC, "id");
        return noticeRepository.findNoticesByCreateAtBetweenAndTitleContaining(pageRequest, startDate, endDate, title);
    }

    public List<Notice> findAllByTop() {
        return noticeQueryDslRepository.findAllByTop();
    }

    public List<Notice> findMain() {
        return noticeQueryDslRepository.findMainNotice();
    }

    public Page<Notice> findNormal(int page, int length, String title) {
        PageRequest pageRequest = PageRequest.of(page, length, Sort.Direction.DESC, "createAt");
        return noticeQueryDslRepository.findNormalNotice(pageRequest, title);
    }
}
