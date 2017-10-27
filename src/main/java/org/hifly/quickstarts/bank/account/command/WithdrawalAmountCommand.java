package org.hifly.quickstarts.bank.account.command;

import org.axonframework.commandhandling.TargetAggregateIdentifier;

public class WithdrawalAmountCommand {

    @TargetAggregateIdentifier
    private String accountId;

    private Double amount;

    public WithdrawalAmountCommand(String accountId, Double amount) {
        this.accountId = accountId;
        this.amount = amount;
    }

    public String getAccountId() {
        return accountId;
    }

    public Double getAmount() {
        return amount;
    }
}
