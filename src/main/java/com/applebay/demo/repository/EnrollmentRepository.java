package com.applebay.demo.repository;

import com.applebay.demo.entity.Enrollment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    boolean existsByStudentIdAndCourseIdAndBatchIgnoreCase(Long studentId, Long courseId, String batch);

    @Query("select e from Enrollment e join fetch e.course join fetch e.student where e.student.id = :studentId")
    List<Enrollment> findByStudentIdWithDetails(@Param("studentId") Long studentId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Enrollment e join fetch e.course where e.id = :id")
    Optional<Enrollment> findByIdForUpdate(@Param("id") Long id);

    @Query("select e from Enrollment e join fetch e.course join fetch e.student where e.id = :id")
    Optional<Enrollment> findDetailsById(@Param("id") Long id);

    @Query("select e from Enrollment e join fetch e.student where e.course.id = :courseId and upper(e.batch) = upper(:batch) order by e.student.studentNumber")
    List<Enrollment> findClassRoster(@Param("courseId") Long courseId, @Param("batch") String batch);
}
