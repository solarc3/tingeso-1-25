package tingeso.groupdiscountsservice.DTO;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class GroupDiscountRequest {
    private BigDecimal basePrice;
    private int numberOfPeople;
}