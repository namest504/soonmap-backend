package soonmap.entity;

import javax.persistence.*;

@Entity
public class Floor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String description;
    private String dir;
    @ManyToOne
    @JoinColumn(name = "building_id")
    private Building building;
}
