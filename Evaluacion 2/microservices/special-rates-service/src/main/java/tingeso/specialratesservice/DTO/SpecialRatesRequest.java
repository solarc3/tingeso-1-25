package tingeso.specialratesservice.DTO;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SpecialRatesRequest {
    private BigDecimal basePrice;
    private int numPeople;
}
