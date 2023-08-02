package soonmap.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import soonmap.dto.BuildingInfoDto.BuildingResponseDto;
import soonmap.dto.BuildingInfoDto.FloorResponseDto;
import soonmap.entity.Building;
import soonmap.entity.Floor;
import soonmap.repository.BuildingRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BuildingInfoServiceTest {

    @Mock
    private BuildingRepository buildingRepository;

    @InjectMocks
    private BuildingInfoService buildingInfoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testGetFloorListByBuildingId() {
        // Given
        Building building = new Building();
        building.setId(1L);
        building.setName("의료과학관");
        building.setFloors(3);

        List<Floor> floors = new ArrayList<>();
        Floor floor1 = new Floor();
        floor1.setId(101L);
        floor1.setDescription("1층");
        floors.add(floor1);
        Floor floor2 = new Floor();
        floor2.setId(102L);
        floor2.setDescription("2층");
        floors.add(floor2);
        Floor floor3 = new Floor();
        floor3.setId(103L);
        floor3.setDescription("3층");
        floors.add(floor3);
        building.setFloor_info(floors);

        when(buildingRepository.findById(1L)).thenReturn(Optional.of(building));

        // When
        List<FloorResponseDto> floorList = buildingInfoService.getFloorListByBuildingId(1L);

        // Then
        assertEquals(3, floorList.size());
        assertEquals(floor1.getDescription(), floorList.get(0).getDescription());
        assertEquals(floor2.getDescription(), floorList.get(1).getDescription());
        assertEquals(floor3.getDescription(), floorList.get(2).getDescription());
    }

    @Test
    void GetAllBuildingList() {
        // Given
        List<Building> buildings = new ArrayList<>();
        Building building1 = new Building();
        building1.setId(1L);
        building1.setName("의료과학관");
        building1.setFloors(5);
        buildings.add(building1);

        Building building2 = new Building();
        building2.setId(2L);
        building2.setName("멀티미디어관");
        building2.setFloors(10);
        buildings.add(building2);

        when(buildingRepository.findAll()).thenReturn(buildings);

        // When
        List<BuildingResponseDto> buildingList = buildingInfoService.getAllBuildingList();

        // Then
        assertEquals(2, buildingList.size());
        assertEquals(building1.getName(), buildingList.get(0).getName());
        assertEquals(building1.getFloors(), buildingList.get(0).getFloors());
        assertEquals(building2.getName(), buildingList.get(1).getName());
        assertEquals(building2.getFloors(), buildingList.get(1).getFloors());
    }

    @Test
    void GetBuildingByName() {
        // Given
        String buildingName = "미디어랩스";
        List<Building> buildings = new ArrayList<>();
        Building building1 = new Building();
        building1.setId(1L);
        building1.setName(buildingName);
        building1.setFloors(3);
        buildings.add(building1);

        Building building2 = new Building();
        building2.setId(2L);
        building2.setName(buildingName);
        building2.setFloors(5);
        buildings.add(building2);

        when(buildingRepository.findBuildingsByName(buildingName)).thenReturn(buildings);

        // When
        List<BuildingResponseDto> buildingList = buildingInfoService.getBuildingByName(buildingName);

        // Then
        assertEquals(2, buildingList.size());
        assertEquals(building1.getName(), buildingList.get(0).getName());
        assertEquals(building1.getFloors(), buildingList.get(0).getFloors());
        assertEquals(building2.getName(), buildingList.get(1).getName());
        assertEquals(building2.getFloors(), buildingList.get(1).getFloors());
    }

        @Test
        void testChangeBuildingListToJson() {
            // Given
            List<BuildingResponseDto> buildingResponseDtos = new ArrayList<>();
            BuildingResponseDto buildingDto1 = new BuildingResponseDto("의료과학관", 5, "의료대입니다.");
            BuildingResponseDto buildingDto2 = new BuildingResponseDto("멀티미디어관", 10, "멀티미디어관입니다.");
            buildingResponseDtos.add(buildingDto1);
            buildingResponseDtos.add(buildingDto2);

            // When
            BuildingInfoService buildingInfoService = new BuildingInfoService(null);
            String json = buildingInfoService.ChangeBuildingListToJson(buildingResponseDtos);

            // Then
            String expectedJson = "[{\"name\":\"의료과학관\",\"floors\":5,\"description\":\"의료대입니다.\"},{\"name\":\"멀티미디어관\",\"floors\":10,\"description\":\"멀티미디어관입니다.\"}]";
            assertEquals(expectedJson, json);
        }

        @Test
        void testChangeFloorListToJson() {
            // Given
            List<FloorResponseDto> floorResponseDtos = new ArrayList<>();
            FloorResponseDto floorDto1 = new FloorResponseDto("1층", "qwer1234");
            FloorResponseDto floorDto2 = new FloorResponseDto("2층", "1234qwer");
            floorResponseDtos.add(floorDto1);
            floorResponseDtos.add(floorDto2);

            // When
            BuildingInfoService buildingInfoService = new BuildingInfoService(null);
            String json = buildingInfoService.ChangeFloorListToJson(floorResponseDtos);

            // Then
            String expectedJson = "[{\"description\":\"1층\",\"dir\":\"qwer1234\"},{\"description\":\"2층\",\"dir\":\"1234qwer\"}]";
            assertEquals(expectedJson, json);
        }


}
