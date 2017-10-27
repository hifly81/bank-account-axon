package org.hifly.quickstarts.bank.account.command;

import org.axonframework.commandhandling.TargetAggregateIdentifier;

public class DepositAmountCommand {

    @TargetAggregateIdentifier
    private String accountId;

    private Double amount;

    public DepositAmountCommand(String accountId, Double amount) {
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
