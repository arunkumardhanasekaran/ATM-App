package com.akcode.atm.service;

import com.akcode.atm.model.AccountRequest;
import com.akcode.atm.model.AccountResponse;

public interface AccountService {

    AccountResponse deposit(AccountRequest accountRequest);

    AccountResponse withdraw(AccountRequest accountRequest);

    AccountResponse checkBalance(long accountId);
}
