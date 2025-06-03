package tingeso.groupdiscountsservice.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class GroupDiscountService {
    private final PriceConfigService priceConfigService;
    @Autowired
    public GroupDiscountService(PriceConfigService priceConfigService) {this.priceConfigService = priceConfigService; }
    public BigDecimal calculateGroupDiscount(BigDecimal basePrice, int numPeople) {
        if (numPeople <= 2) return BigDecimal.ZERO;

        BigDecimal discountRate;
        if (numPeople <= 5) {
            discountRate = BigDecimal.valueOf(priceConfigService.getPrice("DESCUENTO_GRUPO_PEQUENO")
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                .doubleValue());
        } else if (numPeople <= 10) {
            discountRate = BigDecimal.valueOf(priceConfigService.getPrice("DESCUENTO_GRUPO_MEDIANO")
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                .doubleValue());
        } else {
            discountRate = BigDecimal.valueOf(priceConfigService.getPrice("DESCUENTO_GRUPO_GRANDE")
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                .doubleValue());
        }

        return basePrice.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
    }
}