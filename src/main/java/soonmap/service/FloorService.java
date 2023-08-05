package soonmap.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import soonmap.entity.Floor;
import soonmap.repository.FloorRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FloorService {

    private final FloorRepository floorRepository;

    public List<Floor> findFloorByBuilding(Long id) {
        return floorRepository.findFloorByBuilding_Id(id);
    }

    public Floor save(Floor floor) {
        return floorRepository.save(floor);
    }

    public Optional<Floor> findOneById(Long id) {
        return floorRepository.findById(id);
    }

    public Long deleteById(Long id) {
        floorRepository.deleteById(id);
        return id;
    }
}
