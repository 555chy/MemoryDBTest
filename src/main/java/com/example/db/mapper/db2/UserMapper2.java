package com.example.db.mapper.db2;

import java.util.List;

import com.example.db.entity.User;

import org.springframework.stereotype.Repository;

@Repository
public interface UserMapper2 {
    List<User> selectAll();

    User select(int id);

    void insert(User user);
}