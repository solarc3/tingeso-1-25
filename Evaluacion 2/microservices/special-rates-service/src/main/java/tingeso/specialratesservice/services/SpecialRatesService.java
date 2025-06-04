package tingeso.specialratesservice.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class SpecialRatesService {
    private final PriceConfigService priceConfigService;

    @Autowired
    public SpecialRatesService(PriceConfigService priceConfigService) {
        this.priceConfigService = priceConfigService;
    }

    public BigDecimal calculateBirthdayDiscount(BigDecimal totalBasePrice, Integer numPeople) {
        if (numPeople == null || numPeople < 3) {
            return BigDecimal.ZERO;
        }

        BigDecimal discountRate = priceConfigService
            .getPrice("DESCUENTO_CUMPLEANOS")
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);


        int peopleWithDiscount;
        if (numPeople >= 3 && numPeople <= 5) {
            peopleWithDiscount = 1;
        } else if (numPeople >= 6) {
            peopleWithDiscount = 2;
        } else {
            peopleWithDiscount = 0;
        }

        System.out.println("peopleWithDiscount: " + peopleWithDiscount);

        BigDecimal pricePerPerson = totalBasePrice.divide(BigDecimal.valueOf(numPeople), 2, RoundingMode.HALF_UP);

        BigDecimal totalDiscount = pricePerPerson
            .multiply(discountRate)
            .multiply(BigDecimal.valueOf(peopleWithDiscount));


        return totalDiscount.setScale(2, RoundingMode.HALF_UP);
    }
}