package tingeso.karting.services;


import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tingeso.karting.DTO.*;
import tingeso.karting.entities.ComprobanteEntity;
import tingeso.karting.entities.GuestEmbeddable;
import tingeso.karting.entities.ReservaEntity;
import tingeso.karting.entities.ReservaStatus;
import tingeso.karting.repositories.ReservaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservaService {
    private final AvailabilityService availabilityService;
    private final PricingService pricingService;
    private final ReservaRepository reservaRepository;
    private final ModelMapper modelMapper;
    private final KartService kartService;
    private final ComprobanteService comprobanteService;
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
        return pricingService.calculatePrice(pReq);
    }

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

        ReservaResponseDto resp = ReservaResponseDto.builder()
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

        return resp;
    }

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
