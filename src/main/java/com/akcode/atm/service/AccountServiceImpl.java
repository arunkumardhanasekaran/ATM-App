package com.akcode.atm.service;

import com.akcode.atm.constants.Constants;
import com.akcode.atm.entity.Account;
import com.akcode.atm.entity.Transaction;
import com.akcode.atm.exceptions.AccountDoesNotExistsException;
import com.akcode.atm.model.AccountRequest;
import com.akcode.atm.model.AccountResponse;
import com.akcode.atm.repository.AccountRepository;
import com.akcode.atm.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private void sendToKafka(String message) {
        kafkaTemplate.send("account-changes", message);
    }

    @Value("${enable-change-log}")
    private boolean enableChangeLog;

    private final Map<String, ReentrantLock> accountLocks = new HashMap<>();

    @Transactional
    public AccountResponse deposit(AccountRequest accountRequest) {
        long accountId = accountRequest.getAccountId();
        BigDecimal amount = accountRequest.getAmount();
        ReentrantLock lock = accountLocks.computeIfAbsent(String.valueOf(accountId), id -> new ReentrantLock());
        lock.lock();
        try {
            Optional<Account> accountOpt = accountRepository.findById(accountId);
            if (accountOpt.isEmpty()) {
                log.error("Account does not exists");
                throw new AccountDoesNotExistsException(Constants.ACCOUNT_DOES_NOT_EXISTS);
            } else {
                Account account = accountOpt.get();
                account.setBalance(account.getBalance().add(amount));
                accountRepository.save(account);
                log.debug("Amount deposited and total balance is {}", account.getBalance());

                Transaction transaction = new Transaction();
                transaction.setAccount(account);
                transaction.setAmount(amount);
                transaction.setType(Constants.DEPOSIT);
                transaction.setTimestamp(new Timestamp(System.currentTimeMillis()));
                transactionRepository.save(transaction);
                log.debug("Deposit transaction saved. AccountID: {}, Type: {}, Amount: {}", account.getId(),
                        transaction.getType(), transaction.getAmount());

                if (enableChangeLog) {
                    sendToKafka(Constants.DEPOSIT + Constants.SEPARATOR + accountId + Constants.SEPARATOR + amount);
                }

                return AccountResponse.builder()
                        .accountId(accountId)
                        .balance(account.getBalance())
                        .message(Constants.AMOUNT_DEPOSITED)
                        .build();
            }
        } finally {
            lock.unlock();
        }
    }


    @Transactional
    public AccountResponse withdraw(AccountRequest accountRequest) {
        long accountId = accountRequest.getAccountId();
        BigDecimal amount = accountRequest.getAmount();
        ReentrantLock lock = accountLocks.computeIfAbsent(String.valueOf(accountId), id -> new ReentrantLock());
        lock.lock();
        try {
            Optional<Account> accountOpt = accountRepository.findById(accountId);
            if (accountOpt.isEmpty()) {
                log.error("Account does not exists");
                throw new AccountDoesNotExistsException(Constants.ACCOUNT_DOES_NOT_EXISTS);
            } else {
                Account account = accountOpt.get();
                if (account.getBalance().compareTo(amount) >= 0) {
                    account.setBalance(account.getBalance().subtract(amount));
                    accountRepository.save(account);
                    log.debug("Amount withdrawn and total balance is {}", account.getBalance());

                    Transaction transaction = new Transaction();
                    transaction.setAccount(account);
                    transaction.setAmount(amount);
                    transaction.setType(Constants.WITHDRAW);
                    transaction.setTimestamp(new Timestamp(System.currentTimeMillis()));
                    transactionRepository.save(transaction);
                    log.debug("Withdraw transaction saved. AccountID: {}, Type: {}, Amount: {}", account.getId(),
                            transaction.getType(), transaction.getAmount());

                    if (enableChangeLog) {
                        sendToKafka(Constants.WITHDRAW + Constants.SEPARATOR + accountId + Constants.SEPARATOR + amount);
                    }

                    return AccountResponse.builder()
                            .accountId(accountId)
                            .balance(account.getBalance())
                            .message(Constants.AMOUNT_WITHDRAWN)
                            .build();
                } else {
                    log.debug("Account is having insufficient funds");
                    return AccountResponse.builder()
                            .accountId(accountId)
                            .balance(account.getBalance())
                            .message(Constants.INSUFFICIENT_FUNDS)
                            .build();
                }
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public AccountResponse checkBalance(long accountId) {
        Optional<Account> account = accountRepository.findById(accountId);
        if (account.isPresent()) {
            return AccountResponse.builder()
                    .accountId(accountId)
                    .balance(account.get().getBalance())
                    .message(Constants.DISPLAYING_TOTAL_BALANCE)
                    .build();
        } else {
            throw new AccountDoesNotExistsException(Constants.ACCOUNT_DOES_NOT_EXISTS);
        }
    }
}