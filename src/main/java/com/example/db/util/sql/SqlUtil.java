package com.example.db.util.sql;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.sql.Clob;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.sql.rowset.serial.SerialClob;

import com.example.db.annotation.SqlVarLen;

import org.apache.ignite.cache.query.annotations.QuerySqlField;

//sql中有很多聚合函数，例如 COUNT、SUM、MIN 和 MAX，但是唯独没有乘法函数，
//logx+logy=logx*y，对数的相加等于指数的相乘，我们利用这个方式转换加法到乘法
//先对记录取对数log(),然后sum聚合，最后exp，结果就是记录相乘的结果
//select exp(sum(log(col))) from table where id<100
public class SqlUtil {
    public static boolean isShowSql = false;

    /** 类型，字段名，字段值，是否添加索引 */
    public static final int INDEX_TYPE = 0, INDEX_NAME = 1, INDEX_VALUE = 2,INDEX_IDX=3;

    /** SQL字段分割符 */
    private static final CharSequence SQL_SEP = ",";
    private static final CharSequence SQL_SPACE = " ";
    private static final CharSequence SQL_QUESTION = "?";
    private static final CharSequence SQL_EQUAL = "=";
    private static final CharSequence SQL_NEWLINE = "\n";

    /** 数据库类型 */
    public static final String DB_MYSQL = "mysql";
    //byte[]对应binary而非blob
    public static final String DB_IGNITE = "ignite";
    //不支持byte[]；不支持create index；bigdecimal太大，Decimal(36,36)；
    //AS的变量名里result、data、year、month、count都是关键词不能用（加前导下划线也不行）
    //OFFSET not allowed in LIMIT without ORDER BY，且order by必须在limit之前
    public static final String DB_EXASOL = "exasol";
    
    /** java对ignite的类型映射 */
    public static HashMap<Class<?>, String> CLS_MAPPER_IGNITE;
    public static HashMap<Class<?>, String> CLS_MAPPER_MYSQL;
    public static HashMap<Class<?>, String> CLS_MAPPER_EXASOL;
    static {
        CLS_MAPPER_IGNITE = new HashMap<Class<?>, String>() {{
            put(boolean.class, "BOOLEAN");
            put(Boolean.class, "BOOLEAN");
            put(int.class, "INT");
            put(Integer.class, "INT");
            put(byte.class, "TINYINT");
            put(Byte.class, "TINYINT");
            put(short.class, "SMALLINT");
            put(Short.class, "SMALLINT");
            put(long.class, "BIGINT");
            put(Long.class, "BIGINT");
            put(BigDecimal.class, "DECIMAL");
            put(float.class, "REAL");
            put(Float.class, "REAL");
            put(double.class, "DOUBLE");
            put(Double.class, "DOUBLE");
            put(Time.class, "TIME");
            put(Date.class, "DATE");
            put(Timestamp.class, "TIMESTAMP");
            put(String.class, "VARCHAR");
            put(UUID.class, "UUID");
            put(byte[].class, "BINARY");
            put(Byte[].class, "BINARY");
        }};
        CLS_MAPPER_MYSQL  = (HashMap<Class<?>, String>) CLS_MAPPER_IGNITE.clone();
        CLS_MAPPER_EXASOL = (HashMap<Class<?>, String>) CLS_MAPPER_IGNITE.clone();
        CLS_MAPPER_MYSQL.put(byte[].class, "BLOB");
        CLS_MAPPER_MYSQL.put(Byte[].class, "BLOB");
        CLS_MAPPER_EXASOL.remove(byte[].class);
        CLS_MAPPER_EXASOL.remove(Byte[].class);
        CLS_MAPPER_EXASOL.put(BigDecimal.class, "Decimal(36,36)");
        System.out.println("mysql byte = " + CLS_MAPPER_MYSQL.get(byte[].class));
        System.out.println("ignite byte = " + CLS_MAPPER_IGNITE.get(byte[].class));
        System.out.println("exasol byte = " + CLS_MAPPER_EXASOL.get(byte[].class));
    }

