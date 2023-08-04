package soonmap.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class BuildingInfoServiceTest {

    @Mock
    private BuildingRepository buildingRepository;

    @InjectMocks
    private BuildingInfoService buildingInfoService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        objectMapper = new ObjectMapper();
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
        building1.setLatitude(1.1234);
        building1.setLongitude(1.5678);
        building1.setUniqueNumber("H");
        buildings.add(building1);

        Building building2 = new Building();
        building2.setId(2L);
        building2.setName("멀티미디어관");
        building2.setFloors(10);
        building2.setLatitude(2.1234);
        building2.setLongitude(2.5678);
        building2.setUniqueNumber("9");
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
        assertEquals(building1.getLongitude(), buildingList.get(0).getLongitude());
        assertEquals(building1.getLatitude(), buildingList.get(0).getLatitude());
        assertEquals(building1.getUniqueNumber(), buildingList.get(0).getUniqueNumber());
        assertEquals(building2.getLongitude(), buildingList.get(1).getLongitude());
        assertEquals(building2.getLatitude(), buildingList.get(1).getLatitude());
        assertEquals(building2.getUniqueNumber(), buildingList.get(1).getUniqueNumber());
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
        building1.setLatitude(1.1234);
        building1.setLongitude(1.5678);
        building1.setUniqueNumber("H");
        buildings.add(building1);

        Building building2 = new Building();
        building2.setId(2L);
        building2.setName(buildingName);
        building2.setFloors(5);
        building2.setLatitude(2.1234);
        building2.setLongitude(2.5678);
        building2.setUniqueNumber("9");
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
        assertEquals(building1.getLongitude(), buildingList.get(0).getLongitude());
        assertEquals(building1.getLatitude(), buildingList.get(0).getLatitude());
        assertEquals(building1.getUniqueNumber(), buildingList.get(0).getUniqueNumber());
        assertEquals(building2.getLongitude(), buildingList.get(1).getLongitude());
        assertEquals(building2.getLatitude(), buildingList.get(1).getLatitude());
        assertEquals(building2.getUniqueNumber(), buildingList.get(1).getUniqueNumber());
    }
//    @Test
//    public void testGetAllBuildingListReturnsJson() throws JsonProcessingException {
//        List<BuildingResponseDto> buildingResponseDtoList = new ArrayList<>();
//        BuildingResponseDto building1 = new BuildingResponseDto("의료과학관", 6, "의료대 입니다.");
//        BuildingResponseDto building2 = new BuildingResponseDto("인문과학관", 6, "인문대 입니다.");
//        buildingResponseDtoList.add(building1);
//        buildingResponseDtoList.add(building2);
//
//        String jsonResponse = objectMapper.writeValueAsString(buildingResponseDtoList);
//        assertEquals(buildingResponseDtoList,jsonResponse);
//    }
}
