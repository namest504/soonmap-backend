package soonmap.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import soonmap.dto.NoticeDto;
import soonmap.dto.NoticeDto.NoticePageResponse;
import soonmap.dto.NoticeDto.NoticeResponse;
import soonmap.entity.Notice;
import soonmap.service.NoticeService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping("/notice/main")
    public ResponseEntity<?> getMainNoticeTop3(
    ) {
        List<Notice> mainNotice = noticeService.findMain();

        List<NoticeResponse> noticeResponseList = mainNotice.stream()
                .map(NoticeResponse::of)
                .collect(Collectors.toList());

        return ResponseEntity.ok()
                .body(noticeResponseList);
    }

    @GetMapping("/notice/top")
    public ResponseEntity<?> getTopNotice() {
        List<Notice> all = noticeService.findAllByTop();

        List<NoticeResponse> result = all.stream()
                .map(NoticeResponse::of)
                .collect(Collectors.toList());

        return ResponseEntity.ok()
                .body(result);
    }

    @GetMapping("/notice")
    public ResponseEntity<?> getNoticePaging(
            @RequestParam int page,
            @RequestParam(required = false) String title
    ) {
        Page<Notice> normal = noticeService.findNormal(page, 5, title);
        List<NoticeResponse> result = normal.getContent()
                .stream()
                .map(NoticeResponse::of)
                .collect(Collectors.toList());

        return ResponseEntity.ok()
                .body(new NoticePageResponse(normal.getTotalPages(), result));
    }

    @GetMapping("/notice/{id}")
    public ResponseEntity<?> getNoticeDetail(
            @PathVariable Long id
    ) {
        Notice notice = noticeService.findById(id);
        notice.updateView();
        noticeService.save(notice);

        return ResponseEntity.ok()
                .body(NoticeResponse.of(notice));
    }
}