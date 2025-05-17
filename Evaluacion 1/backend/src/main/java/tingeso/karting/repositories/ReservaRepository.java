package tingeso.karting.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tingeso.karting.entities.ReservaEntity;
import tingeso.karting.entities.ReservaStatus;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<ReservaEntity, Long> {

    List<ReservaEntity> findByStatus(ReservaStatus status);
    List<ReservaEntity> findByStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
        OffsetDateTime end, OffsetDateTime start);
    List<ReservaEntity> findByStartTimeBetween(OffsetDateTime from, OffsetDateTime to);

}
