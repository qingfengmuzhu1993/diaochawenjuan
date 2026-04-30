package com.smartsurvey.module.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartsurvey.common.dto.ApiResponse;
import com.smartsurvey.common.dto.PageResult;
import com.smartsurvey.module.incentive.entity.Transaction;
import com.smartsurvey.module.incentive.mapper.TransactionMapper;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/finance")
public class AdminFinanceController {
    private final TransactionMapper transactionMapper;
    public AdminFinanceController(TransactionMapper transactionMapper) { this.transactionMapper = transactionMapper; }

    @GetMapping("/transactions")
    public ApiResponse<PageResult<Transaction>> listTransactions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String type) {
        Page<Transaction> p = new Page<>(page, size);
        LambdaQueryWrapper<Transaction> wrapper = new LambdaQueryWrapper<>();
        if (type != null) wrapper.eq(Transaction::getType, type);
        wrapper.orderByDesc(Transaction::getCreatedAt);
        Page<Transaction> result = transactionMapper.selectPage(p, wrapper);
        return ApiResponse.ok(new PageResult<>(page, size, result.getTotal(), result.getRecords()));
    }
}
