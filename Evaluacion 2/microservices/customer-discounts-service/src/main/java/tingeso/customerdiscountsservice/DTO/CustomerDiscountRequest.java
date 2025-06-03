package tingeso.customerdiscountsservice.DTO;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CustomerDiscountRequest {
    private BigDecimal basePrice;
    private int monthlyVisits;
}
