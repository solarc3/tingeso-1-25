package tingeso.tariffsservice.services;


import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import tingeso.tariffsservice.DTO.*;

import tingeso.tariffsservice.entities.ReservaEntity;
import tingeso.tariffsservice.repositories.ReservaRepository;

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
}
