package soonmap.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Article {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "article_id")
    private Long id;

    @Column
    private String title;

    @Column
    private String articleContent;

    @Column
    private LocalDateTime articleCreateAt;

    @OneToOne(fetch = FetchType.LAZY)
    private Member member;

    @OneToOne(fetch = FetchType.LAZY)
    private ArticleType articleType;

    private int view;

    private boolean isExistImage;
}
