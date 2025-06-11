package tingeso.customerdiscountsservice.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tingeso.customerdiscountsservice.entities.PriceConfigEntity;
import tingeso.customerdiscountsservice.repositories.PriceConfigRepository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PriceConfigService {

    private final PriceConfigRepository repository;

    @Transactional(readOnly = true)
    public BigDecimal getPrice(String key) {
        return repository.findById(key)
                .map(PriceConfigEntity::getPrice)
                .orElse(BigDecimal.ZERO);
    }

    @Transactional
    public void setPrice(String key, BigDecimal value) {
        repository.save(new PriceConfigEntity(key, value));
    }

    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getAllPrices() {
        return repository.findAll().stream()
                .collect(Collectors.toMap(PriceConfigEntity::getKey, PriceConfigEntity::getPrice));
    }
}