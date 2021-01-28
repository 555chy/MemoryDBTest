package com.example.db.server.jdbc.base;

import java.sql.PreparedStatement;
import java.util.concurrent.atomic.AtomicLong;

import com.example.db.entity.Person;
import com.example.db.util.sql.JdbcUtil;

public class PersonHelper extends JdbcHelper<Person> {;
    
    public PersonHelper(String dbType, String tableName, int fetchSize, String ip) {
        super(dbType, tableName, fetchSize, ip);
        idGen = new AtomicLong(0);
    }

    @Override
    public Person random(boolean isReset) {
        if(isReset) idGen.set(0);
        return new Person(idGen);
    }

    @Override
    public Person newInstance() {
        return new Person();
    }

    @Override
    public void insert(PreparedStatement stat, int i) {
        Person person = new Person(idGen);
        JdbcUtil.set(stat, person, dbType);
    }
    
}
