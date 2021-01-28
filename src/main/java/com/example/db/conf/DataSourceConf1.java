package com.example.db.conf;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

@Configuration
@MapperScan(basePackages = "com.example.db.mapper.db1", sqlSessionFactoryRef = "db1SqlSessionFactory")
public class DataSourceConf1 {

    //表示默认数据源
    @Primary
    @Bean("db1DataSource")
    //读取application.yml中的配置参数映射成一个对象
    @ConfigurationProperties(prefix = "spring.datasource.db1")
    public DataSource getDb1DataSource() {
        System.out.println("getDb1DataSource");
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean("db1SqlSessionFactory")
    public SqlSessionFactory db1SqlSessionFactory(@Qualifier("db1DataSource") DataSource dataSource) throws Exception {
        System.out.println("db1SqlSessionFactory");
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        //mapper的xml必须要配置，不然会报：no statement 错误
        bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mapping/db1/*.xml"));
        return bean.getObject();
    }

    @Primary
    @Bean("db1SqlSessionTemplate")
    public SqlSessionTemplate db1SqlSessionTemplate(@Qualifier("db1SqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        System.out.println("db1SqlSessionTemplate");
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
