package com.applebay.demo.service;

import com.applebay.demo.dto.*;
import com.applebay.demo.entity.*;
import com.applebay.demo.exception.*;
import com.applebay.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentService {
    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    @Transactional
    public EnrollmentResponse enroll(EnrollmentRequest req) {
        Student student = studentRepository.findById(req.studentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + req.studentId()));
        Course course = courseRepository.findById(req.courseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + req.courseId()));

        if (!course.isActive()) {
            throw new BusinessRuleException("Cannot enroll into an inactive course: " + course.getName());
        }

        String normalizedBatch = req.batch().trim().toUpperCase();

        // FR05: Check if already enrolled in the exact same course and batch
        if (enrollmentRepository.existsByStudentIdAndCourseIdAndBatchIgnoreCase(student.getId(), course.getId(), normalizedBatch)) {
            throw new DuplicateResourceException(
                "Student (" + student.getStudentNumber() + ") is already enrolled in " + course.getCode() + " for batch " + normalizedBatch
            );
        }

        Enrollment e = new Enrollment();
        e.setStudent(student);
        e.setCourse(course);
        e.setBatch(normalizedBatch);
        
        return toResponse(enrollmentRepository.save(e));
    }

    public List<EnrollmentResponse> findByStudent(Long studentId) {
        return enrollmentRepository.findByStudentIdWithDetails(studentId).stream().map(this::toResponse).toList();
    }

    private EnrollmentResponse toResponse(Enrollment e) {
        return new EnrollmentResponse(
            e.getId(),
            e.getStudent().getId(),
            e.getStudent().getStudentNumber(),
            e.getStudent().getFirstName() + " " + e.getStudent().getLastName(),
            e.getCourse().getId(),
            e.getCourse().getCode(),
            e.getCourse().getName(),
            e.getBatch(),
            e.getEnrolledOn()
        );
    }
}
