package tingeso.customerdiscountsservice.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class CustomerDiscountService {
    private final PriceConfigService priceConfigService;
    @Autowired
    public CustomerDiscountService(PriceConfigService priceConfigService) {this.priceConfigService = priceConfigService;}
    public BigDecimal calculateFrecuencyDiscount(BigDecimal price, Integer visits) {
        BigDecimal discountRate = BigDecimal.ZERO;
        if (visits >= 7) {
            discountRate = priceConfigService
                .getPrice("DESCUENTO_FRECUENCIA_ALTA")
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        else if (visits >= 5) {
            discountRate = priceConfigService
                .getPrice("DESCUENTO_FRECUENCIA_MEDIA")
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        else if (visits >= 2) {
            discountRate = priceConfigService
                .getPrice("DESCUENTO_FRECUENCIA_BAJA")
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        }
        return price.multiply(discountRate)
            .setScale(2, RoundingMode.HALF_UP);
    }
}