    public static HashMap<Class<?>, String> getClsMapper(String dbType) {
        HashMap<Class<?>, String> mapper = null;
        switch(dbType) {
            case DB_MYSQL:
                if(SqlUtil.isShowSql) System.out.println("use mysql mapper");
                mapper = CLS_MAPPER_MYSQL;
            break;
            case DB_IGNITE:
                if(SqlUtil.isShowSql) System.out.println("use ignite mapper");
                mapper = CLS_MAPPER_IGNITE;
            break;
            case DB_EXASOL:
                if(SqlUtil.isShowSql) System.out.println("use exasol mapper");
                mapper = CLS_MAPPER_EXASOL;
            break;
        }
        return mapper;
    }

    public static final String ALIAS_NAME = "tmp";
    public static final String ALIAS_YEAR= "y";
    public static final String ALIAS_MONTH = "m";
    public static final String ALIAS_COUNT = "c";

    //ignite不支持AUTO_INCREMENT
    private static final String SQL_CREATE = "CREATE TABLE IF NOT EXISTS %s (\n%s,\nCONSTRAINT ID PRIMARY KEY (%s)\n)";
    //ignite不支持在create时创建索引
    private static final String SQL_CREATE_WITH_INDEX = "CREATE TABLE IF NOT EXISTS %s (\n%s,\nINDEX idx(%s)\nCONSTRAINT ID PRIMARY KEY (%s)\n)";
    private static final String SQL_INDEX  = "CREATE INDEX idx_%s ON %s(%s)";
    private static final String SQL_INSERT = "INSERT INTO %s (%s) VALUES (%s)";
    private static final String SQL_DELETE = "DELETE FROM %s";
    private static final String SQL_UPDATE = "UPDATE %s SET %s WHERE %s";
    private static final String SQL_QUERY = "SELECT %s FROM %s";
    private static final String SQL_GROUP = "SELECT %s,COUNT(*) FROM %s GROUP BY %s";
    private static final String SQL_COUNT_YEAR_MONTH = "SELECT YEAR(%s) AS " + ALIAS_YEAR + ", MONTH(%s) AS " + ALIAS_MONTH + ", COUNT(*) AS " + ALIAS_COUNT + " FROM %s GROUP BY YEAR(%s),MONTH(%s)";
    private static final String SQL_COUNT = "SELECT COUNT(*) FROM %s";
    private static final String SQL_MIN = "SELECT MIN(%s) AS " + ALIAS_NAME + " FROM %s";
    private static final String SQL_MAX = "SELECT MAX(%s) AS " + ALIAS_NAME + " FROM %s";
    private static final String SQL_AVERAGE = "SELECT AVG(%s) AS " + ALIAS_NAME + " FROM %s";
    private static final String SQL_SUM = "SELECT SUM(%s) AS total FROM %s";
    private static final String SQL_ORDER = "SELECT * FROM %s ORDER BY %s";
    private static final String SQL_DROP = "DROP TABLE IF EXISTS %s";
    
    /** 内连接是最常见的一种连接，只连接匹配的行（交集 InnerJoin等价于join） */
    public static final String SQL_INNER_JOIN = "SELECT %s FROM tableA INNER JOIN tableB ON tableA.id=tableB.id";
    /** 内连接是最常见的一种连接，只连接匹配的行（Left+交集） */
    public static final String SQL_LEFT_JOIN = "SELECT %s FROM tableA LEFT JOIN tableB ON tableA.id=tableB.id";
    /** 内连接是最常见的一种连接，只连接匹配的行（Right+交集） */
    public static final String SQL_RIGHT_JOIN = "SELECT %s FROM tableA RIGHT JOIN tableB ON tableA.id=tableB.id";
    /** 内连接是最常见的一种连接，只连接匹配的行（不支持并集） */
    public static final String SQL_OUTER_JOIN = "SELECT %s FROM tableA OUTER JOIN tableB on tableA.id=tableB.id";

    private static String wrapSql(String sql) {
        if(isShowSql) System.out.println(sql + "\n");
        return sql;
    }

