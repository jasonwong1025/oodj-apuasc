package utils;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

/**
 * EmailService — Utility class responsible for sending emails.
 * 
 * This version uses the 'javax.mail' library to send real emails via Gmail SMTP.
 */
public class EmailService {

    // --- CONFIGURATION ---
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String SENDER_EMAIL = "ziming050211@gmail.com"; 
    private static final String APP_PASSWORD = "ldok tasu ilip kgjy";     

    private EmailService() {}

    /**
     * Sends a real OTP email via SMTP.
     *
     * @param recipientEmail  The target email address.
     * @param otp             The OTP code to include in the email body.
     * @throws Exception if sending fails.
     */
    public static void sendOtpEmail(String recipientEmail, String otp) throws Exception {
        System.out.println("[EmailService] Attempting to send real OTP email to: " + recipientEmail);

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.ssl.trust", SMTP_HOST);
        
        // Added SSL protocol and timeouts to prevent hanging and connection errors
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.connectiontimeout", "10000"); // 10 seconds
        props.put("mail.smtp.timeout", "10000");           // 10 seconds

        // Explicitly using javax.mail classes to avoid ambiguity
        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, APP_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL, "APU-ASC Support"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("APU-ASC Password Reset OTP");
            
            String body = "Dear User,\n\n" +
                          "Your One-Time Password (OTP) for password reset is: " + otp + "\n\n" +
                          "This code is valid for 10 minutes. Please do not share it with anyone.\n\n" +
                          "Regards,\nAPU Automotive Service Centre";
            
            message.setText(body);

            Transport.send(message);
            System.out.println("[EmailService] Real email sent successfully!");
            
        } catch (MessagingException e) {
            System.err.println("[EmailService] SMTP Error: " + e.getMessage());
            // Rethrow with a more user-friendly message
            throw new Exception("Email delivery failed: " + e.getMessage());
        }
    }
}
