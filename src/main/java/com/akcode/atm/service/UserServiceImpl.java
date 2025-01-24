package com.akcode.atm.service;

import com.akcode.atm.constants.Constants;
import com.akcode.atm.entity.Account;
import com.akcode.atm.entity.Transaction;
import com.akcode.atm.entity.User;
import com.akcode.atm.exceptions.UserAlreadyExistsException;
import com.akcode.atm.model.UserRequest;
import com.akcode.atm.model.UserResponse;
import com.akcode.atm.repository.AccountRepository;
import com.akcode.atm.repository.TransactionRepository;
import com.akcode.atm.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Value("${enable-change-log}")
    private boolean enableChangeLog;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private void sendToKafka(String message) {
        kafkaTemplate.send("account-changes", message);
    }

    @Transactional
    public UserResponse create(UserRequest userRequest) {
        String name = userRequest.getName();
        String email = userRequest.getEmail();

        List<User> existingUser = userRepository.findByNameAndEmail(name, email);
        if (!existingUser.isEmpty()) {
            log.error("User already exists");
            throw new UserAlreadyExistsException(Constants.USER_ALREADY_EXISTS);
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        User savedUser = userRepository.save(user);
        log.debug("User created. Name: {}", name);

        Account account = new Account();
        account.setUser(savedUser);
        account.setBalance(new BigDecimal(Constants.INITIAL_AMOUNT));
        accountRepository.save(account);
        log.debug("Account created. AccountId: {}", account.getId());

        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setAmount(new BigDecimal(Constants.INITIAL_AMOUNT));
        transaction.setType(Constants.ACCOUNT_CREATED);
        transaction.setTimestamp(new Timestamp(System.currentTimeMillis()));
        transactionRepository.save(transaction);
        log.debug("User creation transaction saved. AccountID: {}, Type: {}, Amount: {}",
                account.getId(), transaction.getType(), transaction.getAmount());

        if (enableChangeLog) {
            sendToKafka(Constants.ACCOUNT_CREATED + Constants.SEPARATOR + account.getId() + Constants.SEPARATOR + transaction.getAmount());
        }

        return UserResponse.builder()
                .accountId(account.getId())
                .balance(account.getBalance())
                .message(Constants.USER_CREATED)
                .build();
    }
}
