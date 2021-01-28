package com.example.db.mapper.db1;

import java.util.List;

import com.example.db.entity.User;

import org.springframework.stereotype.Repository;

@Repository
public interface UserMapper1 {
    List<User> selectAll();

    User select(int id);

    void insert(User user);
}