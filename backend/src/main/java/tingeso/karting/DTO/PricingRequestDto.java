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
public class PricingRequestDto {
    private OffsetDateTime startTime;
    private Integer laps;       // opcinal num vueltas
    private Integer duration;   // opcional num minutos
    private int numPeople;
    private int monthlyVisits;  // visitas del cliente en el mes
    private boolean birthday;   // si hay cumpleañero en la reserva
}