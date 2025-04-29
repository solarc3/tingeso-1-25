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
        comprobante.setEnviado(true);
        comprobante.setFechaEnvio(OffsetDateTime.now());
        comprobanteRepository.save(comprobante);
    }

    private String generarContenidoHtml(ReservaEntity reserva) {
        StringBuilder html = new StringBuilder();
        html.append("<html><head><style>");
        html.append("body { font-family: Arial, sans-serif; font-size: 12px; }");
        html.append("h1 { font-size: 18px; }");
        html.append("h2 { font-size: 16px; }");
        html.append("table { border-collapse: collapse; width: 100%; }");
        html.append("th, td { border: 1px solid #ddd; padding: 6px; text-align: left; font-size: 11px; }");
        html.append("th { background-color: #f2f2f2; }");
        html.append(".email-cell { word-break: break-word; }");
        html.append("</style></head><body>");

        html.append("<h1>Comprobante de Reserva - KartingRM</h1>");
        html.append("<p>Código de Reserva: " + reserva.getId() + "</p>");
        html.append("<p>Fecha de emisión: " + OffsetDateTime.now().format(
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "</p>");

        html.append("<h2>Información de la Reserva</h2>");
        html.append("<p>Fecha: " + reserva.getStartTime().format(
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "</p>");
        html.append("<p>Responsable: " + reserva.getResponsibleName() + "</p>");
        html.append("<p>Email: " + reserva.getResponsibleEmail() + "</p>");
        html.append("<p>Número de personas: " + reserva.getNumPeople() + "</p>");

        // Tabla con cabeceras ligeramente abreviadas
        html.append("<h2>Detalle de Participantes</h2>");
        html.append("<table>");
        html.append("<tr>");
        html.append("<th>Nombre</th>");
        html.append("<th class='email-cell'>Email</th>");
        html.append("<th>Tarifa</th>");
        html.append("<th>Desc. Grupo</th>");
        html.append("<th>Desc. Frec.</th>");
        html.append("<th>Desc. Cumple</th>");
        html.append("<th>IVA</th>");
        html.append("<th>Total</th>");
        html.append("</tr>");

        BigDecimal precioBaseTotal = reserva.getTotalPrice();
        BigDecimal totalDescuentos = BigDecimal.ZERO;

        if (reserva.getDiscountGroup() != null) {
            totalDescuentos = totalDescuentos.add(reserva.getDiscountGroup());
        }
        if (reserva.getDiscountFreq() != null) {
            totalDescuentos = totalDescuentos.add(reserva.getDiscountFreq());
        }
        if (reserva.getDiscountBirthday() != null) {
            totalDescuentos = totalDescuentos.add(reserva.getDiscountBirthday());
        }

        BigDecimal precioSinIva = precioBaseTotal.divide(new BigDecimal("1.19"), 2, RoundingMode.HALF_UP);
        BigDecimal totalIva = precioBaseTotal.subtract(precioSinIva);

        BigDecimal precioBaseTotalSinDescuentos = precioSinIva.add(totalDescuentos);
        BigDecimal tarifaBaseIndividual = precioBaseTotalSinDescuentos.divide(
            BigDecimal.valueOf(reserva.getNumPeople()), 2, RoundingMode.HALF_UP);

        BigDecimal descuentoGrupoIndividual = BigDecimal.ZERO;
        BigDecimal descuentoFreqIndividual = BigDecimal.ZERO;
        BigDecimal descuentoBirthdayIndividual = BigDecimal.ZERO;

        if (reserva.getDiscountGroup() != null && reserva.getDiscountGroup().compareTo(BigDecimal.ZERO) > 0) {
            descuentoGrupoIndividual = reserva.getDiscountGroup().divide(
                BigDecimal.valueOf(reserva.getNumPeople()), 2, RoundingMode.HALF_UP);
        }

        if (reserva.getDiscountFreq() != null && reserva.getDiscountFreq().compareTo(BigDecimal.ZERO) > 0) {
            descuentoFreqIndividual = reserva.getDiscountFreq().divide(
                BigDecimal.valueOf(reserva.getNumPeople()), 2, RoundingMode.HALF_UP);
        }

        if (reserva.getDiscountBirthday() != null && reserva.getDiscountBirthday().compareTo(BigDecimal.ZERO) > 0) {
            descuentoBirthdayIndividual = reserva.getDiscountBirthday().divide(
                BigDecimal.valueOf(reserva.getNumPeople()), 2, RoundingMode.HALF_UP);
        }

        BigDecimal ivaIndividual = totalIva.divide(
            BigDecimal.valueOf(reserva.getNumPeople()), 2, RoundingMode.HALF_UP);
        BigDecimal totalIndividual = precioBaseTotal.divide(
            BigDecimal.valueOf(reserva.getNumPeople()), 2, RoundingMode.HALF_UP);

        // Iterar sobre los invitados
        for (GuestEmbeddable guest : reserva.getGuests()) {
            html.append("<tr>");
            html.append("<td>" + guest.getName() + "</td>");
            html.append("<td class='email-cell'>" + guest.getEmail() + "</td>");
            html.append("<td>" + formatCurrency(tarifaBaseIndividual) + "</td>");
            html.append("<td>" + formatCurrency(descuentoGrupoIndividual) + "</td>");
            html.append("<td>" + formatCurrency(descuentoFreqIndividual) + "</td>");
            html.append("<td>" + formatCurrency(descuentoBirthdayIndividual) + "</td>");
            html.append("<td>" + formatCurrency(ivaIndividual) + "</td>");
            html.append("<td>" + formatCurrency(totalIndividual) + "</td>");
            html.append("</tr>");
        }

        html.append("</table>");

        // Total general (sin cambios)
        html.append("<h2>Total a Pagar</h2>");
        html.append("<p>Subtotal: ")
            .append(formatCurrency(precioSinIva))
            .append("</p>");
        html.append("<p>IVA (19%): ")
            .append(formatCurrency(totalIva))
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