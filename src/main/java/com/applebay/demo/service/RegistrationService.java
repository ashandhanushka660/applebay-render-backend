package com.applebay.demo.service;

import com.applebay.demo.dto.*;
import com.applebay.demo.entity.Course;
import com.applebay.demo.entity.Enrollment;
import com.applebay.demo.entity.Student;
import com.applebay.demo.exception.BusinessRuleException;
import com.applebay.demo.exception.DuplicateResourceException;
import com.applebay.demo.exception.ResourceNotFoundException;
import com.applebay.demo.repository.CourseRepository;
import com.applebay.demo.repository.EnrollmentRepository;
import com.applebay.demo.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class RegistrationService {
    private final StudentService studentService;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final PaymentService paymentService;

    @Transactional
    public RegistrationResponse registerAndEnroll(RegistrationRequest req) {
        // 1. Register student
        StudentResponse studentResp = studentService.register(req.student());
        Student student = studentRepository.findById(studentResp.id())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        // 2. Validate course
        Course course = courseRepository.findById(req.courseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + req.courseId()));
        if (!course.isActive()) {
            throw new BusinessRuleException("Cannot register into an inactive course: " + course.getName());
        }

        String normalizedBatch = req.batch().trim().toUpperCase();

        // 3. FR05 duplicate check
        if (enrollmentRepository.existsByStudentIdAndCourseIdAndBatchIgnoreCase(student.getId(), course.getId(), normalizedBatch)) {
            throw new DuplicateResourceException("Student is already enrolled in this course and batch.");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setBatch(normalizedBatch);
        enrollment.setEnrolledOn(LocalDate.now());
        enrollment = enrollmentRepository.save(enrollment);

        // 4. Record first payment atomically
        PaymentResponse paymentResp = paymentService.record(
            enrollment.getId(),
            req.firstPayment().amount(),
            req.firstPayment().paymentDate(),
            req.firstPayment().method()
        );

        EnrollmentResponse enrollmentResp = new EnrollmentResponse(
            enrollment.getId(),
            student.getId(),
            student.getStudentNumber(),
            student.getFirstName() + " " + student.getLastName(),
            course.getId(),
            course.getCode(),
            course.getName(),
            enrollment.getBatch(),
            enrollment.getEnrolledOn()
        );

        return new RegistrationResponse(studentResp, enrollmentResp, paymentResp);
    }
}
