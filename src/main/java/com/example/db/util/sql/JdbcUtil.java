package com.example.db.util.sql;

import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.example.db.callback.DataCallback1;
import com.example.db.callback.DataCallback2;
import com.example.db.callback.ProcessRunnable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// SQL类型      JDBC对应方法      返回类型(共13种)
// bit(1)       getBoolean      Boolean 
//  bit(n)      getBytes        Byte 
// tinyint      getByte         Byte 
// smallint     getShort        Short 
// int          getInt          Int 
// bigint       getLong         Long 
// float        getFloat        Float 
// double       getDouble       Double 
// char,varchar,longvarchar getString String 
// text(clob)   getClob         Clob 
// blob         getBlob         Blob 
// date         getDate         java.sql.Date 
// time         getTime         java.sql.Time 
// timestamp    getTimestamp    java.sql.Timestamp(CURRENT_TIMESTAMP增加更新时，会自动设置默认值)
public class JdbcUtil {

    public static final String DRIVER_SQL_SERVER = "com.microsoft.jdbc.sqlserver.SQLServerDriver";
    public static final String DRIVER_MYSQL = "com.mysql.jdbc.Driver";
    public static final String DRIVER_IGNITE = "org.apache.ignite.IgniteJdbcThinDriver";
    public static final String DRIVER_EXASOL = "com.exasol.jdbc.EXADriver";

    public static final String URL_SQL_SERVER = "jdbc:microsoft:sqlserver://%s/dbtest?useUnicode=true&amp;characterEncoding=utf-8";
    public static final String URL_ORACLE = "jdbc:oracle:thin:@%s/dbtest?useUnicode=true&amp;characterEncoding=utf-8";
    public static final String URL_MYSQL = "jdbc:mysql://%s/dbtest?useUnicode=true&amp;characterEncoding=utf-8";
    public static final String URL_IGNITE = "jdbc:ignite:thin://%s;schema=PUBLIC";
    //jdbc:exa:<ipaddress>:<port>;schema=<schemaname>
    // public static final String URL_EXASOL = "jdbc:exa:%s:8563;schema=dbtest";
    public static final String URL_EXASOL = "jdbc:exa:%s:8563;schema=dbtest;clientname=DBeaver;clientversion=7.3.2.202101032114;querytimeout=600;connecttimeout=1000";

    /** 是否显示中间过程 */
    public static boolean isShowPart = false;
    public static String IP_MYSQL = "114.115.160.23";
    public static String IP_IGNITE = "127.0.0.1";
    public static String IP_EXASOL = "192.168.1.204";

    public static class DBInfo {
        public String type;
        public String driver;
        public String url;
        public String username;
        public String password;
        public String ip;

        public DBInfo(String type) {
            this.type = type;
            switch(type) {
                case SqlUtil.DB_MYSQL:
                    driver = DRIVER_MYSQL;
                    url = URL_MYSQL;
                    ip = IP_MYSQL;
                    break;
                case SqlUtil.DB_IGNITE:
                    driver = DRIVER_IGNITE;
                    url = URL_IGNITE;
                    ip = IP_IGNITE;
                    break;
                case SqlUtil.DB_EXASOL:
                    driver = DRIVER_EXASOL;
                    url = URL_EXASOL;
                    ip = IP_EXASOL;
                    username = "sys";
                    password = "exasol";
                    break;
            }
            if(url != null && ip != null) {
                url = String.format(url, ip);
            }
        }
    }

