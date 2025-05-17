package tingeso.karting.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservaRequestDto {
    private String responsibleName;
    private String responsibleEmail;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private Integer laps;
    private Integer duration;
    private int numPeople;
    private int monthlyVisits;
    private boolean birthday;
    private List<GuestDto> guests;
}