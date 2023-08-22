package soonmap.entity;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Building {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int floors;

    @Column(nullable = false)
    private int underground;

    @OneToMany(mappedBy = "building", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<Floor> floorInfo;

    @Column(nullable = false)
    private String description; // 건물에 대한 간단한 설명을 저장하는 칼럼입니다.
    @Column(nullable = false)
    private double latitude; // 위도
    @Column(nullable = false)
    private double longitude; // 경도
    @Column(nullable = false)
    private String uniqueNumber; // 고유번호
}