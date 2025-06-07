package tingeso.tariffsservice.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tingeso.tariffsservice.DTO.PricingRequestDto;
import tingeso.tariffsservice.DTO.PricingResponseDto;
import tingeso.tariffsservice.services.PricingService;


@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class ReservaController {
    private final PricingService pricingService;
// no deberia ser su responsadibilidad verificar disponibilidad si no se esta corriendo el reserva in mmeory data, es
    // problema del ms reservations, ms1 solo se preocupa de preocio y nada mas
    // mover este endpoint a ms 5
//    @PostMapping("/check")
//    public ResponseEntity<PricingResponseDto> checkAvailability(@RequestBody ReservaRequestDto req) {
//        System.out.print("tiempo respuesta");
//        System.out.print(req.getStartTime());
//        System.out.print(req.getEndTime());
//
//        PricingResponseDto pricing = service.checkAvailability(req);
//        return ResponseEntity.ok(pricing);
//    }

    @PostMapping("/calculate-price")
    public ResponseEntity<PricingResponseDto> calculatePrice(@RequestBody PricingRequestDto req) {
        PricingResponseDto calculated = pricingService.calculatePrice(req);
        return ResponseEntity.ok(calculated);
    }

}