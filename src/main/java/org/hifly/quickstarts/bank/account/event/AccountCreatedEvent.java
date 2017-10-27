package org.hifly.quickstarts.bank.account.event;

public class AccountCreatedEvent {

    private String accountId;

    private String customerName;


    public AccountCreatedEvent(String accountId, String customerName) {
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
