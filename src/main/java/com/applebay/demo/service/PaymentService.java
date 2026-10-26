package com.applebay.demo.service;

import com.applebay.demo.dto.BalanceResponse;
import com.applebay.demo.dto.PaymentResponse;
import com.applebay.demo.entity.Enrollment;
import com.applebay.demo.entity.Payment;
import com.applebay.demo.entity.PaymentMethod;
import com.applebay.demo.exception.BusinessRuleException;
import com.applebay.demo.exception.ResourceNotFoundException;
import com.applebay.demo.repository.EnrollmentRepository;
import com.applebay.demo.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Transactional
    public PaymentResponse record(Long enrollmentId, BigDecimal amount, LocalDate paymentDate, PaymentMethod method) {
        Enrollment enrollment = enrollmentRepository.findByIdForUpdate(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with ID: " + enrollmentId));

        BigDecimal totalPaid = paymentRepository.sumByEnrollmentId(enrollmentId);
        BigDecimal courseFee = enrollment.getCourse().getTotalFee();
        BigDecimal outstanding = courseFee.subtract(totalPaid);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Payment amount must be greater than zero.");
        }

        if (amount.compareTo(outstanding) > 0) {
            throw new BusinessRuleException("Payment of " + amount + " exceeds outstanding balance of " + outstanding);
        }

        LocalDate effectiveDate = paymentDate != null ? paymentDate : LocalDate.now();
        if (effectiveDate.isAfter(LocalDate.now())) {
            throw new BusinessRuleException("Payment date cannot be in the future.");
        }

        Payment payment = new Payment();
        payment.setEnrollment(enrollment);
        payment.setAmount(amount);
        payment.setPaymentDate(effectiveDate);
        payment.setMethod(method);
        payment.setReceiptNumber(generateReceiptNumber());

        Payment saved = paymentRepository.save(payment);
        return toPaymentResponse(saved);
    }

    public List<PaymentResponse> getHistory(Long enrollmentId) {
        if (!enrollmentRepository.existsById(enrollmentId)) {
            throw new ResourceNotFoundException("Enrollment not found with ID: " + enrollmentId);
        }
        return paymentRepository.findByEnrollment_IdOrderByPaymentDateAsc(enrollmentId).stream()
                .map(this::toPaymentResponse)
                .toList();
    }

    public BalanceResponse getBalance(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with ID: " + enrollmentId));

        BigDecimal totalPaid = paymentRepository.getTotalPaidByEnrollmentId(enrollmentId);
        BigDecimal totalFee = enrollment.getCourse().getTotalFee();
        BigDecimal outstanding = totalFee.subtract(totalPaid).max(BigDecimal.ZERO);

        return new BalanceResponse(
                enrollment.getId(),
                enrollment.getStudent().getStudentNumber(),
                enrollment.getStudent().getFirstName() + " " + enrollment.getStudent().getLastName(),
                enrollment.getCourse().getCode(),
                enrollment.getCourse().getName(),
                totalFee,
                totalPaid,
                outstanding
        );
    }

    private PaymentResponse toPaymentResponse(Payment p) {
        return new PaymentResponse(
                p.getId(),
                p.getReceiptNumber(),
                p.getEnrollment().getId(),
                p.getAmount(),
                p.getPaymentDate(),
                p.getMethod()
        );
    }

    private String generateReceiptNumber() {
        Long next = paymentRepository.nextReceiptSequence();
        return String.format("RC%d-%06d", java.time.Year.now().getValue(), next);
    }
}
