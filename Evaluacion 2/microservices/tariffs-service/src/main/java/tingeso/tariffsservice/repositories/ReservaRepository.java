package tingeso.tariffsservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tingeso.tariffsservice.entities.ReservaEntity;
import tingeso.tariffsservice.entities.ReservaStatus;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<ReservaEntity, Long> {

    List<ReservaEntity> findByStatus(ReservaStatus status);
    List<ReservaEntity> findByStartTimeBetween(OffsetDateTime from, OffsetDateTime to);

}
