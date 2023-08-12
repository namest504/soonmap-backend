package soonmap.controller;

import com.github.scribejava.core.model.Response;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import soonmap.entity.Menu;
import soonmap.entity.Restaurant;
import soonmap.exception.CustomException;
import soonmap.service.MenuService;
import soonmap.service.RestaurantService;
import soonmap.service.S3Service;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import static soonmap.dto.RestaurantDto.*;

@RestController
@RequiredArgsConstructor
public class RestaurantController {
    private final RestaurantService restaurantService;
    private final MenuService menuService;
    private final S3Service s3Service;

    @GetMapping("/restaurant/{Id}")
    public ResponseEntity<?> findMenuByRestaurantId(@PathVariable Long Id) {
        List<MenuResponseDto> menuResponseDtoList = restaurantService.findMenuByRestrauntId(Id);
        if (menuResponseDtoList.isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "메뉴가 존재하지 않습니다..");
        }
        return ResponseEntity.ok(menuResponseDtoList);
    }

    @GetMapping("/restaurant/info/{Name}")
    public ResponseEntity<?> findRestaurantByName(@PathVariable String Name) {
        try {
            RestaurantResponseDto restaurantResponseDto = restaurantService.findRestaurantByName(Name);
            return ResponseEntity.ok(restaurantResponseDto);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @GetMapping("/restaurant/list")
    public ResponseEntity<?> getRestaurantList() {
        List<RestaurantResponseDto> restaurantResponseDtos = restaurantService.getRestaurantList();
        if (restaurantResponseDtos.isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "레스토랑이 존재하지 않습니다.");
        }
        return ResponseEntity.ok(restaurantResponseDtos);
    }

    @PostMapping("/restaurant/save")
    public ResponseEntity<String> uploadRestaurant(@RequestBody RestaurantRequestDto restaurantRequestDto) {
        Restaurant restaurant = restaurantRequestDto.to_Entity();
        restaurantService.saveRestaurant(restaurant);
        return ResponseEntity.ok("restaurant 저장 성공");
    }

    @PostMapping("/restaurant/{id}")
    public ResponseEntity<?> uploadMenu(
            @PathVariable Long id,
            @RequestBody MenuRequestDto menuRequestDto,
            @RequestParam(value = "image") MultipartFile image
    )throws IOException {
        Restaurant restaurant = restaurantService.findRestaurantById(id)
                .orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST, "레스토랑이 존재하지 않습니다."));
        Menu menu = menuRequestDto.to_Entity();


        String uploadDir = "menuplan/" + menu.getMenuName();
        String uploadResult = s3Service.upload(image, uploadDir);

        menu.setRestaurant(restaurant);
        menu.setMenuImageURL(uploadDir);
        menuService.save(menu);
        return ResponseEntity.ok("menu 저장 성공");
    }
}

