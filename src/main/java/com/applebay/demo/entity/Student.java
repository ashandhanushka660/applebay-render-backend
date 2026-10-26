package com.applebay.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Table(
    name = "students",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_students_student_number", columnNames = "student_number"),
        @UniqueConstraint(name = "uk_students_nic", columnNames = "nic"),
        @UniqueConstraint(name = "uk_students_email", columnNames = "email")
    }
)
@Getter @Setter @NoArgsConstructor
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_number", nullable = false, length = 20, unique = true)
    private String studentNumber;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, length = 20, unique = true)
    private String nic;

    @Column(nullable = false, length = 150, unique = true)
    private String email;

    @Column(nullable = false, length = 25)
    private String phone;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "registered_on")
    private LocalDate registeredOn = LocalDate.now();
}
