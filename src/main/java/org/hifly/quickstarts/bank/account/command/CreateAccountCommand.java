package org.hifly.quickstarts.bank.account.command;

import org.axonframework.commandhandling.TargetAggregateIdentifier;

public class CreateAccountCommand {

    @TargetAggregateIdentifier
    private String accountId;

    private String customerName;

    public CreateAccountCommand(String accountId, String customerName) {
        this.accountId = accountId;
        this.customerName = customerName;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getCustomerName() {
        return customerName;
    }

}
