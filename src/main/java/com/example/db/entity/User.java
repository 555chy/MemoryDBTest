package com.example.db.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * query中要给boolean参数赋值，需要省略它的is
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Integer id;
    private String name;
    private int age;
    private boolean sex;
    private String addr;
}