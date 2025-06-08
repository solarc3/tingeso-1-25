package tingeso.reportsservice.entities;

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
public class ReservaEntity {

    private Long id;
    private List<String> kartIds;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private Integer laps;
    private Integer duration;
    private Integer numPeople;
    private String responsibleName;
    private String responsibleEmail;
    private List<GuestEmbeddable> guests;
    private ReservaStatus status;
    private BigDecimal totalPrice;
    private BigDecimal discountGroup;
    private BigDecimal discountFreq;
    private BigDecimal discountBirthday;
}