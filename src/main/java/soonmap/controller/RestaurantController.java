package soonmap.controller;

import com.github.scribejava.core.model.Response;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import soonmap.service.RestaurantService;

@RestController
@RequiredArgsConstructor
public class RestaurantController {
    private final RestaurantService restaurantService;
//    @GetMapping("/restaurant/{ID}")
//    public ResponseEntity<?> findRestaurantById(@PathVariable Long ID) {
//
//    }
}
