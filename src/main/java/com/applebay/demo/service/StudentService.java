package com.applebay.demo.service;

import com.applebay.demo.dto.PageResponse;
import com.applebay.demo.dto.StudentRequest;
import com.applebay.demo.dto.StudentResponse;
import com.applebay.demo.entity.Enrollment;
import com.applebay.demo.entity.Student;
import com.applebay.demo.exception.DuplicateResourceException;
import com.applebay.demo.exception.ResourceNotFoundException;
import com.applebay.demo.repository.AttendanceRepository;
import com.applebay.demo.repository.EnrollmentRepository;
import com.applebay.demo.repository.PaymentRepository;
import com.applebay.demo.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentService {
    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AttendanceRepository attendanceRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    public StudentResponse register(StudentRequest req) {
        if (studentRepository.existsByNicIgnoreCase(req.nic())) {
            throw new DuplicateResourceException("A student with NIC " + req.nic() + " already exists.");
        }
        if (studentRepository.existsByEmailIgnoreCase(req.email())) {
            throw new DuplicateResourceException("A student with email " + req.email() + " already exists.");
        }

        Student s = new Student();
        s.setFirstName(req.firstName().trim());
        s.setLastName(req.lastName().trim());
        s.setNic(req.nic().trim().toUpperCase());
        s.setEmail(req.email().trim().toLowerCase());
        s.setPhone(req.phone().trim());
        s.setAddress(req.address().trim());
        s.setDateOfBirth(req.dateOfBirth());
        s.setStudentNumber(generateStudentNumber());
        return toResponse(studentRepository.save(s));
    }

    public PageResponse<StudentResponse> search(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Student> result = studentRepository.search(query, pageable);
        return new PageResponse<>(
            result.getContent().stream().map(this::toResponse).toList(),
            result.getNumber(),
            result.getSize(),
            result.getTotalElements(),
            result.getTotalPages()
        );
    }

    public StudentResponse findById(Long id) {
        return toResponse(studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + id)));
    }

    @Transactional
    public StudentResponse update(Long id, StudentRequest req) {
        Student s = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + id));

        if (!s.getNic().equalsIgnoreCase(req.nic()) && studentRepository.existsByNicIgnoreCase(req.nic())) {
            throw new DuplicateResourceException("NIC " + req.nic() + " is already in use by another student.");
        }
        if (!s.getEmail().equalsIgnoreCase(req.email()) && studentRepository.existsByEmailIgnoreCase(req.email())) {
            throw new DuplicateResourceException("Email " + req.email() + " is already in use by another student.");
        }

        s.setFirstName(req.firstName().trim());
        s.setLastName(req.lastName().trim());
        s.setNic(req.nic().trim().toUpperCase());
        s.setEmail(req.email().trim().toLowerCase());
        s.setPhone(req.phone().trim());
        s.setAddress(req.address().trim());
        s.setDateOfBirth(req.dateOfBirth());

        return toResponse(studentRepository.save(s));
    }

    @Transactional
    public void delete(Long id) {
        Student s = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + id));

        List<Enrollment> enrollments = enrollmentRepository.findAll().stream()
                .filter(e -> e.getStudent().getId().equals(id))
                .toList();

        for (Enrollment e : enrollments) {
            attendanceRepository.findAll().stream()
                    .filter(a -> a.getEnrollment().getId().equals(e.getId()))
                    .forEach(attendanceRepository::delete);

            paymentRepository.findAll().stream()
                    .filter(p -> p.getEnrollment().getId().equals(e.getId()))
                    .forEach(paymentRepository::delete);

            enrollmentRepository.delete(e);
        }

        studentRepository.delete(s);
    }

    public StudentResponse toResponse(Student s) {
        return new StudentResponse(
            s.getId(),
            s.getStudentNumber(),
            s.getFirstName(),
            s.getLastName(),
            s.getNic(),
            s.getEmail(),
            s.getPhone(),
            s.getAddress(),
            s.getDateOfBirth()
        );
    }

    private String generateStudentNumber() {
        Long next = studentRepository.getNextStudentNumberSeq();
        return String.format("AB2026-%04d", next);
    }
}
