package com.example.db.test;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class Person implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String name;
}
