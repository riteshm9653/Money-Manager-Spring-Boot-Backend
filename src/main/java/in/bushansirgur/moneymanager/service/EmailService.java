package in.bushansirgur.moneymanager.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import sendinblue.ApiClient;
import sendinblue.ApiException;
import sendinblue.auth.ApiKeyAuth;
import org.springframework.web.client.RestTemplate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import sibModel.CreateSmtpEmail;
import sibModel.SendSmtpEmail;
import sibModel.SendSmtpEmailSender;
import sibModel.SendSmtpEmailTo;
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

       private final RestTemplate restTemplate = new RestTemplate();


  @Value("${spring.mail.properties.mail.smtp.from}")
    private String fromEmail;

    public void sendEmails(String to, String subject, String body) {
    try {
        log.info("📧 Sending email from: {} to: {}", fromEmail, to);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);

        log.info("✅ Email sent successfully to: {}", to);
    } catch (Exception e) {
        // This will print the FULL error in Render logs
        log.error("❌ Failed to send email to {}: {}", to, e.getMessage(), e);
        throw new RuntimeException("Failed to send activation email: " + e.getMessage(), e);
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

    @Value("${BREVO_PASSWORD}")
    private String apiKey;
      public void sendEmail(String to, String subject, String body) {
        try {
            log.info("📧 Sending email from: {} to: {}", fromEmail, to);

            Map<String, Object> requestBody = new HashMap<>();
            
            Map<String, String> sender = new HashMap<>();
            sender.put("email", fromEmail);
            requestBody.put("sender", sender);
            
            Map<String, String> recipient = new HashMap<>();
            recipient.put("email", to);
            requestBody.put("to", Collections.singletonList(recipient));
            
            requestBody.put("subject", subject);
            requestBody.put("htmlContent", body);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", apiKey);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                "https://api.brevo.com/v3/smtp/email",
                HttpMethod.POST,
                requestEntity,
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✅ Email sent successfully to: {}", to);
            } else {
                log.error("❌ Failed to send email: {}", response.getBody());
                throw new RuntimeException("Failed to send email: " + response.getBody());
            }

        } catch (Exception e) {
            log.error("❌ Failed to send email to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send activation email: " + e.getMessage(), e);
        }
    }
}
