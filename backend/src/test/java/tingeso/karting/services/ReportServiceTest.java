package tingeso.karting.services;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tingeso.karting.DTO.ReportEntryDto;
import tingeso.karting.DTO.ReportResponseDto;
import tingeso.karting.entities.ReservaEntity;
import tingeso.karting.entities.ReservaStatus;
import tingeso.karting.repositories.ReservaRepository;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReservaRepository reservaRepository;

    @InjectMocks
    private ReportService reportService;

    @Test
    @DisplayName("getLapsByRevenueReport agrupa por vueltas y suma ingresos correctamente")
    void testGetLapsByRevenueReport() {
        OffsetDateTime start = OffsetDateTime.now();
        OffsetDateTime end = start.plusDays(1);

        ReservaEntity r1 = ReservaEntity.builder()
            .laps(10)
            .duration(null)
            .totalPrice(BigDecimal.valueOf(100))
            .status(ReservaStatus.CONFIRMED)
            .build();

        ReservaEntity r2 = ReservaEntity.builder()
            .laps(15)
            .duration(null)
            .totalPrice(BigDecimal.valueOf(200))
            .status(ReservaStatus.CONFIRMED)
            .build();

        ReservaEntity r3 = ReservaEntity.builder()
            .laps(null)
            .duration(30)
            .totalPrice(BigDecimal.valueOf(50))
            .status(ReservaStatus.CONFIRMED)
            .build();

        ReservaEntity r4 = ReservaEntity.builder()
            .laps(null)
            .duration(40)
            .totalPrice(BigDecimal.valueOf(75))
            .status(ReservaStatus.CONFIRMED)
            .build();

        // Debería ser ignorado (no CONFIRMED)
        ReservaEntity r5 = ReservaEntity.builder()
            .laps(10)
            .duration(null)
            .totalPrice(BigDecimal.valueOf(999))
            .status(ReservaStatus.PENDING)
            .build();

        // Debería ser ignorado (laps y duration nulos)
        ReservaEntity r6 = ReservaEntity.builder()
            .laps(null)
            .duration(null)
            .totalPrice(BigDecimal.valueOf(123))
            .status(ReservaStatus.CONFIRMED)
            .build();

        when(reservaRepository.findByStartTimeBetween(start, end))
            .thenReturn(Arrays.asList(r1, r2, r3, r4, r5, r6));

        ReportResponseDto resp = reportService.getLapsByRevenueReport(start, end);

        assertThat(resp.getReportTitle()).isEqualTo("Ingresos por Número de Vueltas");
        Map<String, ReportEntryDto> map = resp.getEntries()
            .stream()
            .collect(Collectors.toMap(ReportEntryDto::getCategory, e -> e));

        // "10 vueltas": r1 + r3 = 100 + 50
        ReportEntryDto e10 = map.get("10 vueltas");
        assertThat(e10.getCount()).isEqualTo(2);
        assertThat(e10.getTotalRevenue()).isEqualByComparingTo(BigDecimal.valueOf(150));

        // "15 vueltas": solo r2
        ReportEntryDto e15 = map.get("15 vueltas");
        assertThat(e15.getCount()).isEqualTo(1);
        assertThat(e15.getTotalRevenue()).isEqualByComparingTo(BigDecimal.valueOf(200));

        // "20 vueltas": solo r4 (duration > 35)
        ReportEntryDto e20 = map.get("20 vueltas");
        assertThat(e20.getCount()).isEqualTo(1);
        assertThat(e20.getTotalRevenue()).isEqualByComparingTo(BigDecimal.valueOf(75));
    }

    @Test
    @DisplayName("getPeopleByRevenueReport agrupa por número de personas y suma ingresos correctamente")
    void testGetPeopleByRevenueReport() {
        OffsetDateTime start = OffsetDateTime.now();
        OffsetDateTime end = start.plusDays(1);

        ReservaEntity r1 = ReservaEntity.builder()
            .numPeople(1)
            .totalPrice(BigDecimal.valueOf(100))
            .status(ReservaStatus.CONFIRMED)
            .build();

        ReservaEntity r2 = ReservaEntity.builder()
            .numPeople(4)
            .totalPrice(BigDecimal.valueOf(50))
            .status(ReservaStatus.CONFIRMED)
            .build();

        ReservaEntity r3 = ReservaEntity.builder()
            .numPeople(8)
            .totalPrice(BigDecimal.valueOf(80))
            .status(ReservaStatus.CONFIRMED)
            .build();

        ReservaEntity r4 = ReservaEntity.builder()
            .numPeople(12)
            .totalPrice(BigDecimal.valueOf(120))
            .status(ReservaStatus.CONFIRMED)
            .build();

        // Ignorado porque no está CONFIRMED
        ReservaEntity r5 = ReservaEntity.builder()
            .numPeople(2)
            .totalPrice(BigDecimal.valueOf(999))
            .status(ReservaStatus.PENDING)
            .build();

        when(reservaRepository.findByStartTimeBetween(start, end))
            .thenReturn(Arrays.asList(r1, r2, r3, r4, r5));

        ReportResponseDto resp = reportService.getPeopleByRevenueReport(start, end);

        assertThat(resp.getReportTitle()).isEqualTo("Ingresos por Número de Personas");
        Map<String, ReportEntryDto> map = resp.getEntries()
            .stream()
            .collect(Collectors.toMap(ReportEntryDto::getCategory, e -> e));

        ReportEntryDto ep12 = map.get("1-2 personas");
        assertThat(ep12.getCount()).isEqualTo(1);
        assertThat(ep12.getTotalRevenue()).isEqualByComparingTo(BigDecimal.valueOf(100));

        ReportEntryDto ep35 = map.get("3-5 personas");
        assertThat(ep35.getCount()).isEqualTo(1);
        assertThat(ep35.getTotalRevenue()).isEqualByComparingTo(BigDecimal.valueOf(50));

        ReportEntryDto ep610 = map.get("6-10 personas");
        assertThat(ep610.getCount()).isEqualTo(1);
        assertThat(ep610.getTotalRevenue()).isEqualByComparingTo(BigDecimal.valueOf(80));

        ReportEntryDto ep1115 = map.get("11-15 personas");
        assertThat(ep1115.getCount()).isEqualTo(1);
        assertThat(ep1115.getTotalRevenue()).isEqualByComparingTo(BigDecimal.valueOf(120));
    }
}
