CREATE DATABASE IF NOT EXISTS dbtest;
use dbtest;
DROP TABLE IF EXISTS person;
-- ignite支持16种类型
CREATE TABLE IF NOT EXISTS person (
  id BIGINT(11) NOT NULL COMMENT '主键',
  uid UUID NOT NULL COMMENT '通用唯一标识符，长度128位',
  sex TINYINT(1) NOT NULL DEFAULT 0 COMMENT '性别：1-男，0-女',
  male BOOLEAN NOT NULL DEFAULT TRUE COMMENT '性别：true-男，false-女',
  name VARCHAR(25) NOT NULL DEFAULT '' COMMENT '姓名',
  age SMALLINT(2) NOT NULL DEFAULT 0 COMMENT '年龄',
  birthday TIMESTAMP COMMENT '出生时间戳',
  height REAL COMMENT '身高',
  weight INT COMMENT '体重',
  phone VARCHAR(20) COMMENT '电话',
  website VARCHAR(100) COMMENT '网址',
  wages DECIMAL DEFAULT 0 COMMENT '工资',
  month_sales REAL DEFAULT 0 COMMENT '月销售额',
  turnover DOUBLE DEFAULT 0 COMMENT '总营业额',
  hobby_count TINYINT COMMENT '爱好个数',
  hobby VARCHAR(255) COMMENT '爱好',
  data BINARY COMMENT 'byte[]格式数据',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

show variables like 'lower%'