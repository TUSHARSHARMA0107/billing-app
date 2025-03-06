package com.example.billing.controller;

import com.example.billing.service.EmailService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    @Autowired
    private PdfGeneratorService pdfGeneratorService;

    @PostMapping("/send-pdf")
    public String sendInvoice(@RequestParam String email, @RequestParam String invoiceDetails) {
        try {
            pdfGeneratorService.sendInvoiceEmail(email, customerName, totalAmount);
            return "Invoice sent successfully!";
        } catch (MessagingException e) {
            return "Error sending invoice: " + e.getMessage();
        }
    }
}
