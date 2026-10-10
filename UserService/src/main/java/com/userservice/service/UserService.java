package com.userservice.service;

import com.userservice.entity.User;

import java.util.List;

public interface UserService {

    //create
    User saveUser(User user);

    //get all users
    List<User> getAllUsers();

    // get user by id
    User getUser(String userId);

    //delete user by id
    void deleteUser(String userId);

    //update user
    User updateUser(User user);
}

