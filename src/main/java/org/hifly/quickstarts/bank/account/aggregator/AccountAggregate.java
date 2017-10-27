package org.hifly.quickstarts.bank.account.aggregator;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventhandling.EventHandler;
import org.hifly.quickstarts.bank.account.command.CreateAccountCommand;
import org.hifly.quickstarts.bank.account.command.DepositAmountCommand;
import org.hifly.quickstarts.bank.account.command.WithdrawalAmountCommand;
import org.hifly.quickstarts.bank.account.event.AccountCreatedEvent;
import org.hifly.quickstarts.bank.account.event.AmountDepositEvent;
import org.hifly.quickstarts.bank.account.event.AmountWithdrawalEvent;

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
        apply(new AmountWithdrawalEvent(command.getAccountId(), command.getAmount()));
    }

}
