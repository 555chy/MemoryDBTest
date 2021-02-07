package com.example.db.entity;

import java.sql.Timestamp;

import lombok.Data;

/**
 * 执行sql耗时统计
 */
@Data
public class Exec {
    public long id;
    /** 测试时间 */
    public Timestamp testTime;
    /** 数据库类型 */
    public String dbtype;
    /** 表名 */
    public String tbname;
    /** 数据行数 */
    public String nrows;
    /** 数据列数 */
    public byte ncols;
    /** sql语句类型 */
    public String abbr;
    /** sql语句 */
    public String cmd;
    /** 耗时 */
    public long cost;
    /** 内存总大小（单位：G） */
    public byte memG;
    /** 备注 */
    public String remark;

    /** 
     * 设置基本信息 
     */
    public Exec setBase(String dbtype, String tbname, int rows, int ncols, int memG, String remark) {
        this.dbtype = dbtype;
        this.tbname = tbname;
        this.nrows = String.valueOf(rows);
        this.ncols = (byte) ncols;
        this.memG = (byte) memG;
        this.remark = remark;
        return this;
    }

    /**
     * 设置记录信息
     */
    public Exec setInfo(String abbr, String cmd, long cost) {
        this.testTime = new Timestamp(System.currentTimeMillis());
        this.abbr = abbr;
        this.cmd = cmd;
        this.cost = cost;
        return this;
    }

    public Exec setInfo(String abbr, String cmd, long cost, String rows) {
        setInfo(abbr, cmd, cost);
        this.nrows = rows;
        return this;
    }

    /**
     * 获取join的行格式
     */
    public static String getStrRow(int rowA, int rowB) {
        return rowA + "x" + rowB;
    }
}
