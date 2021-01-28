package com.example.db.entity;

import java.sql.Timestamp;
import java.util.concurrent.atomic.AtomicLong;

import com.example.db.annotation.SqlVarLen;
import com.example.db.util.DataGenUtil;

import org.apache.ignite.cache.query.annotations.QuerySqlField;
import org.apache.ignite.cache.query.annotations.QueryTextField;

import lombok.Data;

@Data
public class Person extends TableBean {
    /*
      * @QuerySqlField 这样的注解仅仅可以使得该属性是Ignite可见的，不带盖注解的属性，Ignite是不可见的。
      * @QuerySqlField 表示此字段允许条件查询
      * 如果想设置为索引项，那么就要像示例中，加上index=true属性
      * 如果希望以sql的语句查询Person类 请不要再Person类中定义Map之类的复杂数据结构，否则将造成不能使用sql进行查询
      */
    @QuerySqlField(index = true)
    public long id;

    /** 通用唯一标识符，长度128位（16字节） */
    @QuerySqlField(index = true)
    @SqlVarLen(50)
    public String uid;

    /** 性别：1-男；0-女 */
    @QuerySqlField
    public byte sex;

    /** 性别：true-男；false-女 */
    @QuerySqlField
    public boolean male;

     // 创建基于Lucene的索引
     @QueryTextField
     @SqlVarLen(10)
    public String name;

      /** 年龄 [0,100] */
    @QuerySqlField(index = true)
    public short age;

    /** 出生时间戳 */
    public Timestamp birthday;
    
    /** 身高 [1.00,2.00] */
    @QuerySqlField(index = true)
    public float height;

    /** 体重 [0,100] */
    @QuerySqlField(index = true)
    public short weight;

    /** 电话号码 */
    @SqlVarLen(20)
    public String phone;
    
    /** 网址 */
    @SqlVarLen(50)
    public String website;

    /** 工资 [0.00,20000.00] */
    @QuerySqlField(index = true)
    public double wages;

    /** 月销售额 */
    @QuerySqlField(index = true)
    public float month_sales;
    
    /** 总销售额 */
    @QuerySqlField(index = true)
    // public BigDecimal turnover;
    public double turnover;
    
    /** 省份 */
    @QueryTextField
    @SqlVarLen(30)
    public String province;

    /** 学校 */
    @QueryTextField
    @SqlVarLen(30)
    public String school;

    /** 年级 */
    @QueryTextField
    @SqlVarLen(20)
    public String grade;

    /** 爱好个数 */
    @QuerySqlField(index = true)
    public byte hobby_count;

    /** 一组多个爱好 */
    @QueryTextField
    @SqlVarLen(50)
    public String hobbies;

    /** 数据 */
    public byte[] data;

    public Person() {}
    
    public Person(AtomicLong idGen) {
        id = idGen.getAndIncrement();
        uid = DataGenUtil.uuid().toString();
        male = DataGenUtil.isMale();
        sex = DataGenUtil.sex();
        name = DataGenUtil.name(male);
        age = (short) DataGenUtil.age();
        birthday = DataGenUtil.birthday(age);
        height = DataGenUtil.height();
        weight = (short)DataGenUtil.weight(height);
        phone = DataGenUtil.phone();
        website = DataGenUtil.website();
        wages = DataGenUtil.wages();
        month_sales = DataGenUtil.monthSales();
        turnover = DataGenUtil.turnover().doubleValue();
        province = DataGenUtil.province();
        school = DataGenUtil.school(province);
        grade = DataGenUtil.grade();
        hobby_count = (byte) DataGenUtil.hobbyCount();
        hobbies = DataGenUtil.hobbies(hobby_count);
        data = name.getBytes();
    }

}
