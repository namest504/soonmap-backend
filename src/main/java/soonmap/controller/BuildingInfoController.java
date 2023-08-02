package soonmap.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import soonmap.dto.BuildingInfoDto;
import soonmap.entity.Floor;
import soonmap.service.BuildingInfoService;

import javax.naming.Name;
import java.util.List;
import java.util.NoSuchElementException;

import static soonmap.dto.BuildingInfoDto.*;

@RestController
@RequiredArgsConstructor
public class BuildingInfoController {

    private final BuildingInfoService buildingService;

    /*
     건물을 클릭했을때 건물에 있는 층별 상세정보를 받아 올 수 있는 api입니다.
     */
    @GetMapping("/building/{Id}")
    ResponseEntity<String> getFloorByBuildingId(@PathVariable Long Id) {
        try {
            List<FloorResponseDto> floors = buildingService.getFloorListByBuildingId(Id);
            String jsonFloorList = buildingService.ChangeFloorListToJson(floors);
            return ResponseEntity.ok()
                    .body(jsonFloorList);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Building이 존재하지 않습니다.");
        }
    }

    /*
     건물의 전체 리스트를 반환하는 api입니다.
     */
    @GetMapping("/building/list")
    ResponseEntity<String> getAllBuildingList() {
        try {
            List<BuildingResponseDto> buildings = buildingService.getAllBuildingList();
            String jsonBuildingList = buildingService.ChangeBuildingListToJson(buildings);
            return ResponseEntity.ok()
                    .body(jsonBuildingList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("현재 DB에 저장되어 있는 빌딩이 없습니다.");
        }
    }

    /*
     건물을 이름으로 검색했을때 키워드에 해당되는 건물 리스트 반환하는 api입니다.
     */
    @GetMapping("/building/info/{name}")
    ResponseEntity<String> getBuildingByName(@PathVariable String name) {
        List<BuildingResponseDto> buildings = buildingService.getBuildingByName(name);
        if (buildings.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("해당 이름을 가진 Building이 없습니다.");
        }
        String jsonBuildingList = buildingService.ChangeBuildingListToJson(buildings);
        return ResponseEntity.ok()
                .body(jsonBuildingList);
    }
}
