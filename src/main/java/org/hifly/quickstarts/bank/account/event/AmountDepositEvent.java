package org.hifly.quickstarts.bank.account.event;

public class AmountDepositEvent {

    private String accountId;

    private Double amount;

    public AmountDepositEvent(String accountId, Double amount) {
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
