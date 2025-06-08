package tingeso.reportsservice.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tingeso.reportsservice.DTO.ReportEntryDto;
import tingeso.reportsservice.DTO.ReportResponseDto;
import tingeso.reportsservice.entities.ReservaEntity;
import tingeso.reportsservice.entities.ReservaStatus;
import tingeso.reportsservice.DTO.ReservaResponseDto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportsService {
    @Value("${gateway.base.url}")
    private String gatewayBaseUrl;

    private final RestTemplate restTemplate;

    public ReportResponseDto getLapsByRevenueReport(OffsetDateTime startDate, OffsetDateTime endDate) {
        List<ReservaResponseDto> allReservations = getfindByStartTimeBetween(startDate, endDate);

        List<ReservaResponseDto> reservations = allReservations.stream()
            .filter(r -> {
                try {
                    return ReservaStatus.valueOf(r.getStatus()) == ReservaStatus.CONFIRMED;
                } catch (IllegalArgumentException e) {
                    return false; // Handle invalid status values
                }
            })
            .collect(Collectors.toList());

        Map<Integer, List<ReservaResponseDto>> reservationsByLaps = new HashMap<>();
        for (ReservaResponseDto reservation : reservations) {
            Integer laps = null;
            if (reservation.getStartTime() != null && reservation.getEndTime() != null) {
                long durationMinutes = java.time.Duration.between(
                    reservation.getStartTime(),
                    reservation.getEndTime()
                                                                 ).toMinutes();

                // Convert duration to laps based on your business logic
                if (durationMinutes <= 30) laps = 10;
                else if (durationMinutes <= 35) laps = 15;
                else laps = 20;
            } else {
                // If we can't calculate duration, skip this reservation
                continue;
            }

            reservationsByLaps.computeIfAbsent(laps, k -> new ArrayList<>()).add(reservation);
        }

        List<ReportEntryDto> entries = new ArrayList<>();
        for (Map.Entry<Integer, List<ReservaResponseDto>> entry : reservationsByLaps.entrySet()) {
            BigDecimal totalRevenue = BigDecimal.ZERO;
            for (ReservaResponseDto reservation : entry.getValue()) {
                BigDecimal price = reservation.getTotalAmount();
                if (price != null) {
                    totalRevenue = totalRevenue.add(price);
                }
            }

            entries.add(ReportEntryDto.builder()
                            .category(entry.getKey() + " vueltas")
                            .count(entry.getValue().size())
                            .totalRevenue(totalRevenue)
                            .build());
        }

        return ReportResponseDto.builder()
            .reportTitle("Ingresos por Número de Vueltas")
            .entries(entries)
            .build();
    }

    private List<ReservaResponseDto> getfindByStartTimeBetween(OffsetDateTime startDate, OffsetDateTime endDate) {
        try {
            String url = gatewayBaseUrl + "/api/RESERVATIONS-SERVICE/?startDate={startDate}&endDate={endDate}";

            ResponseEntity<List<ReservaResponseDto>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<ReservaResponseDto>>() {},
                startDate, endDate
                                                                                     );

            return response.getBody() != null ? response.getBody() : new ArrayList<>();
        } catch (Exception e) {
            // Log the error
            System.err.println("Error calling reservations service: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public ReportResponseDto getPeopleByRevenueReport(OffsetDateTime startDate, OffsetDateTime endDate) {
        List<ReservaResponseDto> allReservations = getfindByStartTimeBetween(startDate, endDate);
        List<ReservaResponseDto> reservations = allReservations.stream()
            .filter(r -> {
                try {
                    return ReservaStatus.valueOf(r.getStatus()) == ReservaStatus.CONFIRMED;
                } catch (IllegalArgumentException e) {
                    return false; // Handle invalid status values
                }
            })
            .collect(Collectors.toList());

        Map<String, List<ReservaResponseDto>> reservationsByPeopleCategory = new HashMap<>();
        reservationsByPeopleCategory.put("1-2 personas", new ArrayList<>());
        reservationsByPeopleCategory.put("3-5 personas", new ArrayList<>());
        reservationsByPeopleCategory.put("6-10 personas", new ArrayList<>());
        reservationsByPeopleCategory.put("11-15 personas", new ArrayList<>());

        for (ReservaResponseDto reservation : reservations) {
            int numPeople = reservation.getNumPeople();
            String category;

            if (numPeople <= 2) category = "1-2 personas";
            else if (numPeople <= 5) category = "3-5 personas";
            else if (numPeople <= 10) category = "6-10 personas";
            else category = "11-15 personas";

            reservationsByPeopleCategory.get(category).add(reservation);
        }

        List<ReportEntryDto> entries = new ArrayList<>();
        for (Map.Entry<String, List<ReservaResponseDto>> entry : reservationsByPeopleCategory.entrySet()) {
            BigDecimal totalRevenue = BigDecimal.ZERO;
            for (ReservaResponseDto reservation : entry.getValue()) {
                BigDecimal price = reservation.getTotalAmount();
                if (price != null) {  // Add null check here
                    totalRevenue = totalRevenue.add(price);
                }
            }

            entries.add(ReportEntryDto.builder()
                            .category(entry.getKey())
                            .count(entry.getValue().size())
                            .totalRevenue(totalRevenue)
                            .build());
        }

        return ReportResponseDto.builder()
            .reportTitle("Ingresos por Número de Personas")
            .entries(entries)
            .build();
    }
}