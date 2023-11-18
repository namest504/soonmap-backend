package soonmap.entity;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Restaurant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String restaurantName;
    @Column(nullable = false)
    private String phoneNumber;
    @Column(nullable = false)
    private double latitude;
    @Column(nullable = false)
    private double longtitude;
    @OneToMany(mappedBy = "restaurant", fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
    List<Menu> menuList;
}
