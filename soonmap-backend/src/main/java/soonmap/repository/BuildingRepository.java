package soonmap.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import soonmap.entity.Building;

import java.util.List;
import java.util.Optional;

@Repository
public interface BuildingRepository extends JpaRepository<Building, Long> {
    @Query("select b from Building b where b.name like :name")
    List<Building> findBuildingByName(@Param("name") String name);
    Optional<Building> findByUniqueNumber(String uniqueNumber);
    Page<Building> findAll(Pageable pageable);
}
