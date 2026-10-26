package com.applebay.demo.repository;
import com.applebay.demo.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByEnrollmentIdInAndAttendanceDate(Collection<Long> enrollmentIds, LocalDate date);

    @Query("select a.enrollment.id as enrollmentId, count(a) as sessions, count(case when a.status <> com.applebay.demo.entity.AttendanceStatus.ABSENT then 1 end) as attended from Attendance a where a.enrollment.id in :ids group by a.enrollment.id")
    List<AttendanceSummaryView> summarize(@Param("ids") Collection<Long> ids);

    @Query("select a.status as status, count(a) as total from Attendance a where a.attendanceDate = :date group by a.status")
    List<AttendanceStatusCount> countByStatusOnDate(@Param("date") LocalDate date);
}
