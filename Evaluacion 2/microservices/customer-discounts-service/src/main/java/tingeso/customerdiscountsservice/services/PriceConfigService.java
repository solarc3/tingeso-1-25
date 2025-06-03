package tingeso.customerdiscountsservice.services;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PriceConfigService {
    private final ConcurrentHashMap<String, BigDecimal> priceConfig = new ConcurrentHashMap<>();

    public PriceConfigService() {
        priceConfig.put("DESCUENTO_FRECUENCIA_BAJA", BigDecimal.valueOf(10.0));
        priceConfig.put("DESCUENTO_FRECUENCIA_MEDIA", BigDecimal.valueOf(20.0));
        priceConfig.put("DESCUENTO_FRECUENCIA_ALTA", BigDecimal.valueOf(30.0));
    }

    public BigDecimal getPrice(String key) {
        return priceConfig.getOrDefault(key, BigDecimal.ZERO);
    }

    public void setPrice(String key, BigDecimal value) {
        priceConfig.put(key, value);
    }

    public ConcurrentHashMap<String, BigDecimal> getAllPrices() {
        return new ConcurrentHashMap<>(priceConfig);
    }
}