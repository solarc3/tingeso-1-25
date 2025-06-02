package tingeso.tariffsservice.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tingeso.tariffsservice.DTO.KartAvailabilityResponseDto;
import tingeso.tariffsservice.DTO.PricingResponseDto;
import tingeso.tariffsservice.DTO.ReservaRequestDto;
import tingeso.tariffsservice.DTO.ReservaResponseDto;
import tingeso.tariffsservice.services.ReservaService;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class ReservaController {
    private final ReservaService service;

    @PostMapping("/check")
    public ResponseEntity<PricingResponseDto> checkAvailability(@RequestBody ReservaRequestDto req) {
        System.out.print("tiempo respuesta");
        System.out.print(req.getStartTime());
        System.out.print(req.getEndTime());

        PricingResponseDto pricing = service.checkAvailability(req);
        return ResponseEntity.ok(pricing);
    }


    @GetMapping
    public ResponseEntity<List<ReservaResponseDto>> getReservations(
        @RequestParam("startDate") String startDate,
        @RequestParam("endDate")   String endDate) {
        OffsetDateTime from = OffsetDateTime.parse(startDate);
        OffsetDateTime to   = OffsetDateTime.parse(endDate);
        List<ReservaResponseDto> list = service.getReservationsBetweenDates(from, to);
        return ResponseEntity.ok(list);
    }
    @GetMapping("/availability")
    public ResponseEntity<KartAvailabilityResponseDto> checkKartAvailability(
        @RequestParam("startTime") String startTime,
        @RequestParam("endTime") String endTime) {

        System.out.println("Received availability check request:");
        System.out.println("Start time: " + startTime);
        System.out.println("End time: " + endTime);

        OffsetDateTime start = OffsetDateTime.parse(startTime);
        OffsetDateTime end = OffsetDateTime.parse(endTime);
        KartAvailabilityResponseDto availability = service.getKartAvailability(start, end);

        System.out.println("Availability response: " + availability);
        return ResponseEntity.ok(availability);
    }
}