package tingeso.karting.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class PriceConfigServiceTest {

    @Test
    @DisplayName("Debería inicializar con los precios por defecto")
    void shouldInitializeWithDefaultPrices() {
        PriceConfigService service = new PriceConfigService();

        // Verificar precios base
        assertThat(service.getPrice("VUELTAS_10_PRECIO")).isEqualByComparingTo(BigDecimal.valueOf(15000));
        assertThat(service.getPrice("VUELTAS_15_PRECIO")).isEqualByComparingTo(BigDecimal.valueOf(20000));
        assertThat(service.getPrice("VUELTAS_20_PRECIO")).isEqualByComparingTo(BigDecimal.valueOf(25000));

        // Verificar descuentos
        assertThat(service.getPrice("DESCUENTO_GRUPO_PEQUENO")).isEqualByComparingTo(BigDecimal.valueOf(10.0));
        assertThat(service.getPrice("DESCUENTO_CUMPLEANOS")).isEqualByComparingTo(BigDecimal.valueOf(50.0));
    }

    @Test
    @DisplayName("Debería actualizar precio correctamente")
    void shouldUpdatePriceCorrectly() {
        PriceConfigService service = new PriceConfigService();

        service.setPrice("VUELTAS_10_PRECIO", BigDecimal.valueOf(18000));

        assertThat(service.getPrice("VUELTAS_10_PRECIO")).isEqualByComparingTo(BigDecimal.valueOf(18000));
    }

    @Test
    @DisplayName("Debería devolver todos los precios correctamente")
    void shouldGetAllPricesCorrectly() {
        PriceConfigService service = new PriceConfigService();

        Map<String, BigDecimal> allPrices = service.getAllPrices();

        assertThat(allPrices).isNotEmpty();
        assertThat(allPrices).containsKey("VUELTAS_10_PRECIO");
        assertThat(allPrices).containsKey("DESCUENTO_CUMPLEANOS");
    }

    @Test
    @DisplayName("Debería devolver cero cuando la clave no existe")
    void shouldReturnZeroWhenKeyDoesNotExist() {
        PriceConfigService service = new PriceConfigService();

        BigDecimal result = service.getPrice("CLAVE_INEXISTENTE");

        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }
}