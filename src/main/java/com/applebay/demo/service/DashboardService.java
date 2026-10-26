package com.applebay.demo.service;

import com.applebay.demo.dto.DashboardResponse;
import com.applebay.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final AttendanceRepository attendanceRepository;
    private final PaymentRepository paymentRepository;

    public DashboardResponse summary() {
        long present = 0, late = 0, absent = 0;
        for (AttendanceStatusCount c : attendanceRepository.countByStatusOnDate(LocalDate.now())) {
            switch (c.getStatus()) { case PRESENT -> present = c.getTotal(); case LATE -> late = c.getTotal(); case ABSENT -> absent = c.getTotal(); }
        }
        return new DashboardResponse(studentRepository.count(), courseRepository.countByActiveTrue(), present + late + absent, present, late, absent, paymentRepository.totalCollected());
    }
}
