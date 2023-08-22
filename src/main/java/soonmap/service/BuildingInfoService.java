package soonmap.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import soonmap.entity.Building;
import soonmap.entity.Floor;
import soonmap.exception.CustomException;
import soonmap.repository.BuildingRepository;
import soonmap.repository.FloorQuerydslRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import static soonmap.dto.BuildingInfoDto.*;

@Service
@RequiredArgsConstructor
public class BuildingInfoService {
    private final BuildingRepository buildingRepository;
    private final FloorQuerydslRepository floorQuerydslRepository;

    public List<FloorResponseDto> getFloorListByBuildingId(Long Id) {
        Building building = buildingRepository.findById(Id).orElseThrow(NoSuchElementException::new);
        List<Floor> floors = building.getFloorInfo();

        List<FloorResponseDto> floorDtos = floors.stream().map(FloorResponseDto::new).collect(Collectors.toList());
        return floorDtos;
    }

    public int getTotalFloorByBuilding(Long buildingId) {
        Building building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "존재하지 않는 건물입니다,"));
        return building.getFloorInfo().size();
    }

    public List<BuildingResponseDto> getAllBuildingList() {
        List<Building> buildings = buildingRepository.findAll();
        List<BuildingResponseDto> buildingResponseDtos = buildings.stream().map(BuildingResponseDto::new).collect(Collectors.toList());

        return buildingResponseDtos;
    }

    public List<BuildingResponseDto> getBuildingByName(String name) {
        List<Building> building = buildingRepository.findBuildingByName("%"+name+"%");
        List<BuildingResponseDto> buildingResponseDtoList = building.stream()
                .map(BuildingResponseDto::new)
                .collect(Collectors.toList());
        return buildingResponseDtoList;
    }

    public Building save(Building building) {
        return buildingRepository.save(building);
    }

    public Long deleteById(Long id) {
        buildingRepository.deleteById(id);
        return id;
    }

    public Page<Building> findPageAll(int page, int length) {
        PageRequest pageRequest = PageRequest.of(page, length);
        return buildingRepository.findAll(pageRequest);
    }

    public Optional<Building> findOneById(Long buildingId) {
        return buildingRepository.findById(buildingId);
    }

    public Floor findFloorByBuildingIdAndFloorValue(Long buildingId, int floorValue) {
        return floorQuerydslRepository.findFloorByBuildingIdAndFloorValue(buildingId, floorValue);
    }
}
