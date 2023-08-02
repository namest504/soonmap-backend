package soonmap.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import soonmap.dto.BuildingInfoDto;
import soonmap.entity.Building;
import soonmap.entity.Floor;
import soonmap.repository.BuildingRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static soonmap.dto.BuildingInfoDto.*;

@Service
@RequiredArgsConstructor
public class BuildingInfoService {
    private final BuildingRepository buildingRepository;

    public List<FloorResponseDto> getFloorListByBuildingId(Long Id) {
        Building building = buildingRepository.findById(Id).orElseThrow(NoSuchElementException::new);
        List<Floor> floors = building.getFloor_info();

        List<FloorResponseDto> floorDtos = floors.stream().map(FloorResponseDto::new).collect(Collectors.toList());
        return floorDtos;
    }

    public List<BuildingResponseDto> getAllBuildingList() {
        List<Building> buildings = buildingRepository.findAll();
        List<BuildingResponseDto> buildingResponseDtos = buildings.stream().map(BuildingResponseDto::new).collect(Collectors.toList());

        return buildingResponseDtos;
    }

    public List<BuildingResponseDto> getBuildingByName(String name) {
        List<Building> buildings = buildingRepository.findBuildingsByName(name);

            List<BuildingResponseDto> buildingResponseDtos = buildings.stream().map(BuildingResponseDto::new).collect(Collectors.toList());
            return buildingResponseDtos;
        }
}
