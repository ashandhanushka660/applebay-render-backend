package com.applebay.demo.repository;

import com.applebay.demo.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByEnrollment_IdOrderByPaymentDateAsc(Long enrollmentId);
    List<Payment> findByEnrollmentIdOrderByPaymentDateAsc(Long enrollmentId);

    @Query(value = "SELECT nextval('receipt_number_seq')", nativeQuery = true)
    Long getNextReceiptNumberSeq();

    @Query(value = "SELECT nextval('receipt_number_seq')", nativeQuery = true)
    Long nextReceiptSequence();

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.enrollment.id = :enrollmentId")
    BigDecimal getTotalPaidByEnrollmentId(@Param("enrollmentId") Long enrollmentId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.enrollment.id = :enrollmentId")
    BigDecimal sumByEnrollmentId(@Param("enrollmentId") long enrollmentId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p")
    BigDecimal totalCollected();
}
