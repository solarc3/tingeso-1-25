package tingeso.karting.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tingeso.karting.DTO.ReportResponseDto;
import tingeso.karting.services.ReportService;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/revenue-by-laps")
    public ResponseEntity<ReportResponseDto> getRevenueByLapsReport(
        @RequestParam("startDate") String startDate,
        @RequestParam("endDate") String endDate) {

        OffsetDateTime start = OffsetDateTime.parse(startDate);
        OffsetDateTime end = OffsetDateTime.parse(endDate);
        ReportResponseDto report = reportService.getLapsByRevenueReport(start, end);

        return ResponseEntity.ok(report);
    }

    @GetMapping("/revenue-by-people")
    public ResponseEntity<ReportResponseDto> getRevenueByPeopleReport(
        @RequestParam("startDate") String startDate,
        @RequestParam("endDate") String endDate) {

        OffsetDateTime start = OffsetDateTime.parse(startDate);
        OffsetDateTime end = OffsetDateTime.parse(endDate);
        ReportResponseDto report = reportService.getPeopleByRevenueReport(start, end);

        return ResponseEntity.ok(report);
    }
}