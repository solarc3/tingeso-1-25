package tingeso.karting.services;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PdfServiceTest {

    private final PdfService pdfService = new PdfService();

    @Test
    @DisplayName("generarPdf devuelve un arreglo de bytes que comienza con el encabezado PDF")
    void testGenerarPdfSuccess() {
        String html = "<html><body><h1>Prueba PDF</h1><p>Contenido de ejemplo.</p></body></html>";
        byte[] pdfBytes = pdfService.generarPdf(html);

        assertThat(pdfBytes)
            .isNotNull()
            .isNotEmpty();
        String header = new String(pdfBytes, 0, 4);
        assertThat(header).isEqualTo("%PDF");
    }

    @Test
    @DisplayName("generarPdf lanza RuntimeException si el contenido HTML es inválido")
    void testGenerarPdfFailure() {
        RuntimeException ex = catchThrowableOfType(() ->
                                                       pdfService.generarPdf(null),
                                                   RuntimeException.class);

        assertThat(ex)
            .hasMessageContaining("Error generando PDF");
    }
}
