CREATE DATABASE IF NOT EXISTS db_test;
use db_test;
-- DROP TABLE IF EXISTS `user`;
CREATE TABLE IF NOT EXISTS `user` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(25) NOT NULL COMMENT '姓名',
  `age` int(2) DEFAULT NULL COMMENT '年龄',
  `sex` tinyint(1) NOT NULL DEFAULT '0' COMMENT '性别：0-男，1-女',
  `addr` varchar(100) DEFAULT NULL COMMENT '地址',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4;

INSERT INTO user(name, age, sex, addr) VALUES ('tina', 29, 1, "福建省福州市台江区");
INSERT INTO user(name, age, sex, addr) VALUES ('bob', 30, 0, "福建省厦门");
SELECT * FROM user;

show variables like 'lower%'