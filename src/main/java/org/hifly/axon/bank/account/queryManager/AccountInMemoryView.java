package org.hifly.axon.bank.account.queryManager;

import org.hifly.axon.bank.account.model.Account;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AccountInMemoryView {

    public static Map<String, Account> accounts = Collections.synchronizedMap(new HashMap<>());
}
