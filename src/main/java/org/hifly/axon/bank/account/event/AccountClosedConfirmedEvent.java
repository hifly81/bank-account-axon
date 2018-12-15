package org.hifly.axon.bank.account.event;

public class AccountClosedConfirmedEvent {

    private String accountId;

    private String customerName;


    public AccountClosedConfirmedEvent(String accountId, String customerName) {
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
