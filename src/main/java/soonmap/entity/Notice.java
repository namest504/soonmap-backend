package soonmap.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id")
    private Long id;

    @Column
    private String title;

    @Column
    private String content;

    @OneToOne(fetch = FetchType.LAZY)
    private Member member;

    @Column
    private LocalDateTime createAt;

    @Column
    private boolean isTop;

    @Column
    private int view;

    @Column
    private boolean isExistImage;
}
