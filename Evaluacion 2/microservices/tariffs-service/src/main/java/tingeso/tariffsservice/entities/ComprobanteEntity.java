package tingeso.tariffsservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Data
@Table(name = "comprobante")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComprobanteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String codigoReserva;

    @OneToOne
    @JoinColumn(name = "reserva_id")
    private ReservaEntity reserva;

    @Column(nullable = false)
    private OffsetDateTime fechaEmision;

    @Column(columnDefinition = "TEXT")
    private String contenido;
    @Column(nullable = false)
    private Boolean enviado;

    @Column
    private OffsetDateTime fechaEnvio;
}