    public static String sqlCreate(String tableName, String id, String[][] table) {
        String[] columns = table[INDEX_NAME];
        String[] types = table[INDEX_TYPE];
        String[] indexs = getIndexs(table, id);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < columns.length; i++) {
            // if (!id.equals(columns[i])) {
            //跳过无效字段
            if(types[i] != null) {
                if (sb.length() != 0) {
                    sb.append(SQL_SEP);
                    sb.append(SQL_NEWLINE);
                }
                sb.append(columns[i]);
                sb.append(SQL_SPACE);
                sb.append(types[i]);
            }
            // }
        }
        String content = sb.toString();
        String sql;
        // if(indexs.length == 0) {
            sql = String.format(SQL_CREATE, tableName, content, id);
        // } else {
        //     sql = String.format(SQL_CREATE_WITH_INDEX, tableName, id, content, String.join(SQL_SEP, indexs));
        // }
        return wrapSql(sql);
    }
    
    public static boolean isSupportIndex(String dbType) {
        if(DB_EXASOL.equals(dbType)) return false;
        return true;
    }

    public static String sqlIndex(String tableName, String index) {
        String sql = String.format(SQL_INDEX, index, tableName, index);
        return wrapSql(sql);
    }

    public static String sqlInsert(String tableName, String[][] table) {
        List<String> keys = new ArrayList<String>();
        for(int i=0;i<table[INDEX_TYPE].length;i++) {
            if(table[INDEX_TYPE][i] != null) {
                keys.add(table[INDEX_NAME][i]);
            }
        }
        String[] values = new String[keys.size()];
        Arrays.fill(values, SQL_QUESTION);
        String sql = String.format(SQL_INSERT, tableName, String.join(SQL_SEP, keys), String.join(SQL_SEP, values));
        return wrapSql(sql);
    }

    public static String sqlDelete(String tableName) {
        return sqlDelete(tableName, null);
    }

    public static String sqlDelete(String tableName, String where) {
        String sql = String.format(SQL_DELETE, tableName);
        sql = addWhere(sql, where);
        return wrapSql(sql);
    }

    public static String sqlUpdate(String tableName, String[] keys, String where) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < keys.length; i++) {
            if (sb.length() != 0) {
                sb.append(SQL_SEP);
            }
            sb.append(keys[i]);
            sb.append(SQL_EQUAL);
            sb.append(SQL_QUESTION);
        }
        String sql = String.format(SQL_UPDATE, tableName, sb.toString(), where);
        return wrapSql(sql);
    }

    public static String sqlQuery(String tableName) {
        return sqlQuery(tableName, null);
    }

    public static String sqlQuery(String tableName, String where) {
        return sqlQuery(tableName, where, 0, 0);
    }

    public static String sqlQuery(String tableName, String where, int from, int to) {
        return sqlQuery(tableName, where, from, to, null);
    }

    public static String sqlQuery(String tableName, String where, int from, int to, String query) {
        if(query == null) query = "*";
        String sql = String.format(SQL_QUERY, query, tableName);
        sql = addWhere(sql, where);
        sql = addLimit(sql, from, to);
        return wrapSql(sql);
    }
    
    /**
     * 查找不重复的字段
     */
    public static String sqlQueryDistinct(String tableName, String where, int from, int to, String colName) {
        if(colName == null) colName = "*";
        String sql = String.format(SQL_QUERY, "DISTINCT " +colName, tableName);
        sql = addWhere(sql, where);
        //exasol执行distinct，必须添加order by，且必须在limit前面
        sql = addOrderBy(sql, colName);
        sql = addLimit(sql, from, to);
        return wrapSql(sql);
    }

    public static String sqlGroup(String tableName, String groupBy) {
        return sqlGroup(tableName, groupBy, null);
    }

    public static String sqlGroup(String tableName, String groupBy, String where) {
        String newTableName = addWhere(tableName, where);
        String sql = String.format(SQL_GROUP, groupBy, newTableName, groupBy);
        return wrapSql(sql);
    }


    public static String sqlCount(String tableName) {
        return sqlCount(tableName, null);
    }

    public static String sqlCount(String tableName, String where) {
        String sql = String.format(SQL_COUNT, tableName);
        sql = addWhere(sql, where);
        return wrapSql(sql);
    }

    public static String sqlCountYearMonth(String tableName, String timeCol) {
        return sqlCountYearMonth(tableName, timeCol, null);
    }

    public static String sqlCountYearMonth(String tableName, String timeCol, String where) {
        String sql = String.format(SQL_COUNT_YEAR_MONTH, timeCol, timeCol, tableName, timeCol, timeCol);
        sql = addWhere(sql, where);
        return wrapSql(sql);
    }

    public static String sqlMax(String tableName, String key) {
        String sql = String.format(SQL_MAX, key, tableName);
        return wrapSql(sql);
    }

    public static String sqlMin(String tableName, String key) {
        String sql = String.format(SQL_MIN, key, tableName);
        return wrapSql(sql);
    }

    public static String sqlAvg(String tableName, String key) {
        String sql = String.format(SQL_AVERAGE, key, tableName);
        return wrapSql(sql);
    }
    
    public static String sqlSum(String tableName, String sum) {
        return sqlSum(tableName, sum, null);
    }

    public static String sqlSum(String tableName, String sum, String where) {
        String sql = String.format(SQL_SUM, sum, tableName);
        sql = addWhere(sql, where);
        return wrapSql(sql);
    }

    public static String sqlOrder(String tableName, String orderBy) {
        return sqlOrder(tableName, orderBy, null);
    }

    public static String sqlOrder(String tableName, String orderBy, String where) {
        return sqlOrder(tableName, orderBy, null, 0, 0);
    }

    public static String sqlOrder(String tableName, String orderBy, String where, int from, int to) {
        String newTableName = addWhere(tableName, where);
        String sql = String.format(SQL_ORDER, newTableName, orderBy);
        sql = addLimit(sql, from, to);
        return wrapSql(sql);
    }

    public static String sqlDrop(String tableName) {
        String sql = String.format(SQL_DROP, tableName);
        return wrapSql(sql);
    }

    /** 将表A和表B的字段拼接成select */
    public static String sqlJoin(String sql, String tableA, String tableB, String[] columnA, String[] columnB) {
        return sqlJoin(sql, tableA, tableB, columnA, columnB, 0, 0);
    }

    public static String sqlJoin(String sql, String tableA, String tableB, String[] columnA, String[] columnB, int from, int to) {
        return sqlJoin(sql, tableA, tableB, columnA, columnB, from, to, null);
    }

    public static String sqlJoin(String sql, String tableA, String tableB, String[] columnA, String[] columnB, int from, int to, String addition) {
        String select;
        //表A的列数
        int an = columnA == null ? 0 : columnA.length;
        //表B的列数
        int bn = columnB == null ? 0 : columnB.length;
        if(an == 0 && bn == 0) {
            select = "*";
        } else {
            String[] columns = new String[an + bn];
            for(int i=0;i<an;i++) {
                columns[i] = tableA + "." + columnA[i];
            }
            for(int i=0;i<bn;i++) {
                columns[an+i] = tableB + "." + columnB[i];
            }
            select = String.join(SQL_SEP, columns);
        }
        sql = sql.replace("tableA", tableA);
        sql = sql.replace("tableB", tableB);
        sql = String.format(sql, select);
        //exasol的OFFSET not allowed in LIMIT without ORDER BY，且order by必须在limit之前
        sql = addOrderBy(sql, tableA + ".id");
        sql = addLimit(sql, from, to);
        if(addition != null && addition.length() != 0) {
             sql = sql + " " + addition;
        }
        return wrapSql(sql);
    }

    /**
     * SQL CLOB 是内置类型，它将字符大对象 (Character Large Object) 存储为数据库表某一行中的一个列值 驱动程序使用 SQL
     * locator(CLOB) 实现 Clob 对象 在JDBC中所有的 String 都要求被转为 Clob
     */
    public static Clob str2clob(String str) {
        if (str == null)
            return null;
        try {
            return new SerialClob(str.toCharArray());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

     /**
     * SQL CLOB 是内置类型，它将字符大对象 (Character Large Object) 存储为数据库表某一行中的一个列值 驱动程序使用 SQL
     * locator(CLOB) 实现 Clob 对象 在JDBC中所有的 String 都要求被转为 Clob
     */
    public static String clob2Str(Clob clob) {
        if (clob == null)
            return null;
        try {
            Reader reader = clob.getCharacterStream();
            char[] str = new char[(int) clob.length()];
            reader.read(str);
            reader.close();
            return new String(str);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将对象转为字符串（如果是浮点数则保留两位）
     */
    public static String toStr(Object obj) {
        if (obj == null)
            return null;
        if (obj instanceof Float | obj instanceof Double) {
            DecimalFormat df = new DecimalFormat(".00");
            return df.format(obj);
        } else {
            return String.valueOf(obj);
        }
    }

    /**
     * 用类名作表名
     */
    public static String getTableName(Class<?> cls) {
        return cls.getSimpleName();
    }

    /**
     * 转为长度为3的数组（sql类型、字段名、值）
     */
    public static <T> String[][] toTable(String dbType, T t) {
        return toTable(dbType, t, true);
    }

    /**
     * 转为长度为3的数组（sql类型、字段名、值）
     * 
     * @param needNull 是否需要值为null的字段
     */
    public static <T> String[][] toTable(String dbType, T t, boolean needNull) {
        HashMap<Class<?>, String> mapper = getClsMapper(dbType);
        Class<?> cls = t.getClass();
        String[][] result = new String[4][];
        Field[] fields = cls.getFields();
        int fieldCount = 0;
        //计算字段个数
        for(int i=0;i<fields.length;i++) {
            if ((fields[i].getModifiers() & Modifier.STATIC) == 0) {
                fieldCount++;
            }
        }
        for(int i=0;i<result.length;i++) {
            result[i] = new String[fieldCount];
        }
        for (int i = 0, index = 0; i < fields.length; i++) {
            Field field = fields[i];
            //剔除静态变量
            if ((field.getModifiers() & Modifier.STATIC) == 0) {
                try {
                    Object value = field.get(t);
                    if(value != null || needNull) {
                        boolean isStr = field.getType().equals(String.class);

                        Class<?> type = field.getType();
                        result[INDEX_TYPE][index] = mapper.get(type);
                        if(isStr) {
                            SqlVarLen len = field.getDeclaredAnnotation(SqlVarLen.class);
                            result[INDEX_TYPE][index]  += ("(" + len.value() + ")");
                        }

                        result[INDEX_NAME][index] = field.getName();

                        result[INDEX_VALUE][index] = toStr(value);
                        QuerySqlField qsf = field.getAnnotation(QuerySqlField.class);
                        result[INDEX_IDX][index] = qsf != null && qsf.index() == true ? "y" : null;
                        if (isStr && (result[INDEX_VALUE][index] != null)) {
                            result[INDEX_VALUE][index] = "'" + result[INDEX_VALUE][index] + "'";
                        } 
                        index++;
                    }
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    /** 
     * 根据table获取索引列
     */
    public static String[] getIndexs(String[][] table, String id) {
        ArrayList<String> indexs = new ArrayList<String>();
        for(int i=0; i<table[INDEX_NAME].length; i++) {
            if(table[INDEX_IDX][i] != null) {
                String name = table[INDEX_NAME][i];
                if(!id.equals(name)) {
                    indexs.add(name);
                }
            }
        }
        return indexs.toArray(new String[0]);
    }

    /** 在表名后面组装上where */
    public static String addWhere(String sql, String where) {
        if(where == null) return sql;
        StringBuilder sb = new StringBuilder();
        sb.append(sql);
        sb.append(" WHERE ");
        sb.append(where);
        return sb.toString();
    }

    public static String addOrderBy(String sql, String colName) {
        if(colName == null) return sql;
        StringBuilder sb = new StringBuilder();
        sb.append(sql);
        sb.append(" ORDER BY ");
        sb.append(colName);
        return sb.toString();
    }

    /** 添加取数限制 */
    public static String addLimit(String sql, int from, int to) {
        if(from == 0 && to == 0) return sql;
        StringBuilder sb = new StringBuilder();
        sb.append(sql);
        sb.append(" LIMIT ");
        sb.append(from);
        sb.append(",");
        sb.append(to);
        return sb.toString();
    }

    /**
     * 获取类名
     */
    public static String getClassName(Class<?> cls) {
        return cls.getSimpleName();
    }
}
