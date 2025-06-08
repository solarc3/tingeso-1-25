package tingeso.reservationsservice.controller;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tingeso.reservationsservice.DTO.ComprobanteResponseDto;
import tingeso.reservationsservice.entities.ComprobanteEntity;
import tingeso.reservationsservice.repositories.ComprobanteRepository;
import tingeso.reservationsservice.services.ComprobanteService;
import tingeso.reservationsservice.services.PdfService;

import java.util.List;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class ComprobanteController {
    private final ComprobanteService comprobanteService;
    private final ModelMapper modelMapper;
    private final ComprobanteRepository comprobanteRepository;
    private final PdfService pdfService;

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
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> descargarComprobantePdf(@PathVariable Long id) {
        ComprobanteEntity comprobante = comprobanteRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Comprobante no encontrado"));

        byte[] pdfBytes = pdfService.generarPdf(comprobante.getContenido());

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=comprobante-" + comprobante.getCodigoReserva() + ".pdf");
        headers.setContentType(MediaType.APPLICATION_PDF);

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
    @GetMapping("/reserva/{reservaId}")
    public ResponseEntity<ComprobanteResponseDto> getComprobanteByReservaId(@PathVariable Long reservaId) {
        List<ComprobanteEntity> comprobantes = comprobanteRepository.findByReservaId(reservaId);

        if (comprobantes.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Comprobante no encontrado para esta reserva");
        }

        return ResponseEntity.ok(modelMapper.map(comprobantes.get(0), ComprobanteResponseDto.class));
    }
}