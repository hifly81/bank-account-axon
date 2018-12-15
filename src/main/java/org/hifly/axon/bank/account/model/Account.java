package org.hifly.axon.bank.account.model;

public class Account {

    private String id;
    private String customerName;
    private Double balance = 0.0;

    public Account(String id, String customerName) {
        this.id = id;
        this.customerName = customerName;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }
}