    /**
     * 获取连接器
     */
    public static Connection getConnection(String type) {
        DBInfo dbInfo = new DBInfo(type);
        System.out.println(dbInfo.url);
        try {
            // 加载驱动
            Class.forName(dbInfo.driver);
            // 得到连接
            return DriverManager.getConnection(dbInfo.url, dbInfo.username, dbInfo.password);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Jdbc程序运行完后，切记要释放程序在运行过程中，创建的那些与数据库进行交互的对象 释放资源的代码一定要放在finally中
     */
    public static void close(Connection conn, Statement stat, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (stat != null) {
            try {
                stat.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 查询、聚合函数
     * 
     * @param conn      连接器
     * @param sql       查询语句
     * @param fetchSize 一次读取的行数
     * @param callback  返回数据
     */
    public static void query(Connection conn, String sql, int fetchSize, DataCallback1<ResultSet> callback) {
        PreparedStatement stat = null;
        ResultSet rs = null;
        try {
            conn.setAutoCommit(false);
            // 流式查询(百万行可以提升400ms)
            stat = conn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            // Fetch相当于读缓存，JDBC默认每次检索，会从游标中提取10行记录到内存中，下次执行rs.next，就直接使用内存读取，不用和数据库交互了，
            // 增大Fetch Size的值可以减少交互次数，效率也会高些，但值越高则占用内存越高，要避免出现OOM错误。
            // 百万行可以提升200ms
            // stat.setFetchSize(fetchSize);
            rs = stat.executeQuery();
            if(callback != null && rs != null) {
                while (rs.next()) {
                    callback.onData(rs);
                }
            }
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(null, stat, rs);
        }
    }

/**
     * 查询、聚合函数
     * 
     * @param conn      连接器
     * @param sql       查询语句
     * @param fetchSize 一次读取的行数
     * @param callback  返回数据
 * @return 
     */
    public static <T, P> List<T> querySync(Connection conn, String sql, int fetchSize, ProcessRunnable<T, ResultSet> runnable) {
        PreparedStatement stat = null;
        ResultSet rs = null;
        List<T> data = new ArrayList<>();
        try {
            conn.setAutoCommit(false);
            // 流式查询(百万行可以提升400ms)
            stat = conn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            // Fetch相当于读缓存，JDBC默认每次检索，会从游标中提取10行记录到内存中，下次执行rs.next，就直接使用内存读取，不用和数据库交互了，
            // 增大Fetch Size的值可以减少交互次数，效率也会高些，但值越高则占用内存越高，要避免出现OOM错误。
            // 百万行可以提升200ms
            // stat.setFetchSize(fetchSize);
            rs = stat.executeQuery();
            if(runnable != null && rs != null) {
                while (rs.next()) {
                    data.add(runnable.process(rs));
                }
            }
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(null, stat, rs);
        }
        return data;
    }

    /**
     * 增、删、改（CRUD）
     * 
     * @param conn 连接器
     * @param sql  完整的sql语句
     */
    public static boolean execUpdate(Connection conn, String sql) {
        Statement stat = null;
        try {
            conn.setAutoCommit(false);
            stat = conn.createStatement();
            // executeUpdate()注重及时性，每写一条sql语句就发送给数据库保存起来，没有缓存，这样频繁操作数据库效率非常低
            int num = stat.executeUpdate(sql);
            conn.commit();
            return num > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(null, stat, null);
        }
        return false;
    }

    /**
     * 建库、建表、备份数据库（DDL）
     * 
     * @param conn 连接器
     * @param sql  完整的sql语句 create database xxx create table xxx backup database xxx
     *             to disk=D:/test.bak
     */
    public static boolean exec(Connection conn, String sql) {
        PreparedStatement prepStat = null;
        try {
            conn.setAutoCommit(false);
            prepStat = conn.prepareStatement(sql);
            boolean result = prepStat.execute();
            conn.commit();
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(null, prepStat, null);
        }
        return false;
    }

    /**
     * 批量执行多条SQL
     * 
     * @param conn       连接器
     * @param sqls       完整sql语句
     * @param batchCount 每批次条数
     */
    public static void execBatch(Connection conn, String[] sqls, int batchCount) {
        Statement stat = null;
        try {
            conn.setAutoCommit(false);
            stat = conn.createStatement();
            for (int i = 0; i < sqls.length; i++) {
                stat.addBatch(sqls[i]);
                if (i != 0 && i % batchCount == 0) {
                    int[] result = stat.executeBatch();
                    stat.clearBatch();
                }
            }
            int[] result = stat.executeBatch();
            stat.clearBatch();
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(null, stat, null);
        }
    }

    /**
     * 批量执行预编译的SQL
     * 
     * @param conn       连接器
     * @param sql        值部分用?占位的sql
     * @param count      总条数
     * @param batchCount 每批次条数
     * @param callback   为每条语句填充数据
     */
    public static void execBatch(Connection conn, String sql, int count, int batchCount,
            DataCallback2<PreparedStatement, Integer> callback) {
        PreparedStatement stat = null;
        try {
            if(JdbcUtil.isShowPart) System.out.println("execBatch " + conn.hashCode() + ", count = " + count);
            conn.setAutoCommit(false);
            stat = conn.prepareStatement(sql);
            int left = batchCount;
            for (int i = 0; i < count; i++) {
                // record(i + " / " + count, null);
                // 设置参数
                callback.onData(stat, i);
                stat.addBatch();
                left--;
                if (left <= 0) {
                    left = batchCount;
                    int[] result = stat.executeBatch();
                    // System.out.println(StringUtil.join(",", result));
                    stat.clearBatch();
                }
            }
            if(left > 0 && left != batchCount) {
                int[] result = stat.executeBatch();
            }
            // System.out.println(StringUtil.join(",", result));
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(null, stat, null);
        }
    }

    /**
     * 执行存储过程
     * 
     * @param conn       连接器
     * @param sql        存储过程sql
     * @param count      总条数
     * @param batchCount 每批次条数
     * @param callback   为每条语句填充数据
     * 
     * create procedure store_procedure_insert
     * @c_no nvarchar(50),
     * @c_name nvarchar(50),
     * @t_name nvarchar(50) as insert into t_course(c_no,c_name,t_name)
     * values(@c_no,@c_name,@t_name)
     * 
     * {call store_procedure_insert(?,?,?)}
     */
    public static void execCall(Connection conn, String sql, int count, int batchCount,
            DataCallback2<PreparedStatement, Integer> callback) {
        CallableStatement stat = null;
        try {
            conn.setAutoCommit(false);
            stat = conn.prepareCall(sql);
            for (int i = 0; i < count; i++) {
                // 设置参数
                callback.onData(stat, i);
                stat.addBatch();
                if (i != 0 && i % batchCount == 0) {
                    stat.executeBatch();
                    stat.clearBatch();
                }
            }
            stat.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(null, stat, null);
        }
    }

    /**
     * 自动设置PreparedStatement的键值（索引从1开始）
     */
    public static void set(PreparedStatement stat, int i, Object value) throws SQLException {
        if (value == null)
            return;
        if (value instanceof Boolean) {
            record("setBoolean", value, i);
            stat.setBoolean(i, (boolean) value);
        } else if (value instanceof Byte) {
            record("setByte", value, i);
            stat.setByte(i, (byte) value);
        } else if (value instanceof Short) {
            record("setShort", value, i);
            stat.setShort(i, (short) value);
        } else if (value instanceof Integer) {
            record("setInt", value, i);
            stat.setInt(i, (int) value);
        } else if (value instanceof Long) {
            record("setLong", value, i);
            stat.setLong(i, (long) value);
        } else if (value instanceof Float) {
            record("setFloat", value, i);
            stat.setFloat(i, (float) value);
        } else if (value instanceof Double) {
            record("setDouble", value, i);
            stat.setDouble(i, (double) value);
        } else if(value instanceof BigDecimal) {
            record("setBigDecimal", value, i);
            stat.setBigDecimal(i, (BigDecimal)value);
        } else if (value instanceof String) {
            record("setString", value, i);
            stat.setString(i, String.valueOf(value));
        } else if (value instanceof Clob) {
            record("setClob", value, i);
            stat.setClob(i, (Clob) value);
        } else if (value instanceof Blob) {
            record("setBlob", value, i);
            stat.setBlob(i, (Blob) value);
        } else if (value instanceof Date) {
            record("setDate", value, i);
            stat.setDate(i, (Date) value);
        } else if (value instanceof Time) {
            record("setTime", value, i);
            stat.setTime(i, (Time) value);
        } else if (value instanceof Timestamp) {
            record("setTimestamp", value, i);
            stat.setTimestamp(i, (Timestamp) value);
        } else if (value instanceof byte[]) {
            record("setBytes", value, i);
            stat.setBytes(i, (byte[]) value);
        } else {
            System.out.println("not found " + i);
        }
    }

    /**
     * 自动对PreparedStatement为类里的每个public对象设置 getDeclaredFiled
     * 仅能获取类本身的属性成员（包括私有、共有、保护） getField 仅能获取类(及其父类) public属性成员
     * 只能用在insert，update必须手动写
     */
    public static void set(PreparedStatement stat, Object obj, String dbType) {
        if (obj == null)
            return;
        Field[] fields = obj.getClass().getFields();
        HashMap<Class<?>, String> mapper = SqlUtil.getClsMapper(dbType);
        for (int i = 0, index = 1; i < fields.length; i++) {
            Field field = fields[i];
            if ((field.getModifiers() & Modifier.STATIC) == 0) {
                Class<?> type = field.getType();
                if(mapper.containsKey(type)) {
                    try {
                        set(stat, index++, field.get(obj));
                    } catch (IllegalArgumentException | IllegalAccessException | SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static void get(ResultSet rs, Object obj, String dbType) {
        Field[] fields = obj.getClass().getFields();
        HashMap<Class<?>, String> mapper = SqlUtil.getClsMapper(dbType);
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            if ((field.getModifiers() & Modifier.STATIC) == 0) {
                Class<?> type = field.getType();
                if(mapper.containsKey(type)) {
                    String name = field.getName();
                    Object value = null;
                    try {
                        if (type == Boolean.class) {
                            record("getBoolean", value);
                            value = rs.getBoolean(name);
                        } else if (type == Byte.class) {
                            record("getByte", value);
                            value = rs.getByte(name);
                        } else if (type == Short.class) {
                            record("getShort", value);
                            value = rs.getShort(name);
                        } else if (type == Integer.class) {
                            record("getInt", value);
                            value = rs.getInt(name);
                        } else if (type == Long.class) {
                            record("getLong", value);
                            value = rs.getLong(name);
                        } else if (type == Float.class) {
                            record("getFloat", value);
                            value = rs.getFloat(name);
                        } else if (type == Double.class) {
                            record("getDouble", value);
                            value = rs.getDouble(name);
                        } else if(type == BigDecimal.class) {
                            record("getBigDecimal", value);
                            value = rs.getBigDecimal(name);
                        } else if (type == String.class) {
                            record("getString", value);
                            value = rs.getString(name);
                        } else if (type == Clob.class) {
                            record("getClob", value);
                            value = rs.getClob(name);
                        } else if (type == Blob.class) {
                            record("getBlob", value);
                            value = rs.getBlob(name);
                        } else if (type == Date.class) {
                            record("getDate", value);
                            value = rs.getDate(name);
                        } else if (type == Time.class) {
                            record("getTime", value);
                            value = rs.getTime(name);
                        } else if (type == Timestamp.class) {
                            record("getTimestamp", value);
                            value = rs.getTimestamp(name);
                        } else if (type == byte[].class) {
                            record("getBytes", value);
                            value = rs.getBytes(name);
                        }
                        if (value != null)  field.set(obj, value);
                    } catch (SQLException e) {
                        e.printStackTrace(); 
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static void record(String name, Object value) {
        record(name, value, null);
    }

    public static void record(String name, Object value, Integer index) {
        if(!JdbcUtil.isShowPart) return;
        StringBuilder sb = new StringBuilder();
        if(index != null) {
            sb.append(index);
            sb.append(". ");
        }
        sb.append(name);
        if(value != null) {
            sb.append(" => ");
            sb.append(value);
        }
        System.out.println(sb.toString());
    }

}