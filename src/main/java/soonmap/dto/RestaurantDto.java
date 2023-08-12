package soonmap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import soonmap.entity.Menu;
import soonmap.entity.Restaurant;

public class RestaurantDto {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RestaurantRequestDto {
        private String restaurantName;
        private double latitude;
        private double longtitude;

        public Restaurant to_Entity() {
            return Restaurant.builder()
                    .restaurantName(restaurantName)
                    .latitude(latitude)
                    .longtitude(longtitude)
                    .build();
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RestaurantResponseDto {
        private Long id;
        private String restaurantName;
        private double latitude;
        private double longtitude;

        public RestaurantResponseDto(Restaurant restaurant) {
            this.id = restaurant.getId();
            this.restaurantName = restaurant.getRestaurantName();
            this.latitude = restaurant.getLatitude();
            this.longtitude = restaurant.getLongtitude();
        }
    }
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MenuRequestDto {
        private String menuName;
        private String menuImageURL;

        public Menu to_Entity() {
            return Menu.builder()
                    .menuName(menuName)
                    .menuImageURL(menuImageURL)
                    .build();
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MenuResponseDto {
        private String menuName;
        private String menuImageURL;

        public MenuResponseDto(Menu menu) {
            this.menuName = menu.getMenuName();
            this.menuImageURL = menu.getMenuImageURL();
        }
    }

}
