package tingeso.karting.services;


import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tingeso.karting.DTO.*;
import tingeso.karting.entities.GuestEmbeddable;
import tingeso.karting.entities.ReservaEntity;
import tingeso.karting.entities.ReservaStatus;
import tingeso.karting.repositories.ReservaRepository;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservaService {
    private final AvailabilityService availabilityService;
    private final PricingService pricingService;
    private final ReservaRepository reservaRepository;
    private final ModelMapper modelMapper;

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

    // Nuevo método que devuelve un solo objeto en lugar de una lista
    public ReservaResponseDto createReservation(ReservaRequestDto req) {
        // chequeo de invitado/karts
        if (req.getGuests() == null || req.getGuests().size() != req.getNumPeople()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "La cantidad de invitados debe ser igual al número de karts");
        }

        // 1) Verificar disponibilidad y precio
        PricingResponseDto pricing = checkAvailability(req);
        List<String> free = availabilityService.getFreeKarts(req.getStartTime(), req.getEndTime());
        if (free.size() < req.getNumPeople()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No hay suficientes karts libres");
        }

        List<String> assigned = free.subList(0, req.getNumPeople());

        // 2) Crear la reserva con todos los karts en una sola entidad
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

        assigned.forEach(kartId ->
                             availabilityService.registerKartReservation(kartId, req.getStartTime(), req.getEndTime())
                        );

        // 4) Mapear a DTO de respuesta
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

    // Método existente para reservas entre fechas
    public List<ReservaResponseDto> getReservationsBetweenDates(OffsetDateTime startDate, OffsetDateTime endDate) {
        List<ReservaEntity> entities = reservaRepository.findByStartTimeBetween(startDate, endDate);
        return entities.stream()
            .map(entity -> {
                ReservaResponseDto dto = modelMapper.map(entity, ReservaResponseDto.class);
                // Asegúrate de que todos los campos necesarios estén mapeados
                dto.setStatus(entity.getStatus().toString());
                dto.setTotalAmount(entity.getTotalPrice());
                dto.setBirthdayDiscount(entity.getDiscountBirthday());
                dto.setFrequencyDiscount(entity.getDiscountFreq());
                dto.setGroupDiscount(entity.getDiscountGroup());
                // También deberíamos mapear los guests si hacemos findById
                return dto;
            })
            .collect(Collectors.toList());
    }

    // Mantén el método antiguo para compatibilidad - opcional
    // O puedes eliminarlo si estás seguro de que no se usa en ninguna parte
    @Deprecated
    public List<ReservaResponseDto> createReservations(ReservaRequestDto req) {
        return List.of(createReservation(req));
    }
}
