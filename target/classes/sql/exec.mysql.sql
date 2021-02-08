CREATE DATABASE IF NOT EXISTS dbtest;
use dbtest;
DROP TABLE IF EXISTS exec;
CREATE TABLE IF NOT EXISTS exec (
    id int(10) NOT NULL AUTO_INCREMENT,
    testTime TIMESTAMP NOT NULL COMMENT '测试时间',
    dbtype VARCHAR(20) NOT NULL COMMENT '数据库类型',
    tbname VARCHAR(20) NOT NULL COMMENT '表名',
    nrows VARCHAR(20) COMMENT '数据行数',
    ncols TINYINT COMMENT '数据列数',
    abbr VARCHAR(50) NOT NULL COMMENT 'sql语句类型',
    cmd VARCHAR(200) NOT NULL COMMENT 'sql语句',
    cost INTEGER NOT NULL COMMENT '耗时',
    memG TINYINT NOT NULL COMMENT '内存总大小（单位：G）',
    remark VARCHAR(100) COMMENT '备注',
    PRIMARY KEY(id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;