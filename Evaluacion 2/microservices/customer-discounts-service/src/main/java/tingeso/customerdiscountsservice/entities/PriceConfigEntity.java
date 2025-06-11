package tingeso.customerdiscountsservice.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "price_config")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceConfigEntity {
    @Id
    @Column(name = "config_key", nullable = false)
    private String key;

    @Column(nullable = false)
    private BigDecimal price;
}
