package com.billingapp.controller;

import com.billingapp.model.Invoice;
import com.billingapp.model.InvoiceItem;
import com.billingapp.repository.InvoiceRepository;
import com.billingapp.repository.InvoiceItemRepository;
import com.billingapp.service.QRCodeService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/invoices")
@CrossOrigin(origins = "*")
public class InvoiceController {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final QRCodeService qrCodeService;

    public InvoiceController(InvoiceRepository invoiceRepository, InvoiceItemRepository invoiceItemRepository, QRCodeService qrCodeService) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceItemRepository = invoiceItemRepository;
        this.qrCodeService = qrCodeService;
    }

    // ✅ Get all invoices
    @GetMapping
    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    // ✅ Get a specific invoice by ID
    @GetMapping("/{id}")
    public ResponseEntity<Invoice> getInvoiceById(@PathVariable Long id) {
        return invoiceRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ Create a new invoice
    @PostMapping
    public ResponseEntity<Invoice> createInvoice(@RequestBody Invoice invoice) {
        invoice.calculateTotal();
        Invoice savedInvoice = invoiceRepository.save(invoice);
        return ResponseEntity.ok(savedInvoice);
    }

    // ✅ Update an existing invoice
    @PutMapping("/{id}")
    public ResponseEntity<Invoice> updateInvoice(@PathVariable Long id, @RequestBody Invoice invoiceDetails) {
        Optional<Invoice> invoiceOpt = invoiceRepository.findById(id);
        if (!invoiceOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Invoice invoice = invoiceOpt.get();
        invoice.setCustomerName(invoiceDetails.getCustomerName());
        invoice.setTaxPercentage(invoiceDetails.getTaxPercentage());
        invoice.setDiscountAmount(invoiceDetails.getDiscountAmount());
        invoice.setCurrency(invoiceDetails.getCurrency());
        invoice.calculateTotal();

        Invoice updatedInvoice = invoiceRepository.save(invoice);
        return ResponseEntity.ok(updatedInvoice);
    }

    // ✅ Delete an invoice
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvoice(@PathVariable Long id) {
        if (!invoiceRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        invoiceRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ✅ Add item to an invoice
    @PostMapping("/{invoiceId}/items")
    public ResponseEntity<Invoice> addItemToInvoice(@PathVariable Long invoiceId, @RequestBody InvoiceItem item) {
        Optional<Invoice> invoiceOpt = invoiceRepository.findById(invoiceId);
        if (!invoiceOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Invoice invoice = invoiceOpt.get();
        item.setInvoice(invoice);
        invoice.getItems().add(item);
        invoice.calculateTotal();
        invoiceRepository.save(invoice);

        return ResponseEntity.ok(invoice);
    }

    // ✅ Remove item from an invoice
    @DeleteMapping("/{invoiceId}/items/{itemId}")
    public ResponseEntity<Invoice> removeItemFromInvoice(@PathVariable Long invoiceId, @PathVariable Long itemId) {
        Optional<Invoice> invoiceOpt = invoiceRepository.findById(invoiceId);
        Optional<InvoiceItem> itemOpt = invoiceItemRepository.findById(itemId);

        if (!invoiceOpt.isPresent() || !itemOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Invoice invoice = invoiceOpt.get();
        InvoiceItem item = itemOpt.get();

        invoice.getItems().remove(item);
        invoiceItemRepository.delete(item);
        invoice.calculateTotal();
        invoiceRepository.save(invoice);

        return ResponseEntity.ok(invoice);
    }

    // ✅ Generate QR Code for payment
    @GetMapping("/{id}/qrcode")
    public ResponseEntity<byte[]> generateQRCode(@PathVariable Long id) {
        Optional<Invoice> invoiceOpt = invoiceRepository.findById(id);
        if (!invoiceOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Invoice invoice = invoiceOpt.get();
        String paymentLink = "upi://pay?pa=yourupiid@upi&pn=YourBusiness&mc=1234&tid=123456&tr=" + invoice.getId() + "&tn=Invoice Payment&am=" + invoice.getTotal() + "&cu=" + invoice.getCurrency();
        byte[] qrCodeImage = qrCodeService.generateQRCode(paymentLink, 200, 200);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice_qr.png")
                .contentType(MediaType.IMAGE_PNG)
                .body(qrCodeImage);
    }

    // ✅ Print Invoice (Returns a simple HTML page as PDF)
    @GetMapping("/{id}/print")
    public ResponseEntity<String> printInvoice(@PathVariable Long id) {
        Optional<Invoice> invoiceOpt = invoiceRepository.findById(id);
        if (!invoiceOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Invoice invoice = invoiceOpt.get();
        String html = "<html><head><title>Invoice</title></head><body>"
                + "<h2>Invoice #" + invoice.getId() + "</h2>"
                + "<p>Customer: " + invoice.getCustomerName() + "</p>"
                + "<h3>Items:</h3><ul>";

        for (InvoiceItem item : invoice.getItems()) {
            html += "<li>" + item.getName() + " - " + item.getQuantity() + " x " + item.getPrice() + " = " + (item.getQuantity() * item.getPrice()) + "</li>";
        }

        html += "</ul>"
                + "<p>Tax: " + invoice.getTaxPercentage() + "%</p>"
                + "<p>Discount: " + invoice.getDiscountAmount() + "</p>"
                + "<h3>Total: " + invoice.getTotal() + " " + invoice.getCurrency() + "</h3>"
                + "</body></html>";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "text/html")
                .body(html);
    }
    // Generate and Store Invoice PDF
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> generateAndStoreInvoicePDF(@PathVariable Long id) {
        Optional<Invoice> invoiceOpt = invoiceRepository.findById(id);
        if (!invoiceOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Invoice invoice = invoiceOpt.get();
        String fileName = "invoice_" + id + ".pdf";
        String filePath = PDF_DIRECTORY + fileName;
        
        try {
            Files.createDirectories(Paths.get(PDF_DIRECTORY)); // Ensure directory exists
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            document.add(new Paragraph("Invoice #" + invoice.getId()));
            document.add(new Paragraph("Customer: " + invoice.getCustomerName()));
            document.add(new Paragraph("\nItems:", FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
            
            for (InvoiceItem item : invoice.getItems()) {
                document.add(new Paragraph(item.getName() + " - " + item.getQuantity() + " x " + item.getPrice() + " = " + (item.getQuantity() * item.getPrice())));
            }
            
            document.add(new Paragraph("\nTax: " + invoice.getTaxPercentage() + "%"));
            document.add(new Paragraph("Discount: " + invoice.getDiscountAmount()));
            document.add(new Paragraph("Total: " + invoice.getTotal() + " " + invoice.getCurrency(), FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
            document.close();
            
            invoice.setPdfPath(filePath);
            invoiceRepository.save(invoice);

            byte[] pdfBytes = Files.readAllBytes(Paths.get(filePath));
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    // Update an Invoice Item with Real-time WebSocket Notification
    @PutMapping("/items/{itemId}")
    public ResponseEntity<InvoiceItem> updateInvoiceItem(@PathVariable Long itemId, @RequestBody InvoiceItem updatedItem) {
        Optional<InvoiceItem> existingItemOpt = invoiceItemRepository.findById(itemId);
        if (!existingItemOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        InvoiceItem existingItem = existingItemOpt.get();
        existingItem.setName(updatedItem.getName());
        existingItem.setPrice(updatedItem.getPrice());
        existingItem.setQuantity(updatedItem.getQuantity());
        
        invoiceItemRepository.save(existingItem);
        messagingTemplate.convertAndSend("/topic/invoices", "Item Updated");
        return ResponseEntity.ok(existingItem);
    }

    // Delete an Invoice Item with Real-time WebSocket Notification
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> deleteInvoiceItem(@PathVariable Long itemId) {
        if (!invoiceItemRepository.existsById(itemId)) {
            return ResponseEntity.notFound().build();
        }
        invoiceItemRepository.deleteById(itemId);
        messagingTemplate.convertAndSend("/topic/invoices", "Item Deleted");
        return ResponseEntity.noContent().build();
    }

    //login system
    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public InvoiceController(InvoiceRepository invoiceRepository, UserRepository userRepository, JwtUtil jwtUtil) {
        this.invoiceRepository = invoiceRepository;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/my-invoices")
    public ResponseEntity<List<Invoice>> getUserInvoices(@RequestHeader("Authorization") String token) {
        String username = jwtUtil.extractUsername(token.substring(7)); // Extract username from JWT token
        User user = userRepository.findByUsername(username).orElseThrow();
        
        List<Invoice> invoices = invoiceRepository.findByUser(user);
        return ResponseEntity.ok(invoices);
    }
    @PostMapping("/create")
    public ResponseEntity<String> createInvoice(@RequestHeader("Authorization") String token, @RequestBody Invoice invoice) {
        String username = jwtUtil.extractUsername(token.substring(7));
        User user = userRepository.findByUsername(username).orElseThrow();
        
        invoice.setUser(user);
        invoiceRepository.save(invoice);
        return ResponseEntity.ok("Invoice created successfully!");
      }
////template saving
// 2️⃣ Update Invoice Controller to Handle Invoice Saving

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {
    @Autowired
    private InvoiceService invoiceService;

    @PostMapping("/save")
    public ResponseEntity<Invoice> saveInvoice(@RequestBody Invoice invoice) {
        Invoice savedInvoice = invoiceService.saveInvoice(invoice);
        return ResponseEntity.ok(savedInvoice);
    }
}

// 3️⃣ Service Layer to Save Invoices

@Service
public class InvoiceService {
    @Autowired
    private InvoiceRepository invoiceRepository;

    public Invoice saveInvoice(Invoice invoice) {
        return invoiceRepository.save(invoice);
    }
}
}


