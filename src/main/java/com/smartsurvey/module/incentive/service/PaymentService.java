package com.smartsurvey.module.incentive.service;

import cn.hutool.core.util.IdUtil;
import com.smartsurvey.common.exception.BusinessException;
import com.smartsurvey.common.exception.ErrorCode;
import com.smartsurvey.module.incentive.entity.Transaction;
import com.smartsurvey.module.incentive.mapper.TransactionMapper;
import com.smartsurvey.module.user.entity.User;
import com.smartsurvey.module.user.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class PaymentService {
    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private static final BigDecimal MIN_WITHDRAW = new BigDecimal("10.00");
    private static final BigDecimal MAX_WITHDRAW = new BigDecimal("5000.00");

    private final TransactionMapper transactionMapper;
    private final UserMapper userMapper;

    public PaymentService(TransactionMapper transactionMapper, UserMapper userMapper) {
        this.transactionMapper = transactionMapper;
        this.userMapper = userMapper;
    }

    @Transactional
    public void recharge(Long userId, BigDecimal amount) {
        if (amount.compareTo(new BigDecimal("10")) < 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "充值金额不能低于10元");
        }
        if (amount.compareTo(new BigDecimal("5000")) > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "单笔充值不能超过5000元");
        }

        User user = userMapper.selectById(userId);
        BigDecimal balanceBefore = user.getBalance();
        BigDecimal balanceAfter = balanceBefore.add(amount);
        user.setBalance(balanceAfter);
        userMapper.updateById(user);

        Transaction t = new Transaction();
        t.setTransactionNo(IdUtil.fastSimpleUUID());
        t.setUserId(userId);
        t.setType("recharge");
        t.setAmount(amount);
        t.setBalanceBefore(balanceBefore);
        t.setBalanceAfter(balanceAfter);
        t.setStatus("success");
        t.setRemark("账户充值");
        t.setCreatedAt(LocalDateTime.now());
        transactionMapper.insert(t);

        log.info("User {} recharged {}", userId, amount);
    }

    @Transactional
    public void withdraw(Long userId, BigDecimal amount) {
        if (amount.compareTo(MIN_WITHDRAW) < 0) {
            throw new BusinessException(ErrorCode.WITHDRAW_MINIMUM);
        }
        if (amount.compareTo(MAX_WITHDRAW) > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "单笔提现不能超过5000元");
        }

        User user = userMapper.selectById(userId);
        if (user.getBalance().compareTo(amount) < 0) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_BALANCE);
        }

        BigDecimal balanceBefore = user.getBalance();
        BigDecimal balanceAfter = balanceBefore.subtract(amount);
        user.setBalance(balanceAfter);
        userMapper.updateById(user);

        Transaction t = new Transaction();
        t.setTransactionNo(IdUtil.fastSimpleUUID());
        t.setUserId(userId);
        t.setType("withdraw");
        t.setAmount(amount.negate());
        t.setBalanceBefore(balanceBefore);
        t.setBalanceAfter(balanceAfter);
        t.setStatus("pending");
        t.setRemark("提现申请");
        t.setCreatedAt(LocalDateTime.now());
        transactionMapper.insert(t);

        log.info("User {} withdrew {}", userId, amount);
    }
}
