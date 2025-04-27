package tingeso.karting.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tingeso.karting.DTO.PricingResponseDto;
import tingeso.karting.DTO.ReservaRequestDto;
import tingeso.karting.DTO.ReservaResponseDto;
import tingeso.karting.services.ReservaService;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservaController {
    private final ReservaService service;

    @PostMapping("/check")
    public ResponseEntity<PricingResponseDto> check(@RequestBody ReservaRequestDto req) {
        return ResponseEntity.ok(service.checkAvailability(req));
    }

    @PostMapping
    public ResponseEntity<List<ReservaResponseDto>> create(@RequestBody  ReservaRequestDto req) {
        List<ReservaResponseDto> creado = service.createReservations(req);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }
}
