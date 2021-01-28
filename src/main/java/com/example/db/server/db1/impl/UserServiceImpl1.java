package com.example.db.server.db1.impl;

import java.util.List;

import com.example.db.entity.User;
import com.example.db.mapper.db1.UserMapper1;
import com.example.db.server.db1.UserService1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl1 implements UserService1 {
    @Autowired
    UserMapper1 userMapper;

    public List<User> selectAll() {
        return userMapper.selectAll();
    }

    public User select(int id) {
        return userMapper.select(id);
    }

    public void insert(User user) {
        userMapper.insert(user);
    }
}
