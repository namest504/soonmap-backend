package soonmap.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Image {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long Id;

    private String imageName;

    @Enumerated(EnumType.STRING)
    private ImageType imageType;

    private String imageDir;

    private LocalDateTime createAt;
}
