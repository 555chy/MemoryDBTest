package com.example.db.mapper.db1;

import java.util.List;

import com.example.db.entity.Exec;

import org.springframework.stereotype.Repository;

@Repository
public interface ExecMapper {
    public List<Exec> selectAll();

    public void insert(Exec exec);

    public void delete();

    public void truncate();
}
