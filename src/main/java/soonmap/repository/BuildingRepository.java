package soonmap.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import soonmap.entity.Building;
import soonmap.entity.Floor;

import java.util.List;

public interface BuildingRepository extends JpaRepository<Building, Long> {
    List<Building> findBuildingsByName(String name);
}
