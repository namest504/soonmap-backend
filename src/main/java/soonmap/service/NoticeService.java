package soonmap.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import soonmap.entity.Notice;
import soonmap.exception.CustomException;
import soonmap.repository.NoticeRepository;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;

    public Notice save(Notice notice) {
        return noticeRepository.save(notice);
    }

    public Notice findById(Long id) {
        return noticeRepository.findById(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "존재하지 않는 공지사항"));
    }

    public Page<Notice> findAll(int page, int length) {
        PageRequest pageRequest = PageRequest.of(page, length);
        return noticeRepository.findAll(pageRequest);
    }

    public Long deleteById(Long id) {
        noticeRepository.deleteById(id);

        // todo: 공지사항 삭제 시 관련 이미지 S3 삭제 로직 필요한가?

        return id;
    }
}
