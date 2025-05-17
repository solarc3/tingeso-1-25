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
public class KartAvailabilityResponseDto {
    private OffsetDateTime time;
    private int totalKarts;
    private int availableKarts;
    private List<String> availableKartIds;
}