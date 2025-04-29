package tingeso.karting.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import tingeso.karting.entities.ComprobanteEntity;
import tingeso.karting.entities.GuestEmbeddable;
import tingeso.karting.entities.ReservaEntity;
import tingeso.karting.entities.ReservaStatus;
import tingeso.karting.repositories.ComprobanteRepository;
import tingeso.karting.repositories.ReservaRepository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ComprobanteServiceTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private ComprobanteRepository comprobanteRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PdfService pdfService;

    @InjectMocks
    private ComprobanteService comprobanteService;

    private ReservaEntity reserva;
    private ComprobanteEntity comprobante;
    private List<GuestEmbeddable> guests;

    @BeforeEach
    void setUp() {
        // Crear lista de invitados
        guests = new ArrayList<>();
        guests.add(new GuestEmbeddable("Juan Pérez", "juan@example.com"));
        guests.add(new GuestEmbeddable("Ana López", "ana@example.com"));

        // Crear reserva de prueba
        reserva = ReservaEntity.builder()
            .id(1L)
            .responsibleName("Juan Pérez")
            .responsibleEmail("juan@example.com")
            .startTime(OffsetDateTime.now().plusDays(1))
            .endTime(OffsetDateTime.now().plusDays(1).plusMinutes(30))
            .kartIds(List.of("K001", "K002"))
            .numPeople(2)
            .guests(guests)
            .status(ReservaStatus.CONFIRMED)
            .totalPrice(BigDecimal.valueOf(17850))
            .discountGroup(BigDecimal.valueOf(1500))
            .discountFreq(BigDecimal.ZERO)
            .discountBirthday(BigDecimal.ZERO)
            .build();

        // Crear comprobante de prueba
        comprobante = ComprobanteEntity.builder()
            .id(1L)
            .codigoReserva("RES-1-123456789")
            .reserva(reserva)
            .fechaEmision(OffsetDateTime.now())
            .contenido("<html><body>Comprobante de prueba</body></html>")
            .enviado(false)
            .build();
    }

    @Test
    @DisplayName("Debería generar comprobante correctamente")
    void shouldGenerateComprobante() {
        when(comprobanteRepository.save(any(ComprobanteEntity.class))).thenReturn(comprobante);

        ComprobanteEntity result = comprobanteService.generarComprobante(reserva);

        assertThat(result).isNotNull();
        assertThat(result.getReserva()).isEqualTo(reserva);
        assertThat(result.getCodigoReserva()).isNotNull();
        assertThat(result.getContenido()).isNotNull();
        assertThat(result.getEnviado()).isFalse();

        verify(comprobanteRepository).save(any(ComprobanteEntity.class));
    }

    @Test
    @DisplayName("Debería enviar comprobante por email correctamente")
    void shouldSendComprobanteByEmail() {
        when(comprobanteRepository.findById(1L)).thenReturn(Optional.of(comprobante));
        when(pdfService.generarPdf(anyString())).thenReturn(new byte[100]);

        comprobanteService.enviarComprobantePorEmail(1L);

        // Verificar que se envió el email al responsable
        verify(emailService).enviarEmailConAdjunto(
            eq("juan@example.com"),
            anyString(),
            anyString(),
            eq("comprobante.pdf"),
            any(byte[].class)
                                                  );

        // Verificar que se envió el email a cada invitado (excepto al responsable)
        verify(emailService).enviarEmailConAdjunto(
            eq("ana@example.com"),
            anyString(),
            anyString(),
            eq("comprobante.pdf"),
            any(byte[].class)
                                                  );

        // Verificar que se actualizó el comprobante
        verify(comprobanteRepository).save(argThat(comp ->
                                                       comp.getEnviado() && comp.getFechaEnvio() != null
                                                  ));
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando el comprobante no existe")
    void shouldThrowExceptionWhenComprobanteNotFound() {
        when(comprobanteRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> comprobanteService.enviarComprobantePorEmail(999L))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("no encontrado");
    }
}