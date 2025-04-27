package tingeso.karting.services;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class PdfService {

    public byte[] generarPdf(String contenidoHtml) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            // Usando Flying Saucer con OpenHTMLtoPDF
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(contenidoHtml, null);
            builder.toStream(outputStream);
            builder.run();

            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF: " + e.getMessage());
        }
    }
}