package tingeso.reservationsservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tingeso.reservationsservice.entities.ReservaEntity;
import tingeso.reservationsservice.entities.ReservaStatus;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<ReservaEntity, Long> {

    List<ReservaEntity> findByStatus(ReservaStatus status);
    List<ReservaEntity> findByStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
        OffsetDateTime end, OffsetDateTime start);
    List<ReservaEntity> findByStartTimeBetween(OffsetDateTime from, OffsetDateTime to);

}
