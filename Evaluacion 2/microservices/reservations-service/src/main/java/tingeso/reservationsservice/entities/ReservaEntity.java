package tingeso.reservationsservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "reserva")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ElementCollection
    @CollectionTable(name = "reserva_karts",
                     joinColumns = @JoinColumn(name = "reserva_id"))
    @Column(name = "kart_id")
    private List<String> kartIds;

    @Column(nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime startTime;

    @Column(nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime endTime;

    private Integer laps;
    private Integer duration;

    @Column(nullable = false)
    private Integer numPeople;

    @Column(name = "responsible_name", nullable = false)
    private String responsibleName;

    @Column(name = "responsible_email", nullable = false)
    private String responsibleEmail;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "reserva_guests",
                     joinColumns = @JoinColumn(name = "reserva_id"))
    private List<GuestEmbeddable> guests;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservaStatus status;

    @Column(nullable = false)
    private BigDecimal totalPrice;

    private BigDecimal discountGroup;
    private BigDecimal discountFreq;
    private BigDecimal discountBirthday;
}
