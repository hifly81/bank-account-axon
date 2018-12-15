package org.hifly.axon.bank.account.queryManager;

import org.hifly.axon.bank.account.model.Account;

import java.util.HashMap;
import java.util.Map;

public class AccountInMemoryView {

    public static Map<String, Account> accounts = new HashMap<>();
}
