package soonmap.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
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
        public BuildingResponseDto(Building building) {
            this.name = building.getName();
            this.floors = building.getFloors();
            this.description = building.getDescription();
        }
    }
}
