package com.smartsurvey.module.incentive.service;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartsurvey.common.dto.PageResult;
import com.smartsurvey.module.incentive.dto.BillItemResponse;
import com.smartsurvey.module.incentive.entity.Transaction;
import com.smartsurvey.module.incentive.mapper.TransactionMapper;
import com.smartsurvey.module.user.entity.User;
import com.smartsurvey.module.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionService {
    private final TransactionMapper transactionMapper;
    private final UserMapper userMapper;

    public TransactionService(TransactionMapper transactionMapper, UserMapper userMapper) {
        this.transactionMapper = transactionMapper;
        this.userMapper = userMapper;
    }

    @Transactional
    public Transaction createRewardTransaction(Long userId, BigDecimal amount, Long relatedId, String remark) {
        User user = userMapper.selectById(userId);
        BigDecimal balanceBefore = user.getBalance();
        BigDecimal balanceAfter = balanceBefore.add(amount);

        Transaction t = new Transaction();
        t.setTransactionNo(IdUtil.fastSimpleUUID());
        t.setUserId(userId);
        t.setType("reward");
        t.setAmount(amount);
        t.setBalanceBefore(balanceBefore);
        t.setBalanceAfter(balanceAfter);
        t.setRelatedId(relatedId);
        t.setRelatedType("response");
        t.setStatus("success");
        t.setRemark(remark);
        t.setCreatedAt(LocalDateTime.now());
        transactionMapper.insert(t);
        return t;
    }

    public PageResult<BillItemResponse> getBills(Long userId, int page, int size) {
        Page<Transaction> p = new Page<>(page, size);
        LambdaQueryWrapper<Transaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Transaction::getUserId, userId)
               .orderByDesc(Transaction::getCreatedAt);
        Page<Transaction> result = transactionMapper.selectPage(p, wrapper);

        List<BillItemResponse> list = new ArrayList<>();
        for (Transaction t : result.getRecords()) {
            BillItemResponse item = new BillItemResponse();
            item.setId(t.getId());
            item.setTransactionNo(t.getTransactionNo());
            item.setType(t.getType());
            item.setAmount(t.getAmount());
            item.setBalanceAfter(t.getBalanceAfter());
            item.setStatus(t.getStatus());
            item.setRemark(t.getRemark());
            item.setCreatedAt(t.getCreatedAt());
            list.add(item);
        }
        return new PageResult<>(page, size, result.getTotal(), list);
    }
}
