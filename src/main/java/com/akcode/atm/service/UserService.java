package com.akcode.atm.service;

import com.akcode.atm.model.UserRequest;
import com.akcode.atm.model.UserResponse;

public interface UserService {

    UserResponse create(UserRequest userRequest);

}
