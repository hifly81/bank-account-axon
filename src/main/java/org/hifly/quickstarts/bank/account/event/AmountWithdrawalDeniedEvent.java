package org.hifly.quickstarts.bank.account.event;

public class AmountWithdrawalDeniedEvent {

    private String accountId;

    private Double amount;

    private Double currentBalance;

    public AmountWithdrawalDeniedEvent(String accountId, Double amount, Double currentBalance) {
        this.accountId = accountId;
        this.amount = amount;
        this.currentBalance = currentBalance;
    }

    public String getAccountId() {
        return accountId;
    }

    public Double getAmount() { return amount;}

    public Double getCurrentBalance() { return currentBalance;}

}
