package soonmap.entity;

import javax.persistence.*;
import java.util.List;

@Entity
public class Building {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int floors; // 건물이 몇 층까지 있는지 정보를 저장하는 칼럼입니다.

    @OneToMany(mappedBy = "building", cascade = CascadeType.REMOVE)
    private List<Floor> floor_info;

    @Column(nullable = false)
    private String description; // 건물에 대한 간단한 설명을 저장하는 칼럼입니다.
}