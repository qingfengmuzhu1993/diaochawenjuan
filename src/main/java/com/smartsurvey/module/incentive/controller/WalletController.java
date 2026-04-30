package com.smartsurvey.module.incentive.controller;

import com.smartsurvey.common.annotation.CurrentUser;
import com.smartsurvey.common.dto.ApiResponse;
import com.smartsurvey.common.dto.PageResult;
import com.smartsurvey.module.incentive.dto.BillItemResponse;
import com.smartsurvey.module.incentive.dto.WalletResponse;
import com.smartsurvey.module.incentive.service.TransactionService;
import com.smartsurvey.module.user.entity.User;
import com.smartsurvey.module.user.mapper.UserMapper;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class WalletController {
    private final UserMapper userMapper;
    private final TransactionService transactionService;

    public WalletController(UserMapper userMapper, TransactionService transactionService) {
        this.userMapper = userMapper;
        this.transactionService = transactionService;
    }

    @GetMapping("/wallet")
    public ApiResponse<WalletResponse> getWallet(@CurrentUser Long userId) {
        User user = userMapper.selectById(userId);
        WalletResponse resp = new WalletResponse();
        resp.setBalance(user.getBalance());
        resp.setFrozenBalance(user.getFrozenBalance());
        return ApiResponse.ok(resp);
    }

    @GetMapping("/wallet/bills")
    public ApiResponse<PageResult<BillItemResponse>> getBills(
            @CurrentUser Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(transactionService.getBills(userId, page, size));
    }
}
