package org.hifly.quickstarts.bank.account.event;

public class AccountClosedEvent {

    private String accountId;

    private String customerName;


    public AccountClosedEvent(String accountId, String customerName) {
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
