package tingeso.karting.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservaResponseDto {
    private Long id;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private String kartId;
    private int numPeople;
    private BigDecimal baseRate;
    private BigDecimal groupDiscount;
    private BigDecimal frequencyDiscount;
    private BigDecimal birthdayDiscount;
    private BigDecimal tax;
    private BigDecimal totalAmount;
    private String status;
}
