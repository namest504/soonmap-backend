package soonmap.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import soonmap.entity.Restaurant;

import java.util.List;
import java.util.Optional;

public interface RestaurantRespository extends JpaRepository<Restaurant, Long> {
    Optional<Restaurant> findRestaurantById(Long id);
    Optional<Restaurant> findRestaurantByRestaurantName(String restaurantName);
}