package com.applebay.demo.controller;

import org.springframework.security.access.prepost.PreAuthorize;

import com.applebay.demo.dto.*;
import com.applebay.demo.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CourseController {
    private final CourseService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CourseResponse create(@Valid @RequestBody CourseRequest r) {
        return service.create(r);
    }

    @GetMapping
    public List<CourseResponse> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public CourseResponse getById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PutMapping("/{id}")
    public CourseResponse update(@PathVariable Long id, @Valid @RequestBody CourseRequest r) {
        return service.update(id, r);
    }

    @PatchMapping("/{id}/deactivate")
    public CourseResponse deactivate(@PathVariable Long id) {
        return service.deactivate(id);
    }

    @PatchMapping("/{id}/reactivate")
    public CourseResponse reactivate(@PathVariable Long id) {
        return service.reactivate(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
