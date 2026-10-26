package com.applebay.demo.service;

import com.applebay.demo.dto.EligibilityRow;
import com.applebay.demo.entity.Course;
import com.applebay.demo.entity.Enrollment;
import com.applebay.demo.entity.Student;
import com.applebay.demo.repository.AttendanceRepository;
import com.applebay.demo.repository.AttendanceSummaryView;
import com.applebay.demo.repository.EnrollmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock private AttendanceRepository attendanceRepository;
    @Mock private EnrollmentRepository enrollmentRepository;
    @InjectMocks private AttendanceService attendanceService;

    private Enrollment enrollment(long id, String studentNumber) {
        Course course = new Course();
        course.setCode("HND-NE");
        Student student = new Student();
        student.setStudentNumber(studentNumber);
        student.setFirstName("Test");
        student.setLastName("Student");
        Enrollment enrollment = new Enrollment();
        enrollment.setId(id);
        enrollment.setCourse(course);
        enrollment.setStudent(student);
        return enrollment;
    }

    private AttendanceSummaryView summary(long enrollmentId, long sessions, long attended) {
        AttendanceSummaryView view = mock(AttendanceSummaryView.class);
        when(view.getEnrollmentId()).thenReturn(enrollmentId);
        when(view.getSessions()).thenReturn(sessions);
        when(view.getAttended()).thenReturn(attended);
        return view;
    }

    @Test
    void eligibility_flagsStudentAtExactlyEightPercentAsEligible() {
        AttendanceSummaryView eightyPercent = summary(10L, 10, 8);
        AttendanceSummaryView seventyPercent = summary(11L, 10, 7);

        when(enrollmentRepository.findClassRoster(2L, "BATCH-2026-01"))
                .thenReturn(List.of(enrollment(10L, "AB2026-0010"), enrollment(11L, "AB2026-0011")));
        when(attendanceRepository.summarize(any()))
                .thenReturn(List.of(eightyPercent, seventyPercent));

        List<EligibilityRow> rows = attendanceService.eligibility(2L, "BATCH-2026-01");

        assertEquals(2, rows.size());
        assertTrue(rows.get(0).eligible());
        assertTrue(rows.get(0).percentage().compareTo(new java.math.BigDecimal("80.0")) == 0);
        assertFalse(rows.get(1).eligible());
    }
}
