package com.example.db.controller;

import java.util.List;

import com.example.db.entity.User;
import com.example.db.server.db1.UserService1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user1")
public class UserController1 {

    @Autowired
    private UserService1 userService;

    @GetMapping("/index")
    public String test() {
        return "1 hello world";
    }

    @GetMapping("/selectall")
    public List<User> selectAll() {
        System.out.println("1 select all");
        return userService.selectAll();
    }

    @GetMapping("/select/{id}")
    public User select(@PathVariable int id) {
        System.out.println("1 select one");
        return userService.select(id);
    }

    @GetMapping("/insert")
    public User insert(User user) {
        System.out.println("1 insert");
        userService.insert(user);
        return user;
    }
}