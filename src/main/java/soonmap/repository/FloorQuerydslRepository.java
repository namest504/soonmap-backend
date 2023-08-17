package soonmap.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import soonmap.entity.Floor;

import java.util.Optional;
import static soonmap.entity.QFloor.floor;

@Repository
@RequiredArgsConstructor
public class FloorQuerydslRepository {

    private final JPAQueryFactory queryFactory;

    public Floor findFloorByBuildingIdAndFloorValue(Long buildingId, int floorValue) {
        return queryFactory.selectFrom(floor)
                .where(floor.building.id.eq(buildingId).and(
                        floor.floorValue.eq(floorValue)))
                .fetchOne();
    }
}
