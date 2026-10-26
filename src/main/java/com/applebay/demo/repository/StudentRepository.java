package com.applebay.demo.repository;
import com.applebay.demo.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
public interface StudentRepository extends JpaRepository<Student, Long> {
    boolean existsByNicIgnoreCase(String nic);
    boolean existsByEmailIgnoreCase(String email);
    Optional<Student> findByStudentNumber(String studentNumber);
    @Query(value = "SELECT nextval('student_number_seq')", nativeQuery = true)
    Long getNextStudentNumberSeq();
    @Query(value = "SELECT s FROM Student s WHERE :q IS NULL OR :q = '' " +
            "OR lower(s.studentNumber) LIKE lower(concat('%', :q, '%')) " +
            "OR lower(s.nic) LIKE lower(concat('%', :q, '%')) " +
            "OR lower(s.firstName) LIKE lower(concat('%', :q, '%')) " +
            "OR lower(s.lastName) LIKE lower(concat('%', :q, '%')) " +
            "OR lower(concat(s.firstName, ' ', s.lastName)) LIKE lower(concat('%', :q, '%')) " +
            "ORDER BY s.studentNumber ASC",
          countQuery = "SELECT count(s) FROM Student s WHERE :q IS NULL OR :q = '' " +
            "OR lower(s.studentNumber) LIKE lower(concat('%', :q, '%')) " +
            "OR lower(s.nic) LIKE lower(concat('%', :q, '%')) " +
            "OR lower(s.firstName) LIKE lower(concat('%', :q, '%')) " +
            "OR lower(s.lastName) LIKE lower(concat('%', :q, '%')) " +
            "OR lower(concat(s.firstName, ' ', s.lastName)) LIKE lower(concat('%', :q, '%'))")
    Page<Student> search(@Param("q") String q, Pageable pageable);
}
