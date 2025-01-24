package com.akcode.atm.controller;

import com.akcode.atm.model.UserRequest;
import com.akcode.atm.model.UserResponse;
import com.akcode.atm.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping(value = "/create", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<UserResponse> create(@RequestBody UserRequest userRequest) {
        log.info("users > create > request - name: {}, email: {}",
                userRequest.getName(), userRequest.getEmail());
        return ResponseEntity.ok(userService.create(userRequest));
    }
}
