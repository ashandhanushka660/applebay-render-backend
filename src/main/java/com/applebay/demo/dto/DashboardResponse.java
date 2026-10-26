package com.applebay.demo.dto;
import java.math.BigDecimal;
public record DashboardResponse(long totalStudents, long activeCourses, long attendanceMarkedToday, long presentToday, long lateToday, long absentToday, BigDecimal totalFeesCollected) {}
