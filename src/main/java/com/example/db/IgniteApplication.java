package com.example.db;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
DruidDataSourceAutoCo`nfigure会注入一个DataSourceWrapper，其会在原生的spring.datasource下找 url, username, password 等。动态数据源 URL 等配置是在 dynamic 下，因此需要排除，否则会报错
可以在yml中去除，也可以用代码去除
*/
@SpringBootApplication(exclude = DruidDataSourceAutoConfigure.class)
// @MapperScan("com.example.db.mapper")
public class IgniteApplication {

	public static void main(String[] args) {
		SpringApplication.run(IgniteApplication.class, args);
	}
}