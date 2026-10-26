package com.applebay.demo.service;

import com.applebay.demo.dto.PaymentResponse;
import com.applebay.demo.entity.Course;
import com.applebay.demo.entity.Enrollment;
import com.applebay.demo.entity.Payment;
import com.applebay.demo.entity.PaymentMethod;
import com.applebay.demo.exception.BusinessRuleException;
import com.applebay.demo.repository.EnrollmentRepository;
import com.applebay.demo.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private EnrollmentRepository enrollmentRepository;
    @InjectMocks private PaymentService paymentService;

    private Enrollment enrollmentWithFee(String fee) {
        Course course = new Course();
        course.setTotalFee(new BigDecimal(fee));
        Enrollment enrollment = new Enrollment();
        enrollment.setId(1L);
        enrollment.setCourse(course);
        return enrollment;
    }

    @Test
    void recordPayment_rejectsPaymentGreaterThanOutstandingBalance() {
        when(enrollmentRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(enrollmentWithFee("100000.00")));
        when(paymentRepository.sumByEnrollmentId(1L)).thenReturn(new BigDecimal("80000.00"));

        assertThrows(BusinessRuleException.class, () ->
                paymentService.record(1L, new BigDecimal("25000.00"), LocalDate.now(), PaymentMethod.CASH));

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void recordPayment_rejectsPaymentWhenEnrollmentIsFullyPaid() {
        when(enrollmentRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(enrollmentWithFee("100000.00")));
        when(paymentRepository.sumByEnrollmentId(1L)).thenReturn(new BigDecimal("100000.00"));

        assertThrows(BusinessRuleException.class, () ->
                paymentService.record(1L, new BigDecimal("1000.00"), LocalDate.now(), PaymentMethod.CARD));
    }

    @Test
    void recordPayment_generatesReceiptAndSavesValidInstalment() {
        when(enrollmentRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(enrollmentWithFee("100000.00")));
        when(paymentRepository.sumByEnrollmentId(1L)).thenReturn(new BigDecimal("0.00"));
        when(paymentRepository.nextReceiptSequence()).thenReturn(1L);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentResponse response = paymentService.record(
                1L, new BigDecimal("30000.00"), LocalDate.now(), PaymentMethod.BANK_TRANSFER);

        assertEquals(new BigDecimal("30000.00"), response.amount());
        assertEquals("RC" + Year.now().getValue() + "-000001", response.receiptNumber());
        assertEquals(1L, response.enrollmentId());
    }
}
