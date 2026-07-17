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
    public AuthController(AuthService authService) { this.authService = authService; }

    @PostMapping("/wechat")
    public AuthService.LoginResult login(@Valid @RequestBody WechatLoginRequest request) {
        return authService.login(request.code());
    }

    public record WechatLoginRequest(@NotBlank @Size(max = 128) String code) {}
}
