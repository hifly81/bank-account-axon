package org.hifly.quickstarts.bank.account;

import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.hifly.quickstarts.bank.account.aggregator.AccountAggregate;
import org.hifly.quickstarts.bank.account.command.CreateAccountCommand;
import org.hifly.quickstarts.bank.account.command.DepositAmountCommand;
import org.hifly.quickstarts.bank.account.command.WithdrawalAmountCommand;
import org.hifly.quickstarts.bank.account.event.AccountCreatedEvent;
import org.hifly.quickstarts.bank.account.event.AmountDepositEvent;
import org.hifly.quickstarts.bank.account.event.AmountWithdrawalEvent;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

public class AccountBalanceTest {

    private FixtureConfiguration<AccountAggregate> fixture;

    @Before
    public void setUp() throws Exception {
        fixture = new AggregateTestFixture<AccountAggregate>(AccountAggregate.class);

    }

    @Test
    public void testCreateAccount() {
        String id = UUID.randomUUID().toString();
        String customerName = "kermit the frog";
        fixture.given()
                .when(new CreateAccountCommand(id, customerName))
                .expectEvents(new AccountCreatedEvent(id, customerName));
    }

    @Test
    public void testDeposit() {
        String id = UUID.randomUUID().toString();
        fixture.given(new AccountCreatedEvent(id, "kermit the frog"))
                .when(new DepositAmountCommand(id, 1000.0))
                .expectEvents(new AmountDepositEvent(id, 1000.0));
    }

    @Test
    public void testWithdrawal() {
        String id = UUID.randomUUID().toString();
        fixture.given(new AccountCreatedEvent(id, "kermit the frog"))
                .when(new WithdrawalAmountCommand(id, 1000.0))
                .expectEvents(new AmountWithdrawalEvent(id, 1000.0));
    }

}
