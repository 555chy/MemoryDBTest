package com.example.db.controller;

import java.util.List;

import com.example.db.entity.User;
import com.example.db.server.db2.UserService2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user2")
public class UserController2 {

    @Autowired
    private UserService2 userService;

    @GetMapping("/index")
    public String test() {
        return "2 hello world";
    }

    @GetMapping("/selectall")
    public List<User> selectAll() {
        System.out.println("2 select all");
        return userService.selectAll();
    }

    @GetMapping("/select/{id}")
    public User select(@PathVariable int id) {
        System.out.println("2 select one");
        return userService.select(id);
    }

    @GetMapping("/insert")
    public User insert(User user) {
        System.out.println("2 insert");
        userService.insert(user);
        return user;
    }
}
