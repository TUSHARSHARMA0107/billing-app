package com.example.billing.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class PdfGeneratorService {

    @Autowired
    private JavaMailSender mailSender;

    public byte[] generateInvoicePdf(String customerName, double totalAmount) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document();

        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();
            
            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Font textFont = new Font(Font.HELVETICA, 12);
            
            document.add(new Paragraph("Invoice", titleFont));
            document.add(new Paragraph("Customer: " + customerName, textFont));
            document.add(new Paragraph("Total Amount: ₹" + totalAmount, textFont));
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            document.close();
        }

        return outputStream.toByteArray();
    }

    public void sendInvoiceEmail(String toEmail, String customerName, double totalAmount) throws MessagingException, IOException {
        byte[] pdfBytes = generateInvoicePdf(customerName, totalAmount);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(toEmail);
        helper.setSubject("Your Invoice");
        helper.setText("Please find your invoice attached.", false);
        helper.addAttachment("Invoice.pdf", () -> new java.io.ByteArrayInputStream(pdfBytes));

        mailSender.send(message);
    }
}
