package tingeso.reservationsservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tingeso.reservationsservice.DTO.*;
import tingeso.reservationsservice.services.AvailabilityService;
import tingeso.reservationsservice.services.ReservaService;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
//@RequestMapping("/")
@RequiredArgsConstructor
public class ReservaController {
    private final ReservaService service;
    private final AvailabilityService availabilityService;

    @PostMapping
    public ResponseEntity<ReservaResponseDto> createReservation(
        @RequestBody ReservaRequestDto req
                                                               ) {
        ReservaResponseDto created = service.createReservation(req);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
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
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ReservaResponseDto> cancelReservation(@PathVariable Long id) {
        ReservaResponseDto cancelled = service.cancelReservation(id);
        return ResponseEntity.ok(cancelled);
    }
    //metodo para tarrifs-service
    @PostMapping("/checkAvail")
    public ResponseEntity<AvailabilityResponseDto> checkAvailCarts(@RequestBody AvailabilityRequestDto request){
        AvailabilityResponseDto returning = availabilityService.checkAvail(request);
        return ResponseEntity.ok(returning);
    }
}