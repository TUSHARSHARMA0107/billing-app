package com.example.billingapp.repository;

import com.example.billingapp.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByUser(User user); // Fetch invoices only for a specific user
}
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
}
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    
    @Query("SELECT SUM(i.totalAmount) FROM Invoice i WHERE i.user.id = :userId AND i.status = 'Paid'")
    Double getTotalIncome(@Param("userId") Long userId);
}
