package soonmap.dto;

import lombok.*;
import soonmap.entity.Building;
import soonmap.entity.Floor;

import java.util.List;

public class BuildingInfoDto {
    @Data
    @AllArgsConstructor
    public static class FloorResponseDto { // floor의 정보를 반환할때 사용하는 dto입니다. 설명과 이미지 링크를 반환합니다.
        private String description;
        private String dir;

        public FloorResponseDto(Floor floor) {
            this.description = floor.getDescription();
            this.dir = floor.getDir();
        }
    }

    @Data
    @AllArgsConstructor
    public static class BuildingResponseDto { // building의 정보를 반환할때 사용하는 dto입니다. 이름과 층, 설명을 반환합니다.
        private String name;
        private int floors;
        private String description;
        private double latitude; // 위도
        private double longitude; // 경도
        private String uniqueNumber; // 고유번호

        public BuildingResponseDto(Building building) {
            this.name = building.getName();
            this.floors = building.getFloors();
            this.description = building.getDescription();
            this.latitude = building.getLatitude();
            this.longitude = building.getLongitude();
            this.uniqueNumber = building.getUniqueNumber();
        }

        public static BuildingResponseDto of(Building building) {
            return new BuildingResponseDto(building.getName(), building.getFloors(), building.getDescription(), building.getLatitude(), building.getLongitude(), building.getUniqueNumber());
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BuildingRequest {
        private String name;
        private int floors;
        private String description;
        private double latitude; // 위도
        private double longitude; // 경도
        private String uniqueNumber; // 고유번호
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class BuildingPageResponse {
        private int totalPage;
        private List<BuildingResponseDto> buildingResponseDtoList;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class FloorRequest {
        private String description;
        private int floorValue;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class FloorResponse {
        private String description;
        private String dir;
        private int floorValue;
        private String uniqueNumber;

        public static FloorResponse of(Floor floor) {
            return new FloorResponse(floor.getDescription(), floor.getDir(), floor.getFloorValue(), floor.getBuilding().getUniqueNumber());
        }
    }
}
