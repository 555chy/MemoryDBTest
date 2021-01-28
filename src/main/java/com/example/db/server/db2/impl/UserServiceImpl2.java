package com.example.db.server.db2.impl;

import java.util.List;

import com.example.db.entity.User;
import com.example.db.mapper.db2.UserMapper2;
import com.example.db.server.db2.UserService2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl2 implements UserService2 {
    @Autowired
    UserMapper2 userMapper;

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
