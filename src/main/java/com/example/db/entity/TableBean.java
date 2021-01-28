package com.example.db.entity;

public abstract class TableBean {
    public String getTableName() {
        return getClass().getSimpleName().toLowerCase();
    }
}
