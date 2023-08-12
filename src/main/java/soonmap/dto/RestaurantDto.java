package soonmap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import soonmap.entity.Menu;
import soonmap.entity.Restaurant;

import javax.persistence.Column;

public class RestaurantDto {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RestaurantRequestDto {
        private String restaurantName;
        private double latitude;
        private double longtitude;
        private String phoneNumber;

        public Restaurant to_Entity() {
            return Restaurant.builder()
                    .restaurantName(restaurantName)
                    .latitude(latitude)
                    .longtitude(longtitude)
                    .phoneNumber(phoneNumber)
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
        private String phoneNumber;

        public RestaurantResponseDto(Restaurant restaurant) {
            this.id = restaurant.getId();
            this.restaurantName = restaurant.getRestaurantName();
            this.latitude = restaurant.getLatitude();
            this.longtitude = restaurant.getLongtitude();
            this.phoneNumber = restaurant.getPhoneNumber();
        }
    }
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MenuRequestDto {
        private String menuName;
        private String menuImageURL;
        private String menuDescription;
        private String menuPrice;



        public Menu to_Entity() {
            return Menu.builder()
                    .menuName(menuName)
                    .menuImageURL(menuImageURL)
                    .menuDescription(menuDescription)
                    .menuPrice(menuPrice)
                    .build();
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MenuResponseDto {
        private String menuName;
        private String menuImageURL;
        private String menuDescription;
        private String menuPrice;



        public MenuResponseDto(Menu menu) {
            this.menuName = menu.getMenuName();
            this.menuImageURL = menu.getMenuImageURL();
            this.menuDescription = menu.getMenuDescription();
            this.menuPrice = menu.getMenuPrice();
        }
    }
}
