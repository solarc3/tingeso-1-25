package tingeso.tariffsservice.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PricingResponseDto {
    private BigDecimal baseRate;
    private BigDecimal groupDiscount;
    private BigDecimal frequencyDiscount;
    private BigDecimal birthdayDiscount;
    private BigDecimal tax;
    private BigDecimal totalAmount;
}
