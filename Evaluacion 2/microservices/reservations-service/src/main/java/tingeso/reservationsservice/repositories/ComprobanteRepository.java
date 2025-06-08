package tingeso.reservationsservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tingeso.reservationsservice.entities.ComprobanteEntity;

import java.util.List;

@Repository
public interface ComprobanteRepository extends JpaRepository<ComprobanteEntity, Long> {
    List<ComprobanteEntity> findByReservaId(Long reservaId);
}