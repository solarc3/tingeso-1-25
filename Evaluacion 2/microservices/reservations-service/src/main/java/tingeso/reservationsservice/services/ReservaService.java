package tingeso.reservationsservice.services;


import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import tingeso.reservationsservice.DTO.*;
import tingeso.reservationsservice.entities.ComprobanteEntity;
import tingeso.reservationsservice.entities.GuestEmbeddable;
import tingeso.reservationsservice.entities.ReservaEntity;
import tingeso.reservationsservice.entities.ReservaStatus;
import tingeso.reservationsservice.repositories.ReservaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservaService {
    private final AvailabilityService availabilityService;
    private final ReservaRepository reservaRepository;
    private final ModelMapper modelMapper;
    private final KartService kartService;
    private final ComprobanteService comprobanteService;
    private final RestTemplate restTemplate;

    @Value("${gateway.base.url}")
    private String gatewayBaseUrl;
    @Cacheable(value = "pricing", key = "#req.startTime.toString() + '_' + #req.endTime.toString() + '_' + #req.numPeople")
    public PricingResponseDto checkAvailability(ReservaRequestDto req) {
        AvailabilityRequestDto aReq = AvailabilityRequestDto.builder()
            .startTime(req.getStartTime())
            .endTime(req.getEndTime())
            .numKarts(req.getNumPeople())
            .build();
        AvailabilityResponseDto avail = availabilityService.checkAvail(aReq);
        if (!avail.isOk()) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "No hay disponibilidad para esa franja horaria"
            );
        }

        PricingRequestDto pReq = modelMapper.map(req, PricingRequestDto.class);
        //TODO: recibir info de tariffs-service ms1, ese maneja la logica de pricing
        return calculatePrice(pReq);
    }
    //recibir datos de ms1
    // endpoint /calculate-price
    private PricingResponseDto calculatePrice(PricingRequestDto pReq) {
        try{

            String url = gatewayBaseUrl + "/api/TARIFFS-SERVICE/calculate-price";
            return restTemplate.postForObject(url, pReq, PricingResponseDto.class);
        } catch (RestClientException e) {
            throw new RuntimeException(e);
        }
    }
    @CacheEvict(value = {"reservations", "availability"}, allEntries = true)
    public ReservaResponseDto createReservation(ReservaRequestDto req) {
        if (req.getGuests() == null || req.getGuests().size() != req.getNumPeople()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "La cantidad de invitados debe ser igual al número de karts");
        }

        PricingResponseDto pricing = checkAvailability(req);
        List<String> free = availabilityService.getFreeKarts(req.getStartTime(), req.getEndTime());
        if (free.size() < req.getNumPeople()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No hay suficientes karts libres");
        }

        List<String> assigned = free.subList(0, req.getNumPeople());
        ReservaEntity entity = ReservaEntity.builder()
            .startTime(req.getStartTime())
            .endTime(req.getEndTime())
            .kartIds(assigned)
            .laps(req.getLaps())
            .duration(req.getDuration())
            .numPeople(req.getNumPeople())
            .responsibleName(req.getResponsibleName())
            .responsibleEmail(req.getResponsibleEmail())
            .guests(req.getGuests()
                        .stream()
                        .map(g -> new GuestEmbeddable(g.getName(), g.getEmail()))
                        .collect(Collectors.toList()))
            .status(ReservaStatus.CONFIRMED)
            .totalPrice(pricing.getTotalAmount())
            .discountGroup(pricing.getGroupDiscount())
            .discountFreq(pricing.getFrequencyDiscount())
            .discountBirthday(pricing.getBirthdayDiscount())
            .build();

        ReservaEntity saved = reservaRepository.save(entity);
        System.out.println("Registrando nueva reserva ID " + saved.getId() + " en el servicio de disponibilidad");
        availabilityService.registerReservation(saved);
        ComprobanteEntity comprobante = comprobanteService.generarComprobante(saved);
        comprobanteService.enviarComprobantePorEmail(comprobante.getId());

        return ReservaResponseDto.builder()
            .id(saved.getId())
            .responsibleName(saved.getResponsibleName())
            .responsibleEmail(saved.getResponsibleEmail())
            .startTime(saved.getStartTime())
            .endTime(saved.getEndTime())
            .kartIds(saved.getKartIds())
            .numPeople(saved.getNumPeople())
            .guests(req.getGuests())
            .baseRate(pricing.getBaseRate())
            .groupDiscount(pricing.getGroupDiscount())
            .frequencyDiscount(pricing.getFrequencyDiscount())
            .birthdayDiscount(pricing.getBirthdayDiscount())
            .tax(pricing.getTax())
            .totalAmount(pricing.getTotalAmount())
            .status(saved.getStatus().toString())
            .build();
    }
    @Cacheable(value = "reservations", key = "#startDate.toString() + '_' + #endDate.toString()")
    public List<ReservaResponseDto> getReservationsBetweenDates(OffsetDateTime startDate, OffsetDateTime endDate) {
        List<ReservaEntity> entities = reservaRepository.findByStartTimeBetween(startDate, endDate);
        return entities.stream()
            .map(entity -> {
                ReservaResponseDto dto = modelMapper.map(entity, ReservaResponseDto.class);
                dto.setStatus(entity.getStatus().toString());
                dto.setTotalAmount(entity.getTotalPrice());
                dto.setBirthdayDiscount(entity.getDiscountBirthday());
                dto.setFrequencyDiscount(entity.getDiscountFreq());
                dto.setGroupDiscount(entity.getDiscountGroup());
                return dto;
            })
            .collect(Collectors.toList());
    }

    @Deprecated
    public List<ReservaResponseDto> createReservations(ReservaRequestDto req) {
        return List.of(createReservation(req));
    }
    @Cacheable(value = "availability", key = "#startTime.toString() + '_' + #endTime.toString()")
    public KartAvailabilityResponseDto getKartAvailability(OffsetDateTime startTime, OffsetDateTime endTime) {
        System.out.println("Checking availability for period:");
        System.out.println("Start: " + startTime);
        System.out.println("End: " + endTime);

        List<String> allKarts = kartService.getAllKartIds();
        int totalKarts = allKarts.size();

        System.out.println("Total karts: " + totalKarts);

        List<String> freeKarts = availabilityService.getFreeKarts(startTime, endTime);

        System.out.println("Free karts: " + freeKarts.size());
        System.out.println("Free kart IDs: " + freeKarts);

        return KartAvailabilityResponseDto.builder()
            .time(startTime)
            .totalKarts(totalKarts)
            .availableKarts(freeKarts.size())
            .build();
    }
    @CacheEvict(value = {"reservations", "availability"}, allEntries = true)
    public ReservaResponseDto cancelReservation(Long id) {
        ReservaEntity reservation = reservaRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva no encontrada"));

        if (reservation.getStatus() == ReservaStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La reserva ya está cancelada");
        }

        reservation.setStatus(ReservaStatus.CANCELLED);

        availabilityService.removeReservation(reservation);

        ReservaEntity updated = reservaRepository.save(reservation);

        ReservaResponseDto dto = modelMapper.map(updated, ReservaResponseDto.class);
        dto.setStatus(updated.getStatus().toString());
        dto.setTotalAmount(updated.getTotalPrice());
        dto.setBirthdayDiscount(updated.getDiscountBirthday());
        dto.setFrequencyDiscount(updated.getDiscountFreq());
        dto.setGroupDiscount(updated.getDiscountGroup());

        dto.setGuests(updated.getGuests().stream()
                          .map(g -> new GuestDto(g.getName(), g.getEmail()))
                          .collect(Collectors.toList()));

        return dto;
    }
}
