package tingeso.trackscheduleservice.services;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tingeso.trackscheduleservice.DTO.ReservaResponseDto;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class TrackScheduleService {

    private final RestTemplate restTemplate;

    @Value("${gateway.base.url}")
    private String gatewayBaseUrl;

    public TrackScheduleService(RestTemplate restTemplate) {this.restTemplate = restTemplate;}

    public List<ReservaResponseDto> getReservationsBetweenDates(OffsetDateTime startDate, OffsetDateTime endDate) {
        return reservationsFromService(startDate, endDate);

    }

    private List<ReservaResponseDto> reservationsFromService(OffsetDateTime startDate, OffsetDateTime endDate) {
        String url = gatewayBaseUrl + "/api/RESERVATIONS-SERVICE/?startDate={startDate}&endDate={endDate}";

        return restTemplate.getForObject(url, List.class, startDate, endDate);
    }
}
