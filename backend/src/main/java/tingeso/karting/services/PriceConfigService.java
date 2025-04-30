package tingeso.karting.services;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PriceConfigService {
    private final ConcurrentHashMap<String, BigDecimal> priceConfig = new ConcurrentHashMap<>();

    public PriceConfigService() {
        priceConfig.put("VUELTAS_10_PRECIO", BigDecimal.valueOf(15000));
        priceConfig.put("VUELTAS_15_PRECIO", BigDecimal.valueOf(20000));
        priceConfig.put("VUELTAS_20_PRECIO", BigDecimal.valueOf(25000));
        priceConfig.put("DESCUENTO_GRUPO_PEQUENO", BigDecimal.valueOf(10.0));
        priceConfig.put("DESCUENTO_GRUPO_MEDIANO", BigDecimal.valueOf(20.0));
        priceConfig.put("DESCUENTO_GRUPO_GRANDE", BigDecimal.valueOf(30.0));
        priceConfig.put("DESCUENTO_FRECUENCIA_BAJA", BigDecimal.valueOf(10.0));
        priceConfig.put("DESCUENTO_FRECUENCIA_MEDIA", BigDecimal.valueOf(20.0));
        priceConfig.put("DESCUENTO_FRECUENCIA_ALTA", BigDecimal.valueOf(30.0));
        priceConfig.put("DESCUENTO_CUMPLEANOS", BigDecimal.valueOf(50.0));
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