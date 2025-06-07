package tingeso.tariffsservice.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tingeso.tariffsservice.DTO.PricingResponseDto;
import tingeso.tariffsservice.DTO.ReservaRequestDto;
import tingeso.tariffsservice.services.ReservaService;


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

}