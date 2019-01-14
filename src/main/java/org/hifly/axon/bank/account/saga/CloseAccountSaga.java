package org.hifly.axon.bank.account.saga;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.hifly.axon.bank.account.event.AccountClosedConfirmedEvent;
import org.hifly.axon.bank.account.event.AccountClosedDeniedEvent;
import org.hifly.axon.bank.account.event.AccountClosedEvent;
import org.hifly.axon.bank.account.event.AccountNotExistingEvent;
import org.hifly.axon.bank.account.model.Account;
import org.hifly.axon.bank.account.queryManager.AccountInMemoryView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.axonframework.eventhandling.GenericEventMessage.asEventMessage;
import static org.hifly.axon.bank.account.queryManager.AccountInMemoryView.accounts;

@ProcessingGroup("close-account")
public class CloseAccountSaga {

    private EventBus eventBus;

    private final Logger LOG = LoggerFactory.getLogger(CloseAccountSaga.class);

    public CloseAccountSaga() { }

    public CloseAccountSaga(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @StartSaga
    @SagaEventHandler(associationProperty = "accountId")
    public void handle(AccountClosedEvent event) {

        LOG.info("Received closed account event...");

        Account account = accounts.get(event.getAccountId());

        if(account == null) {
            EventMessage<AccountNotExistingEvent> message = asEventMessage(new AccountNotExistingEvent(event.getAccountId(), event.getCustomerName()));
            eventBus.publish(message);
            LOG.info("account not existing...");
            return;
        }

        if(account.getBalance() < 0) {
            EventMessage<AccountClosedDeniedEvent> message = asEventMessage(new AccountClosedDeniedEvent(event.getAccountId(), event.getCustomerName()));
            eventBus.publish(message);
            LOG.info("account existing <0...");
        }
        else {
            EventMessage<AccountClosedConfirmedEvent> message = asEventMessage(new AccountClosedConfirmedEvent(event.getAccountId(), event.getCustomerName()));
            eventBus.publish(message);
        }

    }

    @EndSaga
    @SagaEventHandler(associationProperty = "accountId")
    public void handle(AccountClosedConfirmedEvent event) {
        AccountInMemoryView.accounts.remove(event.getAccountId());
        LOG.info("account removed {}, customer {} ", event.getAccountId(), event.getCustomerName());
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "accountId")
    public void handle(AccountClosedDeniedEvent event) {
        LOG.info("account can't be removed {}, customer {} ", event.getAccountId(), event.getCustomerName());
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "accountId")
    public void handle(AccountNotExistingEvent event) {
        LOG.info("account not exists {}, saga ended - customer {} ", event.getAccountId(), event.getCustomerName());
    }



}
