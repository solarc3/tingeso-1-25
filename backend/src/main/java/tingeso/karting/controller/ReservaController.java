package tingeso.karting.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tingeso.karting.DTO.PricingResponseDto;
import tingeso.karting.DTO.ReservaRequestDto;
import tingeso.karting.DTO.ReservaResponseDto;
import tingeso.karting.services.ReservaService;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservaController {
    private final ReservaService service;

    @PostMapping("/check")
    public ResponseEntity<PricingResponseDto> checkAvailability(
        @RequestBody ReservaRequestDto req
                                                               ) {
        PricingResponseDto pricing = service.checkAvailability(req);
        return ResponseEntity.ok(pricing);
    }

    /**
     * Crea una reserva con:
     *  – lista de kartIds,
     *  – responsable (nombre + email),
     *  – lista de invitados (name+email).
     */
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
}