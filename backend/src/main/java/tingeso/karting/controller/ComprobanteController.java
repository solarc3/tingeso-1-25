package tingeso.karting.controller;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tingeso.karting.DTO.ComprobanteResponseDto;
import tingeso.karting.entities.ComprobanteEntity;
import tingeso.karting.repositories.ComprobanteRepository;
import tingeso.karting.services.ComprobanteService;

@RestController
@RequestMapping("/api/comprobantes")
@RequiredArgsConstructor
public class ComprobanteController {
    private final ComprobanteService comprobanteService;
    private final ModelMapper modelMapper;
    private final ComprobanteRepository comprobanteRepository;

    @GetMapping("/{id}")
    public ResponseEntity<ComprobanteResponseDto> getComprobante(@PathVariable Long id) {
        ComprobanteEntity comprobante = comprobanteRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Comprobante no encontrado"));

        return ResponseEntity.ok(modelMapper.map(comprobante, ComprobanteResponseDto.class));
    }

    @PostMapping("/enviar/{id}")
    public ResponseEntity<String> enviarComprobante(@PathVariable Long id) {
        comprobanteService.enviarComprobantePorEmail(id);
        return ResponseEntity.ok("Comprobante enviado correctamente");
    }
}