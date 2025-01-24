package com.akcode.atm.controller;

import com.akcode.atm.model.AccountRequest;
import com.akcode.atm.model.AccountResponse;
import com.akcode.atm.service.AccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @PostMapping(value = "/deposit", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<AccountResponse> deposit(@RequestBody AccountRequest accountRequest) {
        log.info("accounts > deposit > request - accountId: {}, amount: {}",
                accountRequest.getAccountId(), accountRequest.getAmount());
        return ResponseEntity.ok(accountService.deposit(accountRequest));
    }

    @PostMapping(value = "/withdraw", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<AccountResponse> withdraw(@RequestBody AccountRequest accountRequest) {
        log.info("accounts > withdraw > request - accountId: {}, amount: {}",
                accountRequest.getAccountId(), accountRequest.getAmount());
        return ResponseEntity.ok(accountService.withdraw(accountRequest));
    }

    @GetMapping(value = "/checkBalance", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<AccountResponse> checkBalance(@RequestParam long accountId) {
        log.info("accounts > checkBalance > request - accountId: {}", accountId);
        return ResponseEntity.ok(accountService.checkBalance(accountId));
    }
}
