package soonmap.dto;

import lombok.*;
import soonmap.entity.Building;
import soonmap.entity.Floor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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
        private Long id;
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
            return new BuildingResponseDto(building.getId(), building.getName(), building.getFloors(), building.getDescription(), building.getLatitude(), building.getLongitude(), building.getUniqueNumber());
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BuildingRequest {
        @NotBlank
        private String name;
        @NotNull
        private int floors;
        @NotBlank
        private String description;
        @NotNull
        private double latitude; // 위도
        @NotNull
        private double longitude; // 경도
        @NotBlank
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
        @NotBlank
        private String description;
        @NotNull
        private int floorValue;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class FloorResponse {
        private Long id;
        private String description;
        private String dir;
        private int floorValue;
        private String uniqueNumber;

        public static FloorResponse of(Floor floor) {
            return new FloorResponse(floor.getId(), floor.getDescription(), floor.getDir(), floor.getFloorValue(), floor.getBuilding().getUniqueNumber());
        }
    }
}
