package com.example.db.test;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class Score implements Serializable {
    private static final long serialVersionUID = 2L;
    private int id;
    private int fkId;
    private int score;
}
