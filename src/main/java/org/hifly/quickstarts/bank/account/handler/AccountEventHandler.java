package org.hifly.quickstarts.bank.account.handler;

import org.axonframework.eventhandling.EventHandler;
import org.hifly.quickstarts.bank.account.event.AccountCreatedEvent;
import org.hifly.quickstarts.bank.account.event.AmountWithdrawalDeniedEvent;
import org.hifly.quickstarts.bank.account.event.AmountDepositEvent;
import org.hifly.quickstarts.bank.account.event.AmountWithdrawalEvent;
import org.hifly.quickstarts.bank.account.model.Account;
import org.hifly.quickstarts.bank.account.queryManager.AccountInMemoryView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AccountEventHandler {

    private final Logger LOG = LoggerFactory.getLogger(AccountEventHandler.class);

    @EventHandler
    public void handle(AccountCreatedEvent event) {
        AccountInMemoryView.accounts.put(event.getAccountId(), new Account(event.getAccountId(), event.getCustomerName()));
        LOG.info("account created {}, customer {} ", event.getAccountId(), event.getCustomerName());
    }

    @EventHandler
    public void handle(AmountDepositEvent event) {
        Account account = AccountInMemoryView.accounts.get(event.getAccountId());
        account.setBalance(account.getBalance() + event.getAmount());
        AccountInMemoryView.accounts.put(event.getAccountId(), account);
        LOG.info("account {}, deposit {} ", event.getAccountId(), event.getAmount());
    }

    @EventHandler
    public void handle(AmountWithdrawalEvent event) {
        Account account = AccountInMemoryView.accounts.get(event.getAccountId());
        account.setBalance(account.getBalance() - event.getAmount());
        LOG.info("account {}, withdrawal {} ", event.getAccountId(), event.getAmount());
    }

    @EventHandler
    public void handle(AmountWithdrawalDeniedEvent event) {
        LOG.info("account {}, withdrawal {} denied!!! - current balance {}", event.getAccountId(), event.getAmount(), event.getCurrentBalance());
    }

}