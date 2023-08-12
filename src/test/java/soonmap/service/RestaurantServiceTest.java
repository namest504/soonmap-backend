package soonmap.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import soonmap.dto.RestaurantDto;
import soonmap.entity.Menu;
import soonmap.entity.Restaurant;
import soonmap.exception.CustomException;
import soonmap.repository.RestaurantRespository;
import soonmap.service.RestaurantService;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static soonmap.dto.RestaurantDto.*;

public class RestaurantServiceTest {

    @Mock
    private RestaurantRespository restaurantRepository;

    @InjectMocks
    private RestaurantService restaurantService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFindMenuByRestaurantId() {
        // Given
        Long restaurantId = 1L;
        List<Menu> menuList = new ArrayList<>();
        menuList.add(new Menu());
        Restaurant restaurant = new Restaurant();
        restaurant.setMenuList(menuList);
        given(restaurantRepository.findRestaurantById(restaurantId)).willReturn(Optional.of(restaurant));

        // When
        List<MenuResponseDto> menuResponseDtoList = restaurantService.findMenuByRestrauntId(restaurantId);

        // Then
        assertFalse(menuResponseDtoList.isEmpty());
    }

    @Test
    public void testFindRestaurantByName() {
        // Given
        String restaurantName = "SampleRestaurant";
        Restaurant restaurant = new Restaurant();
        given(restaurantRepository.findRestaurantByRestaurantName(restaurantName)).willReturn(Optional.of(restaurant));

        // When
        RestaurantResponseDto restaurantResponseDto = restaurantService.findRestaurantByName(restaurantName);

        // Then
        assertNotNull(restaurantResponseDto);
    }

    @Test
    public void testGetRestaurantList() {
        // Given
        List<Restaurant> restaurantList = new ArrayList<>();
        restaurantList.add(new Restaurant());
        given(restaurantRepository.findAll()).willReturn(restaurantList);

        // When
        List<RestaurantResponseDto> restaurantResponseDtos = restaurantService.getRestaurantList();

        // Then
        assertFalse(restaurantResponseDtos.isEmpty());
    }

}
