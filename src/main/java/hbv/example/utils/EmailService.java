package hbv.example.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;
import javax.mail.util.ByteArrayDataSource;

public class EmailService {
    private static final String CONFIG_FILE = "/config.properties"; //  Konfigurationsdatei
    private static String senderEmail;
    private static String senderPassword;
    private static final Properties mailProperties = new Properties();

    //  Laden der E-Mail-Konfiguration aus config.properties
    static {
        try (InputStream input = EmailService.class.getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                System.out.println(" Fehler: config.properties konnte nicht gefunden werden!");
                throw new RuntimeException("Konfigurationsdatei fehlt!");
            }

            mailProperties.load(input);
            senderEmail = mailProperties.getProperty("EMAIL_SENDER");
            senderPassword = mailProperties.getProperty("EMAIL_PASSWORD");

            mailProperties.put("mail.smtp.auth", "true");
            mailProperties.put("mail.smtp.starttls.enable", "true");
            mailProperties.put("mail.smtp.host", "smtp.gmail.com");
            mailProperties.put("mail.smtp.port", "587");

            System.out.println(" E-Mail-Konfiguration erfolgreich geladen!");
        } catch (IOException e) {
            throw new RuntimeException(" Fehler beim Laden der E-Mail-Konfiguration!", e);
        }
    }

    //  Erstellt eine Sitzung mit Authentifizierung für den E-Mail-Versand
    private static Session getSession() {
        return Session.getInstance(mailProperties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });
    }

    // Methode zum Senden einer einfachen E-Mail ohne Anhänge
    public static void sendEmail(String recipientEmail, String subject, String messageText) {
        try {
            Session session = getSession();
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);
            message.setText(messageText);

            Transport.send(message);
            System.out.println(" E-Mail erfolgreich gesendet an: " + recipientEmail);
        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("Fehler beim Senden der E-Mail an: " + recipientEmail);
        }
    }

    //  Methode zum Senden einer E-Mail mit PDF- und QR-Code-Anhang
    public static void sendEmailWithAttachments(String recipientEmail, String Uservorname ,String Usernachname,String firstName, String lastName,
                                                ByteArrayOutputStream pdfData, ByteArrayOutputStream qrCodeData) {
        try {
            Session session = getSession();
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(" Bestätigung des Termins für " + firstName + " " + lastName);

            //  E-Mail-Text als HTML-Format
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            String htmlMessage = "<h3>Hallo " + Uservorname + " " + Usernachname + ",</h3>"
                    + "<p>Hier ist Ihre Terminbestaetigung.</p>"
                    + "<p>Bitte bringen Sie dieses Dokument und den QR-Code zum Termin mit.</p>";
            messageBodyPart.setContent(htmlMessage, "text/html");

            // PDF-Datei als Anhang hinzufügen
            MimeBodyPart pdfAttachment = new MimeBodyPart();
            pdfAttachment.setDataHandler(new DataHandler(new ByteArrayDataSource(pdfData.toByteArray(), "application/pdf")));
            pdfAttachment.setFileName("Terminbestätigung.pdf");

            //  QR-Code als Anhang hinzufügen
            MimeBodyPart qrCodeAttachment = new MimeBodyPart();
           qrCodeAttachment.setDataHandler(new DataHandler(new ByteArrayDataSource(qrCodeData.toByteArray(), "image/png")));
            qrCodeAttachment.setFileName("QRCode.png");
           // qrCodeAttachment.setContent(qrCodeData.toByteArray(), "image/png");

            //  Multipart-E-Mail zusammenstellen
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);
            multipart.addBodyPart(pdfAttachment);
            multipart.addBodyPart(qrCodeAttachment);

            message.setContent(multipart);

            //  E-Mail senden
            Transport.send(message);
            System.out.println(" E-Mail mit PDF und QR-Code erfolgreich gesendet an: " + recipientEmail);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Fehler beim Senden der E-Mail an: " + recipientEmail);
        }
    }
}