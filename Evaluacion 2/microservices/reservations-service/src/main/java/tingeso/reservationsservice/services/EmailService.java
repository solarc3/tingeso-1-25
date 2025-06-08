package tingeso.reservationsservice.services;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void enviarEmailConAdjunto(String destinatario, String asunto, String cuerpo,
                                      String nombreArchivo, byte[] archivo) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(cuerpo, true); // true indica que el cuerpo es HTML

            ByteArrayResource resource = new ByteArrayResource(archivo);
            helper.addAttachment(nombreArchivo, resource);

            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Error al enviar email: " + e.getMessage());
        }
    }
}