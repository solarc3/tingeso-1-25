package tingeso.reportsservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tingeso.reportsservice.DTO.ReportResponseDto;
import tingeso.reportsservice.services.ReportsService;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class ReportsController {

    private final ReportsService reportsService;

    // REPORTS-SERVICE/?startDate=X&endDate=Y
    @GetMapping("/revenue-by-laps")
    public ResponseEntity<ReportResponseDto> getRevenueByLapsReport(
        @RequestParam("startDate") String startDate,
        @RequestParam("endDate") String endDate) {

        OffsetDateTime start = OffsetDateTime.parse(startDate);
        OffsetDateTime end = OffsetDateTime.parse(endDate);
        ReportResponseDto report = reportsService.getLapsByRevenueReport(start, end);

        return ResponseEntity.ok(report);
    }

    @GetMapping("/revenue-by-people")
    public ResponseEntity<ReportResponseDto> getRevenueByPeopleReport(
        @RequestParam("startDate") String startDate,
        @RequestParam("endDate") String endDate) {

        OffsetDateTime start = OffsetDateTime.parse(startDate);
        OffsetDateTime end = OffsetDateTime.parse(endDate);
        ReportResponseDto report = reportsService.getPeopleByRevenueReport(start, end);

        return ResponseEntity.ok(report);
    }
}