package tingeso.karting.services;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tingeso.karting.entities.ComprobanteEntity;
import tingeso.karting.entities.GuestEmbeddable;
import tingeso.karting.entities.ReservaEntity;
import tingeso.karting.repositories.ComprobanteRepository;
import tingeso.karting.repositories.ReservaRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ComprobanteService {
    private final ReservaRepository reservaRepository;
    private final ComprobanteRepository comprobanteRepository;
    private final EmailService emailService;
    private final PdfService pdfService;

    @Transactional
    public ComprobanteEntity generarComprobante(ReservaEntity reserva) {
        String codigoReserva = "RES-" + reserva.getId() + "-" + System.currentTimeMillis();

        String contenidoHtml = generarContenidoHtml(reserva);

        // Crear la entidad de comprobante
        ComprobanteEntity comprobante = ComprobanteEntity.builder()
            .codigoReserva(codigoReserva)
            .reserva(reserva)
            .fechaEmision(OffsetDateTime.now())
            .contenido(contenidoHtml)
            .enviado(false)
            .build();

        return comprobanteRepository.save(comprobante);
    }

    @Transactional
    public void enviarComprobantePorEmail(Long comprobanteId) {
        ComprobanteEntity comprobante = comprobanteRepository.findById(comprobanteId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Comprobante no encontrado"));

        ReservaEntity reserva = comprobante.getReserva();

        // Generar PDF del comprobante
        byte[] pdfBytes = pdfService.generarPdf(comprobante.getContenido());

        // Enviar email al responsable
        emailService.enviarEmailConAdjunto(
            reserva.getResponsibleEmail(),
            "Comprobante de Reserva - KartingRM",
            "Adjunto encontrarás el comprobante de tu reserva en KartingRM.",
            "comprobante.pdf",
            pdfBytes
                                          );

        // Enviar email a cada invitado
        for (GuestEmbeddable guest : reserva.getGuests()) {
            if (!guest.getEmail().equals(reserva.getResponsibleEmail())) {
                emailService.enviarEmailConAdjunto(
                    guest.getEmail(),
                    "Comprobante de Reserva - KartingRM",
                    "Has sido invitado a una sesión de karting. Adjunto encontrarás el comprobante de la reserva.",
                    "comprobante.pdf",
                    pdfBytes
                                                  );
            }
        }

        // Actualizar estado del comprobante
        comprobante.setEnviado(true);
        comprobante.setFechaEnvio(OffsetDateTime.now());
        comprobanteRepository.save(comprobante);
    }

    private String generarContenidoHtml(ReservaEntity reserva) {
        // Generar HTML para el comprobante según el formato requerido
        StringBuilder html = new StringBuilder();
        html.append("<html><head><style>");
        html.append("body { font-family: Arial, sans-serif; }");
        html.append("table { border-collapse: collapse; width: 100%; }");
        html.append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
        html.append("th { background-color: #f2f2f2; }");
        html.append("</style></head><body>");

        html.append("<h1>Comprobante de Reserva - KartingRM</h1>");
        html.append("<p>Código de Reserva: " + reserva.getId() + "</p>");
        html.append("<p>Fecha de emisión: " + OffsetDateTime.now().format(
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "</p>");

        // Información de la reserva
        html.append("<h2>Información de la Reserva</h2>");
        html.append("<p>Fecha: " + reserva.getStartTime().format(
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "</p>");
        html.append("<p>Responsable: " + reserva.getResponsibleName() + "</p>");
        html.append("<p>Email: " + reserva.getResponsibleEmail() + "</p>");
        html.append("<p>Número de personas: " + reserva.getNumPeople() + "</p>");

        // Tabla de participantes
        html.append("<h2>Detalle de Participantes</h2>");
        html.append("<table>");
        html.append("<tr><th>Nombre</th><th>Email</th><th>Tarifa Base</th><th>Descuento Grupo</th>");
        html.append("<th>Descuento Frecuencia</th><th>Descuento Cumpleaños</th><th>IVA</th><th>Total</th></tr>");

        // Calcular precios individuales
        BigDecimal tarifaBase = reserva.getTotalPrice().divide(
            BigDecimal.valueOf(reserva.getNumPeople()), 2, RoundingMode.HALF_UP);
        BigDecimal descuentoGrupo = BigDecimal.ZERO;
        BigDecimal descuentoFreq = BigDecimal.ZERO;
        BigDecimal descuentoBirthday = BigDecimal.ZERO;

        if (reserva.getDiscountGroup() != null) {
            descuentoGrupo = reserva.getDiscountGroup().divide(
                BigDecimal.valueOf(reserva.getNumPeople()), 2, RoundingMode.HALF_UP);
        }

        if (reserva.getDiscountFreq() != null) {
            descuentoFreq = reserva.getDiscountFreq().divide(
                BigDecimal.valueOf(reserva.getNumPeople()), 2, RoundingMode.HALF_UP);
        }

        if (reserva.getDiscountBirthday() != null) {
            descuentoBirthday = reserva.getDiscountBirthday().divide(
                BigDecimal.valueOf(reserva.getNumPeople()), 2, RoundingMode.HALF_UP);
        }

        // Iterar sobre los invitados
        for (GuestEmbeddable guest : reserva.getGuests()) {
            html.append("<tr>");
            html.append("<td>" + guest.getName() + "</td>");
            html.append("<td>" + guest.getEmail() + "</td>");
            html.append("<td>" + formatCurrency(tarifaBase) + "</td>");
            html.append("<td>" + formatCurrency(descuentoGrupo) + "</td>");
            html.append("<td>" + formatCurrency(descuentoFreq) + "</td>");
            html.append("<td>" + formatCurrency(descuentoBirthday) + "</td>");

            // Calcular IVA individual
            BigDecimal subtotal = tarifaBase.subtract(descuentoGrupo)
                .subtract(descuentoFreq)
                .subtract(descuentoBirthday);
            BigDecimal iva = subtotal.multiply(new BigDecimal("0.19")).setScale(2, RoundingMode.HALF_UP);
            BigDecimal total = subtotal.add(iva);

            html.append("<td>")
                .append(formatCurrency(iva))
                .append("</td>");
            html.append("<td>")
                .append(formatCurrency(total))
                .append("</td>");
            html.append("</tr>");
        }

        html.append("</table>");

        // Total general
        html.append("<h2>Total a Pagar</h2>");
        html.append("<p>Subtotal: ")
            .append(formatCurrency(reserva.getTotalPrice()
                                       .subtract(
                                           calcularIVA(reserva.getTotalPrice()))))
            .append("</p>");
        html.append("<p>IVA (19%): ")
            .append(formatCurrency(calcularIVA(reserva.getTotalPrice())))
            .append("</p>");
        html.append("<p>Total: " + formatCurrency(reserva.getTotalPrice()) + "</p>");

        html.append("<p><strong>Este comprobante debe ser presentado el día de su visita al kartódromo.</strong></p>");

        html.append("</body></html>");

        return html.toString();
    }

    private BigDecimal calcularIVA(BigDecimal monto) {
        return monto.multiply(new BigDecimal("0.19")).divide(new BigDecimal("1.19"), 2, RoundingMode.HALF_UP);
    }

    private String formatCurrency(BigDecimal amount) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "CL"));
        return currencyFormat.format(amount);
    }
}