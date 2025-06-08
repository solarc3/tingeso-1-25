package tingeso.reportsservice.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tingeso.reportsservice.DTO.ReportEntryDto;
import tingeso.reportsservice.DTO.ReportResponseDto;
import tingeso.reportsservice.entities.ReservaEntity;
import tingeso.reportsservice.entities.ReservaStatus;

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
        //reservaRepository, instead call method from reservations microservice
        //List<ReservaEntity> allReservations = reservaRepository.findByStartTimeBetween(startDate, endDate);
        List<ReservaEntity> allReservations = getfindByStartTimeBetween(startDate, endDate);

        List<ReservaEntity> reservations = allReservations.stream()
            .filter(r -> r.getStatus() == ReservaStatus.CONFIRMED)
            .collect(Collectors.toList());
        Map<Integer, List<ReservaEntity>> reservationsByLaps = new HashMap<>();
        for (ReservaEntity reservation : reservations) {
            Integer laps = reservation.getLaps();
            if (laps == null) {
                if (reservation.getDuration() != null) {
                    if (reservation.getDuration() <= 30) laps = 10;
                    else if (reservation.getDuration() <= 35) laps = 15;
                    else laps = 20;
                } else {
                    continue;
                }
            }

            reservationsByLaps.computeIfAbsent(laps, k -> new ArrayList<>()).add(reservation);
        }

        // Calculate revenue for each category
        List<ReportEntryDto> entries = new ArrayList<>();
        for (Map.Entry<Integer, List<ReservaEntity>> entry : reservationsByLaps.entrySet()) {
            BigDecimal totalRevenue = BigDecimal.ZERO;
            for (ReservaEntity reservation : entry.getValue()) {
                totalRevenue = totalRevenue.add(reservation.getTotalPrice());
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

    private List<ReservaEntity> getfindByStartTimeBetween(OffsetDateTime startDate, OffsetDateTime endDate) {
        String url = gatewayBaseUrl + "/api/RESERVATIONS-SERVICE/?startDate={startDate}&endDate={endDate}";

        return restTemplate.getForObject(url, List.class, startDate, endDate);
    }

    public ReportResponseDto getPeopleByRevenueReport(OffsetDateTime startDate, OffsetDateTime endDate) {
        //List<ReservaEntity> allReservations = reservaRepository.findByStartTimeBetween(startDate, endDate);
        List<ReservaEntity> allReservations = getfindByStartTimeBetween(startDate, endDate);
        List<ReservaEntity> reservations = allReservations.stream()
            .filter(r -> r.getStatus() == ReservaStatus.CONFIRMED)
            .collect(Collectors.toList());

        Map<String, List<ReservaEntity>> reservationsByPeopleCategory = new HashMap<>();
        reservationsByPeopleCategory.put("1-2 personas", new ArrayList<>());
        reservationsByPeopleCategory.put("3-5 personas", new ArrayList<>());
        reservationsByPeopleCategory.put("6-10 personas", new ArrayList<>());
        reservationsByPeopleCategory.put("11-15 personas", new ArrayList<>());
        for (ReservaEntity reservation : reservations) {
            int numPeople = reservation.getNumPeople();
            String category;

            if (numPeople <= 2) category = "1-2 personas";
            else if (numPeople <= 5) category = "3-5 personas";
            else if (numPeople <= 10) category = "6-10 personas";
            else category = "11-15 personas";

            reservationsByPeopleCategory.get(category).add(reservation);
        }

        List<ReportEntryDto> entries = new ArrayList<>();
        for (Map.Entry<String, List<ReservaEntity>> entry : reservationsByPeopleCategory.entrySet()) {
            BigDecimal totalRevenue = BigDecimal.ZERO;
            for (ReservaEntity reservation : entry.getValue()) {
                totalRevenue = totalRevenue.add(reservation.getTotalPrice());
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