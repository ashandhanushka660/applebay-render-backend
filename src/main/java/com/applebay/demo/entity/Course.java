package com.applebay.demo.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "courses")
@Getter @Setter @NoArgsConstructor
public class Course {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 20)
    private String code;
    @Column(nullable = false, length = 150)
    private String name;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private CourseLevel level;
    @Column(name = "duration_months", nullable = false)
    private Integer durationMonths;
    @Column(name = "total_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalFee;
    @Column(nullable = false)
    private boolean active = true;
}
