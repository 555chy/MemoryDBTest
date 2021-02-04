package com.example.db.server.db1;

import java.util.List;

import com.example.db.entity.Exec;

public interface ExecService {
    List<Exec> selectAll();
    void insert(Exec exec);
    void delete();
    void truncate();
}
