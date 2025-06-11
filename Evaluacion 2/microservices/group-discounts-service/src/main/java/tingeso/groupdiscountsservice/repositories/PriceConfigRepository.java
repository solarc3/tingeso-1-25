package tingeso.groupdiscountsservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tingeso.groupdiscountsservice.entities.PriceConfigEntity;

@Repository
public interface PriceConfigRepository extends JpaRepository<PriceConfigEntity, String> {
}
