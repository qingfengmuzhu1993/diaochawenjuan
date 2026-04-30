package com.smartsurvey.module.user.controller;

import com.smartsurvey.common.dto.ApiResponse;
import com.smartsurvey.module.user.dto.*;
import com.smartsurvey.module.user.service.AuthService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiResponse<LoginResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ApiResponse.ok(authService.register(req));
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return ApiResponse.ok(authService.login(req));
    }

    @PostMapping("/send-sms")
    public ApiResponse<Void> sendSms(@RequestParam String phone) {
        authService.sendSmsCode(phone);
        return ApiResponse.ok();
    }

    @PostMapping("/refresh-token")
    public ApiResponse<LoginResponse> refreshToken(@RequestParam String refreshToken) {
        return ApiResponse.ok(authService.refreshToken(refreshToken));
    }
}
