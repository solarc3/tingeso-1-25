package tingeso.specialratesservice.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class SpecialRatesService {
    private final PriceConfigService priceConfigService;
    @Autowired
    public SpecialRatesService(PriceConfigService priceConfigService) {this.priceConfigService = priceConfigService;}

    public BigDecimal calculateBirthdayDiscount(BigDecimal basePrice, int numPeople){

        BigDecimal pricePerPerson = basePrice.divide(BigDecimal.valueOf(numPeople), 2, RoundingMode.HALF_UP);
        BigDecimal discountRate = priceConfigService.getPrice("DESCUENTO_CUMPLEANOS").divide(BigDecimal.valueOf(100),2,RoundingMode.HALF_UP);
        BigDecimal discountAmount;
        if (numPeople >= 3 && numPeople <= 5) {
            discountAmount = pricePerPerson.multiply(discountRate);
        }
        else if (numPeople >= 6 && numPeople <= 10) {
            discountAmount = pricePerPerson.multiply(discountRate).multiply(BigDecimal.valueOf(2));
        }
        else {
            discountAmount = pricePerPerson.multiply(discountRate).multiply(BigDecimal.valueOf(2));
        }
        return discountAmount.setScale(2, RoundingMode.HALF_UP);
    }

}
