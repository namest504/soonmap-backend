package soonmap.entity;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id")
    private Long id;

    @Column
    private String title;

    @Column
    private String noticeContent;

    @Column
    private LocalDateTime noticeCreateAt;

    @Column
    private boolean isTop;

    @Column
    private int view;

    @Column
    private boolean isExistImage;
}
