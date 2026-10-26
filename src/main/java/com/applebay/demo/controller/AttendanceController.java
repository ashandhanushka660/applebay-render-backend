package com.applebay.demo.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import com.applebay.demo.dto.*;
import com.applebay.demo.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController @RequestMapping("/api/attendance") @RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AttendanceController {
    private final AttendanceService service;
    @PostMapping public AttendanceMarkResponse mark(@Valid @RequestBody MarkAttendanceRequest r) { return service.markClassAttendance(r); }
    @GetMapping("/class-sheet") public List<ClassSheetRow> classSheet(@RequestParam Long courseId, @RequestParam String batch, @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate date) { return service.classSheet(courseId, batch, date); }
    @GetMapping("/eligibility") public List<EligibilityRow> eligibility(@RequestParam Long courseId, @RequestParam String batch) { return service.eligibility(courseId, batch); }
}
