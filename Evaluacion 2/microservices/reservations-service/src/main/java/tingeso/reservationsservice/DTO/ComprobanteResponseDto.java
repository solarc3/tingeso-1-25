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
public class ComprobanteResponseDto {
    private Long id;
    private String codigoReserva;
    private OffsetDateTime fechaEmision;
    private Boolean enviado;
    private Long reservaId;
}