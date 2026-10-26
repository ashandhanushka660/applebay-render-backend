package com.applebay.demo.service;

import com.applebay.demo.dto.*;
import com.applebay.demo.entity.*;
import com.applebay.demo.exception.*;
import com.applebay.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Transactional
    public AttendanceMarkResponse markClassAttendance(MarkAttendanceRequest req) {
        String batch = req.batch().trim().toUpperCase();
        Map<Long, Enrollment> roster = loadRoster(req.courseId(), batch);
        Set<Long> seen = new HashSet<>();
        for (AttendanceRecordRequest r : req.records()) {
            if (!roster.containsKey(r.enrollmentId())) throw new BusinessRuleException("Enrollment does not belong to class");
            if (!seen.add(r.enrollmentId())) throw new BusinessRuleException("Duplicate enrollment in request");
        }

        Map<Long, Attendance> existing = attendanceRepository.findByEnrollmentIdInAndAttendanceDate(seen, req.date()).stream().collect(Collectors.toMap(a -> a.getEnrollment().getId(), Function.identity()));
        int created = 0, updated = 0, unchanged = 0;
        List<Attendance> toSave = new ArrayList<>();
        
        for (AttendanceRecordRequest r : req.records()) {
            Attendance a = existing.get(r.enrollmentId());
            if (a == null) {
                a = new Attendance(); a.setEnrollment(roster.get(r.enrollmentId())); a.setAttendanceDate(req.date()); a.setStatus(r.status()); created++;
            } else if (a.getStatus() != r.status()) {
                a.setStatus(r.status()); updated++;
            } else { unchanged++; }
            toSave.add(a);
        }
        attendanceRepository.saveAll(toSave);
        return new AttendanceMarkResponse(req.courseId(), batch, req.date(), created, updated, unchanged);
    }

    public List<ClassSheetRow> classSheet(Long courseId, String batch, LocalDate date) {
        Map<Long, Enrollment> roster = loadRoster(courseId, batch.trim().toUpperCase());
        Map<Long, Attendance> marked = attendanceRepository.findByEnrollmentIdInAndAttendanceDate(roster.keySet(), date).stream().collect(Collectors.toMap(a -> a.getEnrollment().getId(), Function.identity()));
        return roster.values().stream().map(e -> new ClassSheetRow(e.getId(), e.getStudent().getStudentNumber(), e.getStudent().getFirstName() + " " + e.getStudent().getLastName(), marked.containsKey(e.getId()) ? marked.get(e.getId()).getStatus() : null)).toList();
    }

    public List<EligibilityRow> eligibility(Long courseId, String batch) {
        Map<Long, Enrollment> roster = loadRoster(courseId, batch.trim().toUpperCase());
        Map<Long, AttendanceSummaryView> summary = attendanceRepository.summarize(roster.keySet()).stream().collect(Collectors.toMap(AttendanceSummaryView::getEnrollmentId, Function.identity()));
        return roster.values().stream().map(e -> {
            AttendanceSummaryView s = summary.get(e.getId());
            long sessions = s == null ? 0 : s.getSessions(), attended = s == null ? 0 : s.getAttended();
            BigDecimal pct = sessions == 0 ? BigDecimal.ZERO.setScale(1) : BigDecimal.valueOf(attended * 100).divide(BigDecimal.valueOf(sessions), 1, RoundingMode.HALF_UP);
            return new EligibilityRow(e.getId(), e.getStudent().getStudentNumber(), e.getStudent().getFirstName() + " " + e.getStudent().getLastName(), sessions, attended, pct, sessions > 0 && attended * 100 >= 80 * sessions);
        }).toList();
    }

    private Map<Long, Enrollment> loadRoster(Long courseId, String batch) {
        List<Enrollment> list = enrollmentRepository.findClassRoster(courseId, batch);
        if (list.isEmpty()) throw new ResourceNotFoundException("No students found");
        return list.stream().collect(Collectors.toMap(Enrollment::getId, Function.identity(), (a, b) -> a, LinkedHashMap::new));
    }
}
