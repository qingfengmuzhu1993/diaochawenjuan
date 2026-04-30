package com.smartsurvey.module.incentive.controller;

import com.smartsurvey.common.annotation.CurrentUser;
import com.smartsurvey.common.dto.ApiResponse;
import com.smartsurvey.module.incentive.service.PaymentService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/payment/recharge")
    public ApiResponse<Void> recharge(@CurrentUser Long userId,
                                       @RequestParam BigDecimal amount) {
        paymentService.recharge(userId, amount);
        return ApiResponse.ok();
    }

    @PostMapping("/payment/withdraw")
    public ApiResponse<Void> withdraw(@CurrentUser Long userId,
                                       @RequestParam BigDecimal amount) {
        paymentService.withdraw(userId, amount);
        return ApiResponse.ok();
    }
}
