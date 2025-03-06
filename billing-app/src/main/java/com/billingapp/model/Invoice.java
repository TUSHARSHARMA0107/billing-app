package com.billingapp.model;
import jakarta.persistence.*;
import lombok.*;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity


@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;
    private Double totalAmount;
    private Date invoiceDate;
    private String status; // "Paid" or "Unpaid"

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String customerName;
    private BigDecimal subtotal;
    private BigDecimal taxPercentage;
    private BigDecimal discountAmount;
    private BigDecimal total;
    private String currency; // Stores the currency type (e.g., USD, INR, EUR)

    public Invoice() {
    }

    public Invoice(String customerName, BigDecimal subtotal, BigDecimal taxPercentage, BigDecimal discountAmount, String currency) {
        this.customerName = customerName;
        this.subtotal = subtotal;
        this.taxPercentage = taxPercentage;
        this.discountAmount = discountAmount;
        this.currency = currency;
        calculateTotal();

        //logo
        private String logo;
    }

    // Auto-calculates the total after tax and discount
    public void calculateTotal() {
        BigDecimal taxAmount = subtotal.multiply(taxPercentage.divide(BigDecimal.valueOf(100)));
        this.total = subtotal.add(taxAmount).subtract(discountAmount);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getTaxPercentage() {
        return taxPercentage;
    }

    public void setTaxPercentage(BigDecimal taxPercentage) {
        this.taxPercentage = taxPercentage;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
private List<InvoiceItem> items = new ArrayList<>();

// Getter & Setter for Items
public List<InvoiceItem> getItems() { return items; }
public void setItems(List<InvoiceItem> items) { this.items = items; }

// Method to calculate total dynamically
public void calculateTotal() {
    double totalItems = items.stream().mapToDouble(item -> item.getPrice() * item.getQuantity()).sum();
    double total = totalItems + (totalItems * (taxPercentage / 100)) - discountAmount;
    this.total = total;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "invoices")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title; // Invoice title
    private Double totalAmount;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Link invoice to the logged-in user
}
}


