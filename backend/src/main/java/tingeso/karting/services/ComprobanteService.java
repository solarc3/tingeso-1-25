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
        html.append("body { font-family: Arial, sans-serif; font-size: 12px; }"); // Fuente más pequeña
        html.append("h1 { font-size: 18px; }"); // Título principal más pequeño
        html.append("h2 { font-size: 16px; }"); // Subtítulos más pequeños
        html.append("table { border-collapse: collapse; width: 100%; }");
        html.append("th, td { border: 1px solid #ddd; padding: 6px; text-align: left; font-size: 11px; }"); // Celdas más pequeñas
        html.append("th { background-color: #f2f2f2; }");
        html.append(".email-cell { word-break: break-word; }"); // Para emails largos
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
        html.append("<th>Tarifa</th>"); // Abreviado
        html.append("<th>Desc. Grupo</th>"); // Abreviado
        html.append("<th>Desc. Frec.</th>"); // Abreviado
        html.append("<th>Desc. Cumple</th>"); // Abreviado
        html.append("<th>IVA</th>");
        html.append("<th>Total</th>");
        html.append("</tr>");

        // Calcular precios individuales (sin cambios)
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

        // Iterar sobre los invitados (mismo código)
        for (GuestEmbeddable guest : reserva.getGuests()) {
            html.append("<tr>");
            html.append("<td>" + guest.getName() + "</td>");
            html.append("<td class='email-cell'>" + guest.getEmail() + "</td>");

            // Precio individual (ya con descuentos aplicados)
            BigDecimal totalIndividual = tarifaBase.subtract(descuentoGrupo)
                .subtract(descuentoFreq)
                .subtract(descuentoBirthday);

            // Calcular el subtotal (sin IVA) a partir del total
            BigDecimal subtotalIndividual = totalIndividual.divide(new BigDecimal("1.19"), 2, RoundingMode.HALF_UP);
            BigDecimal iva = totalIndividual.subtract(subtotalIndividual);

            html.append("<td>" + formatCurrency(tarifaBase) + "</td>");
            html.append("<td>" + formatCurrency(descuentoGrupo) + "</td>");
            html.append("<td>" + formatCurrency(descuentoFreq) + "</td>");
            html.append("<td>" + formatCurrency(descuentoBirthday) + "</td>");
            html.append("<td>" + formatCurrency(iva) + "</td>");
            html.append("<td>" + formatCurrency(totalIndividual) + "</td>");
            html.append("</tr>");
        }

        html.append("</table>");

        // Total general (sin cambios)
        html.append("<h2>Total a Pagar</h2>");
        html.append("<p>Subtotal: ")
            .append(formatCurrency(reserva.getTotalPrice()
                                       .subtract(calcularIVA(reserva.getTotalPrice()))))
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