package tingeso.tariffsservice.services;


import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import tingeso.tariffsservice.DTO.*;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReservaService {
    private final PricingService pricingService;
    private final ModelMapper modelMapper;
    private final RestTemplate restTemplate;
    @Value("${gateway.base.url}")
    private String gatewayBaseUrl;

    public PricingResponseDto checkAvailability(ReservaRequestDto req) {
        AvailabilityRequestDto aReq = AvailabilityRequestDto.builder()
            .startTime(req.getStartTime())
            .endTime(req.getEndTime())
            .numKarts(req.getNumPeople())
            .build();
        //TODO: hacer que los datos lleguen del ms 5? no se usa en ningun lado mais
        AvailabilityResponseDto avail = getCheckAvail(aReq);
        if (!avail.isOk()) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "No hay disponibilidad para esa franja horaria"
            );
        }
        PricingRequestDto pReq = modelMapper.map(req, PricingRequestDto.class);
        return pricingService.calculatePrice(pReq);
    }

    private AvailabilityResponseDto getCheckAvail(AvailabilityRequestDto aReq) {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("startTime", aReq.getStartTime());
            request.put("endTime", aReq.getEndTime());
            request.put("numKarst", aReq.getNumKarts());
            request.put("duration", aReq.getDuration());

            String url = gatewayBaseUrl + "/api/RESERVATIONS-SERVICE/checkAvail";
            return restTemplate.postForObject(url, request, AvailabilityResponseDto.class);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

    }
}
