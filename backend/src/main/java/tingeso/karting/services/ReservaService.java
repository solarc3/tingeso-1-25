package tingeso.karting.services;


import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tingeso.karting.DTO.*;
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
    //TODO: revisar ya que se asume 1 kart en la reserva request pero eso no se sabe, se debe calcular
    // en base a la cantidad de gente que hay
    // requiere cambio de logica en availService y pricingService ya que se hacen los streams correspondientes
    public PricingResponseDto checkAvailability(ReservaRequestDto req) {
        AvailabilityRequestDto aReq = AvailabilityRequestDto.builder()
            .startTime(req.getStartTime())
            .endTime(req.getEndTime())
            .numKarts(req.getNumPeople())
            .build();
        AvailabilityResponseDto avail = availabilityService.checkAvail(aReq);
        if (!avail.isOk()) {
            //tira 409
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "No hay disponibilidad para esa franja horaria"
            );        }
        PricingRequestDto pReq = modelMapper.map(req, PricingRequestDto.class);
        return pricingService.calculatePrice(pReq);
    }

    public List<ReservaResponseDto> createReservations(ReservaRequestDto req) {
        // Verificar disponibilidad y obtener pricing
        PricingResponseDto pricing = checkAvailability(req);
        List<String> free = availabilityService.getFreeKarts(req.getStartTime(), req.getEndTime());
        int needed = req.getNumPeople();
        List<String> assigned = free.subList(0, needed);

        List<ReservaResponseDto> responses = new ArrayList<>();
        for (String kartId : assigned) {
            ReservaEntity entity = ReservaEntity.builder()
                .startTime(req.getStartTime())
                .endTime(req.getEndTime())
                .kartId(kartId)
                .numPeople(req.getNumPeople())
                .status(ReservaStatus.CONFIRMED)
                .totalPrice(pricing.getTotalAmount())
                .discountGroup(pricing.getGroupDiscount())
                .discountFreq(pricing.getFrequencyDiscount())
                .discountBirthday(pricing.getBirthdayDiscount())
                .build();
            ReservaEntity saved = reservaRepository.save(entity);
            availabilityService.registerReservation(saved);

            ReservaResponseDto resp = modelMapper.map(saved, ReservaResponseDto.class);
            resp.setBaseRate(pricing.getBaseRate());
            resp.setGroupDiscount(pricing.getGroupDiscount());
            resp.setFrequencyDiscount(pricing.getFrequencyDiscount());
            resp.setBirthdayDiscount(pricing.getBirthdayDiscount());
            resp.setTax(pricing.getTax());
            resp.setTotalAmount(pricing.getTotalAmount());
            responses.add(resp);
        }
        return responses;
    }

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
                return dto;
            })
            .collect(Collectors.toList());
    }}
