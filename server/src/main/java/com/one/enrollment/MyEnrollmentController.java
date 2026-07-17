package com.one.enrollment;

import com.one.security.OnePrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/me/enrollments")
public class MyEnrollmentController {

    private final EnrollmentService enrollmentService;

    public MyEnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @GetMapping
    public List<EnrollmentService.EnrollmentView> list(@AuthenticationPrincipal OnePrincipal principal) {
        return enrollmentService.myEnrollments(principal.userId());
    }
}
