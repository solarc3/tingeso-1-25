package tingeso.reservationsservice.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailabilityRequestDto {
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private int numKarts;
    private int duration; //minutos
}
