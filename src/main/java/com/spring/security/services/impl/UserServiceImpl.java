package com.spring.security.services.impl;

import com.spring.security.persistence.entities.UserEntity;
import com.spring.security.persistence.repositories.UserRepository;
import com.spring.security.services.IUsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements IUsersService {
    @Autowired
    UserRepository userRepository;
    @Override
    public List<UserEntity> findAllUsers() {
        return userRepository.findAll();
    }
}
