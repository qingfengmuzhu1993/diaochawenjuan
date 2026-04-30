package com.smartsurvey.module.incentive.dto;

import java.math.BigDecimal;

public class WalletResponse {
    private BigDecimal balance;
    private BigDecimal frozenBalance;

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public BigDecimal getFrozenBalance() { return frozenBalance; }
    public void setFrozenBalance(BigDecimal frozenBalance) { this.frozenBalance = frozenBalance; }
}
