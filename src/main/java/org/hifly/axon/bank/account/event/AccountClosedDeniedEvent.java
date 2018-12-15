package org.hifly.axon.bank.account.event;

public class AccountClosedDeniedEvent {

    private String accountId;

    private String customerName;


    public AccountClosedDeniedEvent(String accountId, String customerName) {
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
