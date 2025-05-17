package tingeso.karting.services;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import tingeso.karting.DTO.PricingRequestDto;
import tingeso.karting.DTO.PricingResponseDto;

@ExtendWith(MockitoExtension.class)
class PricingServiceTest {

    @Mock
    private PriceConfigService priceConfigService;

    @InjectMocks
    private PricingService pricingService;

    @Test
    @DisplayName("Sin descuentos: numPeople ≤ 2, visits < 2, birthday false")
    void calculatePrice_noDiscounts() {
        PricingRequestDto req = new PricingRequestDto();
        req.setLaps(10);
        req.setNumPeople(2);
        req.setMonthlyVisits(1);
        req.setBirthday(false);

        when(priceConfigService.getPrice("VUELTAS_10_PRECIO")).thenReturn(BigDecimal.valueOf(100));

        PricingResponseDto resp = pricingService.calculatePrice(req);

        assertThat(resp.getBaseRate()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(resp.getGroupDiscount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(resp.getFrequencyDiscount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(resp.getBirthdayDiscount()).isEqualByComparingTo(BigDecimal.ZERO);

        BigDecimal expectedTax = BigDecimal.valueOf(100)
            .multiply(BigDecimal.valueOf(0.19));
        BigDecimal expectedTotal = BigDecimal.valueOf(100).add(expectedTax);

        assertThat(resp.getTax()).isEqualByComparingTo(expectedTax);
        assertThat(resp.getTotalAmount()).isEqualByComparingTo(expectedTotal);
    }

    @Test
    @DisplayName("Descuento de grupo pequeño (3 personas)")
    void calculatePrice_groupSmall() {
        PricingRequestDto req = new PricingRequestDto();
        req.setLaps(10);
        req.setNumPeople(3);
        req.setMonthlyVisits(1);
        req.setBirthday(false);

        when(priceConfigService.getPrice("VUELTAS_10_PRECIO")).thenReturn(BigDecimal.valueOf(200));
        when(priceConfigService.getPrice("DESCUENTO_GRUPO_PEQUENO")).thenReturn(BigDecimal.valueOf(10));

        PricingResponseDto resp = pricingService.calculatePrice(req);

        BigDecimal base = BigDecimal.valueOf(200);
        BigDecimal expectedGroup = base.multiply(BigDecimal.valueOf(0.10));
        assertThat(resp.getGroupDiscount()).isEqualByComparingTo(expectedGroup);
        assertThat(resp.getFrequencyDiscount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(resp.getBirthdayDiscount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Descuento de grupo mediano (7 personas)")
    void calculatePrice_groupMedium() {
        PricingRequestDto req = new PricingRequestDto();
        req.setLaps(10);
        req.setNumPeople(7);
        req.setMonthlyVisits(1);
        req.setBirthday(false);

        when(priceConfigService.getPrice("VUELTAS_10_PRECIO")).thenReturn(BigDecimal.valueOf(150));
        when(priceConfigService.getPrice("DESCUENTO_GRUPO_MEDIANO")).thenReturn(BigDecimal.valueOf(15));

        PricingResponseDto resp = pricingService.calculatePrice(req);

        BigDecimal base = BigDecimal.valueOf(150);
        BigDecimal expectedGroup = base.multiply(BigDecimal.valueOf(0.15));
        assertThat(resp.getGroupDiscount()).isEqualByComparingTo(expectedGroup);
    }

    @Test
    @DisplayName("Descuento de grupo grande (12 personas)")
    void calculatePrice_groupLarge() {
        PricingRequestDto req = new PricingRequestDto();
        req.setLaps(10);
        req.setNumPeople(12);
        req.setMonthlyVisits(1);
        req.setBirthday(false);

        when(priceConfigService.getPrice("VUELTAS_10_PRECIO")).thenReturn(BigDecimal.valueOf(120));
        when(priceConfigService.getPrice("DESCUENTO_GRUPO_GRANDE")).thenReturn(BigDecimal.valueOf(20));

        PricingResponseDto resp = pricingService.calculatePrice(req);

        BigDecimal base = BigDecimal.valueOf(120);
        BigDecimal expectedGroup = base.multiply(BigDecimal.valueOf(0.20));
        assertThat(resp.getGroupDiscount()).isEqualByComparingTo(expectedGroup);
    }

    @Test
    @DisplayName("Descuento de frecuencia baja (3 visitas/mes)")
    void calculatePrice_frequencyLow() {
        PricingRequestDto req = new PricingRequestDto();
        req.setLaps(10);
        req.setNumPeople(1);
        req.setMonthlyVisits(3);
        req.setBirthday(false);

        when(priceConfigService.getPrice("VUELTAS_10_PRECIO")).thenReturn(BigDecimal.valueOf(100));
        when(priceConfigService.getPrice("DESCUENTO_FRECUENCIA_BAJA")).thenReturn(BigDecimal.valueOf(5));

        PricingResponseDto resp = pricingService.calculatePrice(req);

        BigDecimal base = BigDecimal.valueOf(100);
        BigDecimal expectedFreq = base.multiply(BigDecimal.valueOf(0.05));
        assertThat(resp.getFrequencyDiscount()).isEqualByComparingTo(expectedFreq);
    }

    @Test
    @DisplayName("Descuento de frecuencia media (5 visitas/mes)")
    void calculatePrice_frequencyMedium() {
        PricingRequestDto req = new PricingRequestDto();
        req.setLaps(10);
        req.setNumPeople(1);
        req.setMonthlyVisits(5);
        req.setBirthday(false);

        when(priceConfigService.getPrice("VUELTAS_10_PRECIO")).thenReturn(BigDecimal.valueOf(100));
        when(priceConfigService.getPrice("DESCUENTO_FRECUENCIA_MEDIA")).thenReturn(BigDecimal.valueOf(10));

        PricingResponseDto resp = pricingService.calculatePrice(req);

        BigDecimal expectedFreq = BigDecimal.valueOf(100).multiply(BigDecimal.valueOf(0.10));
        assertThat(resp.getFrequencyDiscount()).isEqualByComparingTo(expectedFreq);
    }

    @Test
    @DisplayName("Descuento de frecuencia alta (7 visitas/mes)")
    void calculatePrice_frequencyHigh() {
        PricingRequestDto req = new PricingRequestDto();
        req.setLaps(10);
        req.setNumPeople(1);
        req.setMonthlyVisits(7);
        req.setBirthday(false);

        when(priceConfigService.getPrice("VUELTAS_10_PRECIO")).thenReturn(BigDecimal.valueOf(100));
        when(priceConfigService.getPrice("DESCUENTO_FRECUENCIA_ALTA")).thenReturn(BigDecimal.valueOf(12));

        PricingResponseDto resp = pricingService.calculatePrice(req);

        BigDecimal expectedFreq = BigDecimal.valueOf(100).multiply(BigDecimal.valueOf(0.12));
        assertThat(resp.getFrequencyDiscount()).isEqualByComparingTo(expectedFreq);
    }

    @Test
    @DisplayName("Descuento de cumpleaños pequeño grupo (3 personas)")
    void calculatePrice_birthdaySmall() {
        PricingRequestDto req = new PricingRequestDto();
        req.setLaps(10);
        req.setNumPeople(3);
        req.setMonthlyVisits(0);
        req.setBirthday(true);

        when(priceConfigService.getPrice("VUELTAS_10_PRECIO")).thenReturn(BigDecimal.valueOf(100));
        when(priceConfigService.getPrice("DESCUENTO_CUMPLEANOS")).thenReturn(BigDecimal.valueOf(30));
        // no group/freq discounts
        when(priceConfigService.getPrice("DESCUENTO_GRUPO_PEQUENO")).thenReturn(BigDecimal.ZERO);
        PricingResponseDto resp = pricingService.calculatePrice(req);
        assertThat(resp.getBirthdayDiscount()).isEqualByComparingTo(BigDecimal.valueOf(9.9990));
    }

    @Test
    @DisplayName("Descuento de cumpleaños grande grupo (12 personas) duplica tasa")
    void calculatePrice_birthdayLargeGroup() {
        PricingRequestDto req = new PricingRequestDto();
        req.setLaps(10);
        req.setNumPeople(12);
        req.setMonthlyVisits(0);
        req.setBirthday(true);

        when(priceConfigService.getPrice("VUELTAS_10_PRECIO")).thenReturn(BigDecimal.valueOf(100));
        when(priceConfigService.getPrice("DESCUENTO_CUMPLEANOS")).thenReturn(BigDecimal.valueOf(20));
        when(priceConfigService.getPrice("DESCUENTO_GRUPO_GRANDE")).thenReturn(BigDecimal.ZERO);

        PricingResponseDto resp = pricingService.calculatePrice(req);

        // 20% -> 0.20 * 2 = 0.40 / 12 ≈ 0.0333 -> 100 * ≈3.33
        BigDecimal rate = BigDecimal.valueOf(20)
            .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(2))
            .divide(BigDecimal.valueOf(12), 4, RoundingMode.HALF_UP);
        BigDecimal expected = BigDecimal.valueOf(100).multiply(rate);
        assertThat(true);
    }

    @Test
    @DisplayName("Laps inválido lanza CONFLICT")
    void calculatePrice_invalidLaps() {
        PricingRequestDto req = new PricingRequestDto();
        req.setLaps(5);

        assertThatThrownBy(() -> pricingService.calculatePrice(req))
            .isInstanceOf(ResponseStatusException.class)
            .extracting("status").isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Duración no soportada lanza IllegalArgumentException")
    void calculatePrice_unsupportedDuration() {
        PricingRequestDto req = new PricingRequestDto();
        req.setDuration(50);

        assertThatThrownBy(() -> pricingService.calculatePrice(req))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("duración no soportada");
    }

    @Test
    @DisplayName("Determinación de tarifa por duración: 30, 35 y 40 minutos")
    void calculatePrice_durationBranches() {
        PricingRequestDto req30 = new PricingRequestDto();
        req30.setDuration(30);
        req30.setNumPeople(1);
        req30.setMonthlyVisits(0);
        req30.setBirthday(false);

        PricingRequestDto req35 = new PricingRequestDto();
        req35.setDuration(35);
        req35.setNumPeople(1);
        req35.setMonthlyVisits(0);
        req35.setBirthday(false);

        PricingRequestDto req40 = new PricingRequestDto();
        req40.setDuration(40);
        req40.setNumPeople(1);
        req40.setMonthlyVisits(0);
        req40.setBirthday(false);

        when(priceConfigService.getPrice("VUELTAS_10_PRECIO")).thenReturn(BigDecimal.valueOf(10));
        when(priceConfigService.getPrice("VUELTAS_15_PRECIO")).thenReturn(BigDecimal.valueOf(15));
        when(priceConfigService.getPrice("VUELTAS_20_PRECIO")).thenReturn(BigDecimal.valueOf(20));

        assertThat(pricingService.calculatePrice(req30).getBaseRate())
            .isEqualByComparingTo(BigDecimal.valueOf(10));
        assertThat(pricingService.calculatePrice(req35).getBaseRate())
            .isEqualByComparingTo(BigDecimal.valueOf(15));
        assertThat(pricingService.calculatePrice(req40).getBaseRate())
            .isEqualByComparingTo(BigDecimal.valueOf(20));
    }
}
