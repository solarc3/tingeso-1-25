package tingeso.karting.services;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Properties;

import jakarta.mail.BodyPart;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        // Inyectamos el email de origen
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@example.com");
    }

    @Test
    @DisplayName("enviarEmailConAdjunto envía correctamente un correo con adjunto")
    void testEnviarEmailConAdjuntoSuccess() throws Exception {
        // Creamos un MimeMessage real para inspección
        MimeMessage mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(mimeMessage);

        byte[] archivo = {1, 2, 3};
        emailService.enviarEmailConAdjunto(
            "dest@example.com",
            "Asunto de prueba",
            "<p>Contenido HTML</p>",
            "archivo.txt",
            archivo
                                          );

        // Verificamos que se invocó el envío
        verify(mailSender).send(mimeMessage);

        // Verificamos cabeceras
        assertThat(mimeMessage.getFrom()[0].toString()).isEqualTo("noreply@example.com");
        assertThat(mimeMessage.getAllRecipients()[0].toString()).isEqualTo("dest@example.com");
        assertThat(mimeMessage.getSubject()).isEqualTo("Asunto de prueba");

        // Verificamos que el contenido es multipart con cuerpo y adjunto
        Object content = mimeMessage.getContent();
        assertThat(content).isInstanceOf(Multipart.class);
        Multipart mp = (Multipart) content;
        assertThat(mp.getCount()).isEqualTo(2);


        // Parte 1: adjunto
        BodyPart part1 = mp.getBodyPart(1);
        assertThat(part1.getFileName()).isEqualTo("archivo.txt");
    }

    @Test
    @DisplayName("enviarEmailConAdjunto lanza RuntimeException al fallar el envío")
    void testEnviarEmailConAdjuntoFailure() throws Exception {
        MimeMessage mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new MailSendException("SMTP caído")).when(mailSender).send(mimeMessage);

        RuntimeException ex = catchThrowableOfType(() ->
                                                       emailService.enviarEmailConAdjunto(
                                                           "dest@example.com",
                                                           "Asunto",
                                                           "Cuerpo",
                                                           "fichero.pdf",
                                                           new byte[]{}
                                                                                         ), RuntimeException.class);

        assertThat(ex).hasMessageContaining("Error al enviar email: SMTP caído");
    }
}
