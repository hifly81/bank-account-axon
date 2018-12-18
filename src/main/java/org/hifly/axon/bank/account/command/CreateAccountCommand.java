package org.hifly.axon.bank.account.command;


import org.axonframework.modelling.command.TargetAggregateIdentifier;

public class CreateAccountCommand {

    @TargetAggregateIdentifier
    protected String accountId;
    protected String customerName;

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
