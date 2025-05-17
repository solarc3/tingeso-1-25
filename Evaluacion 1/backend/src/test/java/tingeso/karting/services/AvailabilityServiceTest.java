package tingeso.karting.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import tingeso.karting.DTO.AvailabilityRequestDto;
import tingeso.karting.DTO.AvailabilityResponseDto;
import tingeso.karting.entities.ReservaEntity;
import tingeso.karting.entities.ReservaStatus;
import tingeso.karting.repositories.ReservaRepository;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private KartService kartService;

    @InjectMocks
    private AvailabilityService availabilityService;

    private OffsetDateTime now;
    private List<String> kartIds;

    @BeforeEach
    void setUp() {
        now = OffsetDateTime.now();
        kartIds = Arrays.asList("K001", "K002");
        when(kartService.getAllKartIds()).thenReturn(kartIds);
        when(reservaRepository.count()).thenReturn(0L);
        when(reservaRepository.findByStatus(any(ReservaStatus.class))).thenReturn(Collections.emptyList());
        availabilityService.initAfterStartup();
    }

    @Test
    @DisplayName("initAfterStartup debe poblar el calendario con todos los karts y sin intervalos")
    void testInitAfterStartupPopulatesCalendar() {
        @SuppressWarnings("unchecked")
        Map<String, NavigableSet<?>> calendar =
            (Map<String, NavigableSet<?>>) ReflectionTestUtils.getField(availabilityService, "calendar");

        assertThat(calendar).isNotNull();
        assertThat(calendar.keySet()).containsExactlyInAnyOrderElementsOf(kartIds);
        calendar.values().forEach(set -> assertThat(set).isEmpty());
    }

    @Test
    @DisplayName("registerKartReservation + getFreeKarts detecta ocupación y liberación")
    void testRegisterKartReservationAndGetFreeKarts() {
        availabilityService.registerKartReservation("K001", now.plusHours(1), now.plusHours(2));

        List<String> ocupada = availabilityService.getFreeKarts(
            now.plusHours(1).plusMinutes(30),
            now.plusHours(1).plusMinutes(45));
        assertThat(ocupada).doesNotContain("K001");

        List<String> libre = availabilityService.getFreeKarts(
            now.plusHours(2).plusMinutes(1),
            now.plusHours(3));
        assertThat(libre).contains("K001");
    }

    @Test
    @DisplayName("registerReservation con lista vacía no altera el calendario")
    void testRegisterReservationWithEmptyKarts() {
        ReservaEntity resEmpty = ReservaEntity.builder()
            .id(10L)
            .kartIds(Collections.emptyList())
            .startTime(now)
            .endTime(now.plusHours(1))
            .status(ReservaStatus.CONFIRMED)
            .build();
        availabilityService.registerReservation(resEmpty);

        List<String> libres = availabilityService.getFreeKarts(now, now.plusHours(1));
        assertThat(libres).containsAll(kartIds);
    }

    @Test
    @DisplayName("removeReservation elimina correctamente un intervalo previamente registrado")
    void testRemoveReservation() {
        ReservaEntity res = ReservaEntity.builder()
            .id(2L)
            .kartIds(Collections.singletonList("K002"))
            .startTime(now.plusHours(3))
            .endTime(now.plusHours(4))
            .status(ReservaStatus.CONFIRMED)
            .build();

        availabilityService.registerReservation(res);
        assertThat(availabilityService.getFreeKarts(
            now.plusHours(3).plusMinutes(10),
            now.plusHours(3).plusMinutes(15)))
            .doesNotContain("K002");
        availabilityService.removeReservation(res);
        assertThat(availabilityService.getFreeKarts(
            now.plusHours(3).plusMinutes(10),
            now.plusHours(3).plusMinutes(15)))
            .contains("K002");
    }

    @Test
    @DisplayName("checkAvail retorna ok y sin conflictos cuando hay suficiente disponibilidad")
    void testCheckAvailReturnsOkAndNoConflicts() {
        AvailabilityRequestDto req = new AvailabilityRequestDto();
        req.setStartTime(now.plusHours(5));
        req.setEndTime(now.plusHours(6));
        req.setNumKarts(2);
        req.setDuration(60);

        AvailabilityResponseDto resp = availabilityService.checkAvail(req);
        assertThat(resp.isOk()).isTrue();
        assertThat(resp.getConflicts()).isNull();
    }

    @Test
    @DisplayName("checkAvail retorna false y lista de conflictos cuando faltan karts")
    void testCheckAvailReturnsFalseAndConflicts() {
        AvailabilityRequestDto req = new AvailabilityRequestDto();
        req.setStartTime(now.plusHours(5));
        req.setEndTime(now.plusHours(6));
        req.setNumKarts(3);
        req.setDuration(60);

        AvailabilityResponseDto resp = availabilityService.checkAvail(req);
        assertThat(resp.isOk()).isFalse();
        assertThat(resp.getConflicts()).isEmpty();
    }

    @Test
    @DisplayName("removeReservation acepta entidades con kartIds nulo sin lanzar excepción")
    void testRemoveReservationNullKarts() {
        ReservaEntity resNull = new ReservaEntity();
        resNull.setId(5L);
        resNull.setKartIds(null);
        resNull.setStartTime(now);
        resNull.setEndTime(now.plusHours(1));

        availabilityService.removeReservation(resNull);
        // Si no lanzó excepción, el test pasa
    }

    @Test
    @DisplayName("initAfterStartup recarga intervalos existentes de la base de datos")
    void testInitCalendarLoadsExisting() {
        // Preparo dos reservas: una CONFIRMED y otra PENDING
        ReservaEntity c = ReservaEntity.builder()
            .id(3L)
            .kartIds(Collections.singletonList("K001"))
            .startTime(now.plusDays(1))
            .endTime(now.plusDays(1).plusHours(1))
            .status(ReservaStatus.CONFIRMED)
            .build();

        ReservaEntity p = ReservaEntity.builder()
            .id(4L)
            .kartIds(Collections.singletonList("K002"))
            .startTime(now.plusDays(2))
            .endTime(now.plusDays(2).plusHours(2))
            .status(ReservaStatus.PENDING)
            .build();

        reset(reservaRepository);
        when(kartService.getAllKartIds()).thenReturn(kartIds);
        when(reservaRepository.count()).thenReturn(2L);
        when(reservaRepository.findByStatus(ReservaStatus.CONFIRMED))
            .thenReturn(Collections.singletonList(c));
        when(reservaRepository.findByStatus(ReservaStatus.PENDING))
            .thenReturn(Collections.singletonList(p));

        availabilityService.initAfterStartup();

        assertThat(availabilityService.getFreeKarts(
            now.plusDays(1).plusMinutes(10),
            now.plusDays(1).plusMinutes(20)))
            .doesNotContain("K001");

        assertThat(availabilityService.getFreeKarts(
            now.plusDays(2).plusMinutes(30),
            now.plusDays(2).plusMinutes(40)))
            .doesNotContain("K002");
    }
    @Test
    @DisplayName("initAfterStartup maneja kartId no existente y cae en branch de ERROR")
    void testInitAfterStartupWithInvalidKartId() {
        ReservaEntity bad = ReservaEntity.builder()
            .id(99L)
            .kartIds(Collections.singletonList("BAD"))
            .startTime(now.plusHours(1))
            .endTime(now.plusHours(2))
            .status(ReservaStatus.CONFIRMED)
            .build();

        reset(reservaRepository);
        when(kartService.getAllKartIds()).thenReturn(kartIds);
        when(reservaRepository.count()).thenReturn(1L);
        when(reservaRepository.findByStatus(ReservaStatus.CONFIRMED))
            .thenReturn(Collections.singletonList(bad));
        when(reservaRepository.findByStatus(ReservaStatus.PENDING))
            .thenReturn(Collections.emptyList());

        availabilityService.initAfterStartup();

        @SuppressWarnings("unchecked")
        Map<String, NavigableSet<?>> calendar =
            (Map<String, NavigableSet<?>>) ReflectionTestUtils.getField(availabilityService, "calendar");
        assertThat(calendar.keySet()).doesNotContain("BAD");
    }

    @Test
    @DisplayName("registerReservation con kartIds null ejecuta el else sin lanzar excepción")
    void testRegisterReservationNullList() {
        ReservaEntity resNull = ReservaEntity.builder()
            .id(123L)
            .kartIds(null)
            .startTime(now)
            .endTime(now.plusHours(1))
            .status(ReservaStatus.PENDING)
            .build();
        availabilityService.registerReservation(resNull);
    }

    @Test
    @DisplayName("Interval: overlaps, equals, hashCode y compareTo via reflection")
    void testIntervalMethodsReflectively() throws Exception {
        Class<?> cls = Class.forName("tingeso.karting.services.AvailabilityService$Interval");
        Constructor<?> ctor = cls.getDeclaredConstructor(OffsetDateTime.class, OffsetDateTime.class);
        ctor.setAccessible(true);

        OffsetDateTime s1 = now;
        OffsetDateTime e1 = now.plusHours(1);
        Object i1 = ctor.newInstance(s1, e1);
        Object i2 = ctor.newInstance(s1, e1);

        assertThat(i1).isEqualTo(i2);
        assertThat(i1.hashCode()).isEqualTo(i2.hashCode());

        Method compareTo = cls.getMethod("compareTo", cls);
        assertThat((int)compareTo.invoke(i1, i2)).isZero();

        Method overlaps = cls.getMethod("overlaps", OffsetDateTime.class, OffsetDateTime.class);
        assertThat((boolean)overlaps.invoke(i1, now.plusMinutes(30), now.plusMinutes(30))).isTrue();
        assertThat((boolean)overlaps.invoke(i1, now.plusHours(2), now.plusHours(3))).isFalse();

        Object i3 = ctor.newInstance(now.plusHours(2), now.plusHours(3));
        assertThat((int)compareTo.invoke(i1, i3)).isLessThan(0);
        assertThat((int)compareTo.invoke(i3, i1)).isGreaterThan(0);
    }

}
