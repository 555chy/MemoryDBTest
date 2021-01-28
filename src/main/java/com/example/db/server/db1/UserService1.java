package com.example.db.server.db1;

import java.util.List;

import com.example.db.entity.User;

//注解在 service 实现或 mapper 接口方法上，不要同时在 service 和 mapper 注解。
// @DS("db1")
public interface UserService1 {
    List<User> selectAll();
    User select(int id);
    void insert(User user);
}