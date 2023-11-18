package soonmap.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import soonmap.entity.Building;
import soonmap.entity.Floor;
import soonmap.exception.CustomException;
import soonmap.service.BuildingInfoService;

import java.util.List;
import java.util.NoSuchElementException;

import static soonmap.dto.BuildingInfoDto.*;

@RestController
@RequiredArgsConstructor
public class BuildingInfoController {

    @Value("${CLOUD_FRONT_URL}")
    private String CLOUD_FRONT_URL;

    private final BuildingInfoService buildingService;

    /*
     건물을 클릭했을때 건물에 있는 층별 상세정보를 받아 올 수 있는 api입니다.
     */
//    @GetMapping("/building/{Id}")
//    ResponseEntity<?> getFloorByBuildingId(@PathVariable Long Id) {
//        try {
//            List<FloorResponseDto> floors = buildingService.getFloorListByBuildingId(Id);
//            return ResponseEntity.ok()
//                    .body(floors);
//        } catch (NoSuchElementException e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                    .body("Building이 존재하지 않습니다.");
//        }
//    }

    @GetMapping("/building/{id}")
    public ResponseEntity<?> getTotalFloorByBuildingId(@PathVariable Long id) {
        Building building = buildingService.findOneById(id)
                .orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST, "존재하지 않은 건물입니다."));
        int totalFloorByBuilding = buildingService.getTotalFloorByBuilding(id);
        return ResponseEntity.ok()
                .body(new BuildingInfoResponse(building.getName(), building.getFloors(), building.getUnderground(),totalFloorByBuilding));
    }

    /*
    keyword가 빈칸일 때는 전체 건물 리스트를 반환하고, 키워드가 있을 때는 검색 기능을 이용해서 Building 객체를 반환하는 api입니다.
     */

    @GetMapping("/building")
    ResponseEntity<List<BuildingResponseDto>> getBuildingOrBuildingListByKeyword(@RequestParam(required = false) String keyword) {
        if (keyword.isEmpty()) {
            List<BuildingResponseDto> buildingResponseDtoList = buildingService.getAllBuildingList();
            if (buildingResponseDtoList.isEmpty()) {
                throw new CustomException(HttpStatus.BAD_REQUEST, "헌재 DB에 저장되어 있는 Building이 없습니다.");
            }
            return ResponseEntity.ok(buildingResponseDtoList);
        } else {
            List<BuildingResponseDto> buildingResponseDtoList = buildingService.getBuildingByName(keyword);
            if (buildingResponseDtoList.isEmpty()) {
                throw new CustomException(HttpStatus.NOT_FOUND, "해당 키워드에 맞는 건물이 없습니다.");
            }
            return ResponseEntity.ok(buildingResponseDtoList);
        }
    }

    @GetMapping("/floor")
    public ResponseEntity<?> getFloorPlan(@RequestParam("buildingId") Long buildingId,
                                          @RequestParam("floor") int floorValue) {
        Building building = buildingService.findOneById(buildingId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "존재하지 않는 건물입니다."));

        Floor floorByBuildingIdAndFloorValue = buildingService.findFloorByBuildingIdAndFloorValue(buildingId, floorValue);

        return ResponseEntity.ok()
                .body(CLOUD_FRONT_URL + floorByBuildingIdAndFloorValue.getDir());
    }
}
