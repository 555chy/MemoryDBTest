package com.example.db.server.db1.impl;

import java.util.List;

import com.example.db.entity.Exec;
import com.example.db.mapper.db1.ExecMapper;
import com.example.db.server.db1.ExecService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExecServiceImpl implements ExecService {
    @Autowired
	ExecMapper execMapper;
	
    @Override
    public List<Exec> selectAll() {
        return execMapper.selectAll();
    }

    @Override
    public void insert(Exec exec) {
        execMapper.insert(exec);
    }

    @Override
    public void delete() {
        execMapper.delete();
    }
}
