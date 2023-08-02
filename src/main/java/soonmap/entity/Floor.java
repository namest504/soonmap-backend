package soonmap.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Floor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String description; // 층별 설명
    private String dir; // S3 이미지 경로
    @ManyToOne
    @JoinColumn(name = "building_id")
    private Building building;
}
