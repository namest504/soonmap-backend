package soonmap.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import soonmap.dto.RestaurantDto;
import soonmap.entity.Menu;
import soonmap.entity.Restaurant;
import soonmap.repository.RestaurantRespository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import static soonmap.dto.RestaurantDto.*;

@Service
@RequiredArgsConstructor
public class RestaurantService {
    private final RestaurantRespository restaurantRespository;

    public List<MenuResponseDto> findMenuByRestrauntId(Long id) {
        Restaurant restaurant = restaurantRespository.findRestaurantById(id).orElseThrow(() -> new NoSuchElementException());

            List<Menu> menuList = restaurant.getMenuList();
            List<MenuResponseDto> menuResponseDtoList = menuList.stream().map(MenuResponseDto::new).collect(Collectors.toList());
            return menuResponseDtoList;

    }

    public Optional<Restaurant> findRestaurantById(Long id) {
        Optional<Restaurant> restaurant = restaurantRespository.findRestaurantById(id);
        return restaurant;
    }

    public RestaurantResponseDto findRestaurantByName(String name) {
        Restaurant restaurant = restaurantRespository.findRestaurantByRestaurantName(name).orElseThrow(() -> new NoSuchElementException());
            RestaurantResponseDto restaurantResponseDto = new RestaurantResponseDto(restaurant);
            return restaurantResponseDto;
    }

    public List<RestaurantResponseDto> getRestaurantList() {
        List<Restaurant> restaurantList = restaurantRespository.findAll();
        List<RestaurantResponseDto> restaurantResponseDtos = restaurantList.stream().map(RestaurantResponseDto::new).collect(Collectors.toList());
        return restaurantResponseDtos;
    }

    @Transactional
    public void saveRestaurant(Restaurant restaurant) {
        restaurantRespository.save(restaurant);
    }
}
