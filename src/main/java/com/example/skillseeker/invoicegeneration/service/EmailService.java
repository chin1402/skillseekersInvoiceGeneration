package com.example.skillseeker.invoicegeneration.service;

import com.example.skillseeker.invoicegeneration.helper.Constants;
import com.example.skillseeker.invoicegeneration.message.InvoiceHeaderData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.FileNotFoundException;
import java.util.Properties;

/**
 * @author I077659
 */
@Service
@Slf4j
public class EmailService {

    public static final String htmlTemplate = "<p>Hello, <b>%s</b>!\n" +
            "<br>\n</br>" +
            "<br>We want to express our gratitude for availing our services.\n</br>" +
            "<br>\n</br>" +
            "<br>The invoice for the month of <b>%s</b> is enclosed.\n</br>" +
            "<br>\n</br>" +
            "<br>The invoice includes all of the details.\n</br>" +
            "<br>\n</br>" +
            "<br>Looking forward to your response. In case more information is required, please revert.\n</br>" +
            "<br>\n</br>" +
            "<br>Regards and thanks,\n</br>" +
            "<br>Kritika Ahuja</br></p>";

    public void sendEmailWithAttachment(InvoiceHeaderData invoiceData){
        final String username = "skill4seekers@gmail.com";
        final String password = "oyzjmbjjscgbspho";

        Properties props = new java.util.Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        Message msg = new MimeMessage(session);
        try {
            log.info("Triggering invoice Email to: " + invoiceData.getEmail());
            msg.setFrom(new InternetAddress("skill4seekers@gmail.com"));
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(invoiceData.getEmail()));
            msg.setSubject(String.format(Constants.INVOICE_HEADING, invoiceData.getMonthOfInvoice()));

            Multipart multipart = new MimeMultipart();

            MimeBodyPart textBodyPart = new MimeBodyPart();
            textBodyPart.setContent(
                    String.format(htmlTemplate, invoiceData.getParentName(), invoiceData.getMonthOfInvoice()), "text/html; charset=utf-8"
            );

            MimeBodyPart attachmentBodyPart= new MimeBodyPart();
            DataSource source = new FileDataSource(invoiceData.getInvoiceFileName()); // ex : "C:\\test.pdf"
            attachmentBodyPart.setDataHandler(new DataHandler(source));
            attachmentBodyPart.setFileName(invoiceData.getInvoiceFileName()); // ex : "test.pdf"

            multipart.addBodyPart(textBodyPart);  // add the text part
            multipart.addBodyPart(attachmentBodyPart); // add the attachement part

            msg.setContent(multipart);


            Transport.send(msg);
            log.info("Email sent successful to Email: " + invoiceData.getEmail());
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}

