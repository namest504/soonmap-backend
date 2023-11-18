package soonmap.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import soonmap.entity.Menu;

public interface MenuRepository extends JpaRepository<Menu, Long> {

}