package org.hifly.axon.bank.account.event;

public class AmountWithdrawalEvent {

    private String accountId;

    private Double amount;

    public AmountWithdrawalEvent(String accountId, Double amount) {
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
