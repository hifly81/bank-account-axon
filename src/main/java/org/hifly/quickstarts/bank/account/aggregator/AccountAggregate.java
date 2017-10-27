package org.hifly.quickstarts.bank.account.aggregator;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventhandling.EventHandler;
import org.hifly.quickstarts.bank.account.command.CreateAccountCommand;
import org.hifly.quickstarts.bank.account.command.DepositAmountCommand;
import org.hifly.quickstarts.bank.account.command.WithdrawalAmountCommand;
import org.hifly.quickstarts.bank.account.event.AccountCreatedEvent;
import org.hifly.quickstarts.bank.account.event.AmountWithdrawalDeniedEvent;
import org.hifly.quickstarts.bank.account.event.AmountDepositEvent;
import org.hifly.quickstarts.bank.account.event.AmountWithdrawalEvent;
import org.hifly.quickstarts.bank.account.model.Account;
import org.hifly.quickstarts.bank.account.queryManager.AccountInMemoryView;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

public class AccountAggregate {

    @AggregateIdentifier
    private String accountId;

    public AccountAggregate() { }

    @CommandHandler
    public AccountAggregate(CreateAccountCommand command) {
        apply(new AccountCreatedEvent(command.getAccountId(), command.getCustomerName()));
    }

    @EventHandler
    public void on(AccountCreatedEvent event) {
        this.accountId = event.getAccountId();
    }


    @CommandHandler
    public void deposit(DepositAmountCommand command) {
        apply(new AmountDepositEvent(command.getAccountId(), command.getAmount()));
    }

    @CommandHandler
    public void withdrawal(WithdrawalAmountCommand command) {
        Account account = AccountInMemoryView.accounts.get(command.getAccountId());
        if(account.getBalance() < command.getAmount())
            apply(new AmountWithdrawalDeniedEvent(command.getAccountId(), command.getAmount(), account.getBalance()));
        else
            apply(new AmountWithdrawalEvent(command.getAccountId(), command.getAmount()));
    }

}
