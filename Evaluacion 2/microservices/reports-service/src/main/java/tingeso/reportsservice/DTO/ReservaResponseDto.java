package tingeso.reportsservice.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservaResponseDto {
    private Long id;
    private String responsibleName;
    private String responsibleEmail;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private List<String> kartIds;
    private List<GuestDto> guests;
    private int numPeople;
    private BigDecimal baseRate;
    private BigDecimal groupDiscount;
    private BigDecimal frequencyDiscount;
    private BigDecimal birthdayDiscount;
    private BigDecimal tax;
    private BigDecimal totalAmount;
    private String status;
}