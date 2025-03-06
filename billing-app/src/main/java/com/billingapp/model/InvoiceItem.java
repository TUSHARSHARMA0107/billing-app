package com.billingapp.model;

import jakarta.persistence.*;

@Entity
public class InvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;   // Item name
    private double price;  // Item price
    private int quantity;  // Item quantity

    @ManyToOne
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice; // Link to Invoice

    // Constructors
    public InvoiceItem() {}

    public InvoiceItem(String name, double price, int quantity, Invoice invoice) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.invoice = invoice;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public Invoice getInvoice() { return invoice; }
    public void setInvoice(Invoice invoice) { this.invoice = invoice; }
}
