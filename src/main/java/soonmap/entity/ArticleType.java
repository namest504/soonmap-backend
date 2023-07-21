package soonmap.entity;

import javax.persistence.*;

@Entity
public class ArticleType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "article_type_id")
    private Long id;

    @Column
    private String articleType;
}
