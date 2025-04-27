package tingeso.karting.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservaRequestDto {
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private Integer laps;
    private Integer duration;
    private int numPeople;
    private int monthlyVisits;
    private boolean birthday;
}
