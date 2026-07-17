package com.one.identity;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/wechat")
    public AuthService.LoginResult login(@Valid @RequestBody WechatLoginRequest request) {
        return authService.login(request.code());
    }

    @PostMapping("/admin")
    public AuthService.LoginResult adminLogin(@Valid @RequestBody AdminLoginRequest request) {
        return authService.adminLogin(request.username(), request.password());
    }

    public record WechatLoginRequest(@NotBlank @Size(max = 128) String code) {
    }

    public record AdminLoginRequest(
            @NotBlank @Size(max = 64) String username,
            @NotBlank @Size(max = 256) String password
    ) {
    }
}
