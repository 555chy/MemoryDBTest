package com.example.db.entity;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import com.example.db.annotation.SqlVarLen;

public class Score extends TableBean {
    private static final int SCORE_MAX = 150;
    
    public long id;
    /** 英文名 */
    @SqlVarLen(20)
    public String englishName;
    /** 语文分数 */
    public short chinese;
    /** 数学分数 */
    public short math;
    /** 英语分数 */
    public short english;
    /** 用户ID，外键 */
    public long personId;
    /** 班级[1,6] */
    public byte classId;
    
    public Score() {}

    public Score(AtomicLong idGen) {
        id = idGen.getAndIncrement();
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<5;i++) {
            char c = i == 0 ? 'A' : 'a';
            sb.append((char)(c + random.nextInt(26)));
        }
        englishName = sb.toString();
        chinese = (byte) random.nextInt(SCORE_MAX);
        math = (byte) random.nextInt(SCORE_MAX);
        english = (byte) random.nextInt(SCORE_MAX);
        classId = (byte) (random.nextInt(6) + 1);
    }

}