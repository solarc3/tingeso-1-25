package tingeso.karting.services;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import tingeso.karting.DTO.*;
import tingeso.karting.entities.*;
import tingeso.karting.repositories.ReservaRepository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock
    private AvailabilityService availabilityService;
    @Mock
    private PricingService pricingService;
    @Mock
    private ReservaRepository reservaRepository;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private KartService kartService;
    @Mock
    private ComprobanteService comprobanteService;

    @InjectMocks
    private ReservaService reservaService;

    private OffsetDateTime now;

    @BeforeEach
    void setUp() {
        now = OffsetDateTime.now();
    }

    @Test
    @DisplayName("checkAvailability lanza CONFLICT cuando no hay disponibilidad")
    void testCheckAvailabilityConflict() {
        ReservaRequestDto req = ReservaRequestDto.builder()
            .startTime(now)
            .endTime(now.plusHours(1))
            .numPeople(1)
            .build();
        AvailabilityResponseDto avail = new AvailabilityResponseDto();
        avail.setOk(false);
        when(availabilityService.checkAvail(any())).thenReturn(avail);

        assertThatThrownBy(() -> reservaService.checkAvailability(req))
            .isInstanceOf(ResponseStatusException.class)
            .extracting("status").isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("checkAvailability retorna PricingResponseDto cuando hay disponibilidad")
    void testCheckAvailabilitySuccess() {
        ReservaRequestDto req = ReservaRequestDto.builder()
            .startTime(now)
            .endTime(now.plusHours(1))
            .numPeople(2)
            .build();
        AvailabilityResponseDto avail = new AvailabilityResponseDto();
        avail.setOk(true);
        when(availabilityService.checkAvail(any())).thenReturn(avail);

        PricingRequestDto pReq = new PricingRequestDto();
        when(modelMapper.map(eq(req), eq(PricingRequestDto.class))).thenReturn(pReq);

        PricingResponseDto pricing = new PricingResponseDto();
        when(pricingService.calculatePrice(pReq)).thenReturn(pricing);

        PricingResponseDto result = reservaService.checkAvailability(req);
        assertThat(result).isSameAs(pricing);
    }

    @Test
    @DisplayName("createReservation lanza BAD_REQUEST si guests es null")
    void testCreateReservationGuestsNull() {
        ReservaRequestDto req = ReservaRequestDto.builder()
            .startTime(now)
            .endTime(now.plusHours(1))
            .numPeople(1)
            .guests(null)
            .build();

        assertThatThrownBy(() -> reservaService.createReservation(req))
            .isInstanceOf(ResponseStatusException.class)
            .extracting("status").isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("createReservation lanza BAD_REQUEST si tamaño de guests != numPeople")
    void testCreateReservationGuestsMismatch() {
        List<GuestDto> guests = List.of(
            new GuestDto("n1","e1"), new GuestDto("n2","e2")
                                       );
        ReservaRequestDto req = ReservaRequestDto.builder()
            .startTime(now)
            .endTime(now.plusHours(1))
            .numPeople(1)
            .guests(guests)
            .build();

        assertThatThrownBy(() -> reservaService.createReservation(req))
            .isInstanceOf(ResponseStatusException.class)
            .extracting("status").isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("createReservation lanza CONFLICT si faltan karts libres")
    void testCreateReservationNotEnoughKarts() {
        List<GuestDto> guests = List.of(new GuestDto("g","e"));
        ReservaRequestDto req = ReservaRequestDto.builder()
            .startTime(now)
            .endTime(now.plusHours(1))
            .numPeople(2)
            .guests(guests)
            .laps(1)
            .duration(30)
            .responsibleName("R")
            .responsibleEmail("r@e.com")
            .build();

        AvailabilityResponseDto avail = new AvailabilityResponseDto();
        avail.setOk(true);

        assertThatThrownBy(() -> reservaService.createReservation(req))
            .isInstanceOf(ResponseStatusException.class)
            .extracting("status").isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("createReservation éxito: reserva creada y comprobante enviado")
    void testCreateReservationSuccess() {
        List<GuestDto> guests = List.of(
            new GuestDto("g1","e1"), new GuestDto("g2","e2")
                                       );
        ReservaRequestDto req = ReservaRequestDto.builder()
            .startTime(now)
            .endTime(now.plusHours(1))
            .numPeople(2)
            .laps(3)
            .duration(45)
            .responsibleName("John")
            .responsibleEmail("john@e.com")
            .guests(guests)
            .build();

        AvailabilityResponseDto avail = new AvailabilityResponseDto();
        avail.setOk(true);
        when(availabilityService.checkAvail(any())).thenReturn(avail);
        when(availabilityService.getFreeKarts(req.getStartTime(), req.getEndTime()))
            .thenReturn(List.of("K1","K2","K3"));

        PricingRequestDto pReq = new PricingRequestDto();
        when(modelMapper.map(req, PricingRequestDto.class)).thenReturn(pReq);

        PricingResponseDto pricing = PricingResponseDto.builder()
            .baseRate(BigDecimal.valueOf(10.0))
            .groupDiscount(BigDecimal.valueOf(1.0))
            .frequencyDiscount(BigDecimal.valueOf(2.0))
            .birthdayDiscount(BigDecimal.valueOf(3.0))
            .tax(BigDecimal.valueOf(4.0))
            .totalAmount(BigDecimal.valueOf(100.0))
            .build();
        when(pricingService.calculatePrice(pReq)).thenReturn(pricing);

        ReservaEntity saved = ReservaEntity.builder()
            .id(5L)
            .startTime(req.getStartTime())
            .endTime(req.getEndTime())
            .kartIds(List.of("K1","K2"))
            .laps(req.getLaps())
            .duration(req.getDuration())
            .numPeople(req.getNumPeople())
            .responsibleName(req.getResponsibleName())
            .responsibleEmail(req.getResponsibleEmail())
            .guests(guests.stream()
                        .map(g -> new GuestEmbeddable(g.getName(), g.getEmail()))
                        .collect(Collectors.toList()))
            .status(ReservaStatus.CONFIRMED)
            .totalPrice(pricing.getTotalAmount())
            .discountGroup(pricing.getGroupDiscount())
            .discountFreq(pricing.getFrequencyDiscount())
            .discountBirthday(pricing.getBirthdayDiscount())
            .build();
        when(reservaRepository.save(any())).thenReturn(saved);

        ComprobanteEntity comp = ComprobanteEntity.builder().id(7L).build();
        when(comprobanteService.generarComprobante(saved)).thenReturn(comp);
        doNothing().when(comprobanteService).enviarComprobantePorEmail(comp.getId());

        ReservaResponseDto resp = reservaService.createReservation(req);

        assertThat(resp.getId()).isEqualTo(5L);
        assertThat(resp.getResponsibleName()).isEqualTo("John");
        assertThat(resp.getKartIds()).containsExactly("K1","K2");
        assertThat(resp.getTotalAmount()).isEqualTo(BigDecimal.valueOf(100.0));
        assertThat(resp.getGroupDiscount()).isEqualTo(BigDecimal.valueOf(1.0));
        assertThat(resp.getGuests())
            .extracting(GuestDto::getName)
            .containsExactly("g1","g2");

        verify(availabilityService).registerReservation(saved);
        verify(comprobanteService).enviarComprobantePorEmail(comp.getId());
    }

    @Test
    @DisplayName("getReservationsBetweenDates mapea correctamente las entidades")
    void testGetReservationsBetweenDates() {
        OffsetDateTime start = now;
        OffsetDateTime end = now.plusDays(1);

        ReservaEntity e1 = ReservaEntity.builder()
            .id(10L)
            .status(ReservaStatus.CONFIRMED)
            .totalPrice(BigDecimal.valueOf(50.0))
            .discountBirthday(BigDecimal.valueOf(1.0))
            .discountFreq(BigDecimal.valueOf(2.0))
            .discountGroup(BigDecimal.valueOf(3.0))
            .build();
        ReservaEntity e2 = ReservaEntity.builder()
            .id(11L)
            .status(ReservaStatus.PENDING)
            .totalPrice(BigDecimal.valueOf(60.0))
            .discountBirthday(BigDecimal.valueOf(4.0))
            .discountFreq(BigDecimal.valueOf(5.0))
            .discountGroup(BigDecimal.valueOf(6.0))
            .build();

        when(reservaRepository.findByStartTimeBetween(start, end))
            .thenReturn(List.of(e1, e2));
        ReservaResponseDto dto1 = ReservaResponseDto.builder().build();
        ReservaResponseDto dto2 = ReservaResponseDto.builder().build();
        when(modelMapper.map(e1, ReservaResponseDto.class)).thenReturn(dto1);
        when(modelMapper.map(e2, ReservaResponseDto.class)).thenReturn(dto2);

        List<ReservaResponseDto> result = reservaService.getReservationsBetweenDates(start, end);
        assertThat(result).containsExactly(dto1, dto2);
        assertThat(dto1.getStatus()).isEqualTo("CONFIRMED");
        assertThat(dto1.getTotalAmount()).isEqualTo(BigDecimal.valueOf(50.0));
        assertThat(dto2.getStatus()).isEqualTo("PENDING");
    }

    @Test
    @DisplayName("getKartAvailability retorna totales y disponibles")
    void testGetKartAvailability() {
        OffsetDateTime s = now;
        OffsetDateTime e = now.plusHours(2);

        when(kartService.getAllKartIds()).thenReturn(List.of("K1","K2","K3"));
        when(availabilityService.getFreeKarts(s, e))
            .thenReturn(List.of("K1","K2"));

        KartAvailabilityResponseDto res = reservaService.getKartAvailability(s, e);
        assertThat(res.getTotalKarts()).isEqualTo(3);
        assertThat(res.getAvailableKarts()).isEqualTo(2);
        assertThat(res.getTime()).isEqualTo(s);
    }

    @Test
    @DisplayName("cancelReservation lanza NOT_FOUND si no existe")
    void testCancelReservationNotFound() {
        when(reservaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservaService.cancelReservation(99L))
            .isInstanceOf(ResponseStatusException.class)
            .extracting("status").isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("cancelReservation lanza BAD_REQUEST si ya está cancelada")
    void testCancelReservationAlreadyCancelled() {
        ReservaEntity res = ReservaEntity.builder()
            .id(20L)
            .status(ReservaStatus.CANCELLED)
            .build();
        when(reservaRepository.findById(20L)).thenReturn(Optional.of(res));

        assertThatThrownBy(() -> reservaService.cancelReservation(20L))
            .isInstanceOf(ResponseStatusException.class)
            .extracting("status").isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("cancelReservation éxito y mapea DTO correctamente")
    void testCancelReservationSuccess() {
        List<GuestEmbeddable> guests = List.of(
            new GuestEmbeddable("g","e")
                                              );
        ReservaEntity res = ReservaEntity.builder()
            .id(30L)
            .status(ReservaStatus.CONFIRMED)
            .totalPrice(BigDecimal.valueOf(200.0))
            .discountBirthday(BigDecimal.valueOf(10.0))
            .discountFreq(BigDecimal.valueOf(20.0))
            .discountGroup(BigDecimal.valueOf(30.0))
            .guests(guests)
            .build();

        when(reservaRepository.findById(30L)).thenReturn(Optional.of(res));
        when(reservaRepository.save(res)).thenReturn(res);
        ReservaResponseDto mapped = ReservaResponseDto.builder().build();
        when(modelMapper.map(res, ReservaResponseDto.class)).thenReturn(mapped);

        ReservaResponseDto dto = reservaService.cancelReservation(30L);
        assertThat(dto.getStatus()).isEqualTo("CANCELLED");
        assertThat(dto.getTotalAmount()).isEqualTo(BigDecimal.valueOf(200.0));
        assertThat(dto.getBirthdayDiscount()).isEqualTo(BigDecimal.valueOf(10.0));
        assertThat(dto.getGuests())
            .extracting(GuestDto::getName)
            .containsExactly("g");

        verify(availabilityService).removeReservation(res);
    }
}
