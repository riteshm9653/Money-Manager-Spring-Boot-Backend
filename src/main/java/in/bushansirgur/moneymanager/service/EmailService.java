package in.bushansirgur.moneymanager.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    
  @Value("${spring.mail.properties.mail.smtp.from}")
    private String fromEmail;

    public void sendEmail(String to, String subject, String body) {
        try {
            
            log.info("📧 Sending email from: {} to: {}", fromEmail, to);
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("✅ Email sent successfully to: {}", to);
        }catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void sendEmailWithAttachment(String to, String subject, String body, byte[] attachment, String filename) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body);
        helper.addAttachment(filename, new ByteArrayResource(attachment));
        mailSender.send(message);
        log.info("✅ Email with attachment sent to: {}", to);
    }

    public void sendContactEmail(String name, String userEmail, String subject, String message)
            throws MessagingException {

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

        // You will receive the email
        helper.setTo("riteshm9052@gmail.com");          // ← Your email
        helper.setFrom(fromEmail);                      // Must be a verified sender in Brevo
        helper.setReplyTo(userEmail);                   // So you can reply directly to the user
        helper.setSubject(subject != null && !subject.isBlank() 
                ? "[Contact] " + subject 
                : "[Contact] New message from " + name);

        String htmlContent = """
                <div style="font-family: Arial, sans-serif; max-width: 600px;">
                    <h2 style="color: #7c3aed;">New Contact Form Submission</h2>
                    <p><strong>Name:</strong> %s</p>
                    <p><strong>Email:</strong> %s</p>
                    <p><strong>Subject:</strong> %s</p>
                    <hr>
                    <p><strong>Message:</strong></p>
                    <p style="white-space: pre-line;">%s</p>
                </div>
                """.formatted(name, userEmail, 
                              subject != null ? subject : "No subject", 
                              message);

        helper.setText(htmlContent, true); // true = HTML

        mailSender.send(mimeMessage);
    }
}
