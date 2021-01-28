package com.example.db.server.jdbc.base;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.example.db.callback.DataCallback1;
import com.example.db.callback.DataCallback2;
import com.example.db.callback.ProcessRunnable;
import com.example.db.util.sql.JdbcUtil;
import com.example.db.util.sql.SqlUtil;

public abstract class JdbcHelper<T> {

    // private static final String SQL_PROCE_CALL = "{call store_procedure_insert(?,?,?,?,?,?)}";
    // private static final String SQL_PROCE = """
    //         create procedure store_procedure_insert
    //         @id LONG,
    //         @male BOOLEAN,
    //         @name VARCHAR,
    //         @age INT,
    //         @phoneNum CHAR(12),
    //         @province VARCHAR
    //         as
    //         insert into %s(id, male, name, age, phone, province)
    //         values(@id, @male, @name, @age, @phone, @province)
    //         """;

    public AtomicLong idGen;
    public Connection conn;
    /** （sql类型、字段名、值） */
    private String[][] table;
    private int fetchSize;
    public String dbType;
    protected String tableName;

    /**
     * 初始化
     * @param tableName 表名
     * @param ip              jdbc连接的IP地址
     * @param fetchSize   多少条命令成一组进行发送
     */
    public JdbcHelper(String dbType, String tableName, int fetchSize, String ip) {
        this.dbType = dbType;
        idGen = new AtomicLong(0);
        this.tableName = tableName;
        conn = JdbcUtil.getConnection(dbType);
        T t = random();
        table = SqlUtil.toTable(dbType, t, true);
        this.fetchSize = fetchSize;
    }

    public T random() {
        return random(false);
    }

    /** 创建随机对象用于插入 */
    public abstract T random(boolean resetId);

    public abstract T newInstance();

    public abstract void insert(PreparedStatement stat, int i);

    /** exasol不支持index */
    public String create(boolean isCreateIndex) {
        String id = "id";
        String sql = SqlUtil.sqlCreate(tableName, id, table);
        String[] indexs = SqlUtil.getIndexs(table, id);
        if(indexs.length == 0 || !isCreateIndex || !SqlUtil.isSupportIndex(dbType)) {
            JdbcUtil.exec(conn, sql);
        } else {
            String[] sqls = new String[indexs.length + 1];
            sqls[0] = sql;
            for(int i=0;i<indexs.length;i++) {
                sqls[i+1] = SqlUtil.sqlIndex(tableName, indexs[i]);
            }
            JdbcUtil.execBatch(conn, sqls, fetchSize);
        }
        return sql;
    }

    public String insert(int count) {
        String sql = SqlUtil.sqlInsert(tableName, table);
        JdbcUtil.execBatch(conn, sql, count, fetchSize, new DataCallback2<PreparedStatement, Integer>(){

            @Override
            public void onData(PreparedStatement stat, Integer i) {
                insert(stat, i);
            }
        });
        return sql;
    }

    public String update(int count, String[] updateColumns, DataCallback2<PreparedStatement,Integer> callback) {
        String sql = SqlUtil.sqlUpdate(tableName, updateColumns, "id=?");
        JdbcUtil.execBatch(conn, sql, count, fetchSize, callback);
        return sql;
    }

    public String query(boolean isShow) {
        return query(isShow, null, 0, 0);
    }

    public String query(boolean isShow, String where) {
        return query(isShow, where, 0, 0);
    }

    public String query(boolean isShow, String where, int from, int to) {
        return query(isShow, where, 0, 0, null);
    }

    public String query(boolean isShow, String where, int from, int to, String query) {
        String sql = SqlUtil.sqlQuery(tableName, where, from, to, query);
        JdbcUtil.query(conn, sql, fetchSize, new DataCallback1<ResultSet>(){

            @Override
            public void onData(ResultSet rs) {
                T t = newInstance();
                JdbcUtil.get(rs, t, dbType);
            }
        });
        return sql;
    }


    public List<T> querySync(boolean isShow) {
        return querySync(isShow, null, 0, 0);
    }

    public List<T> querySync(boolean isShow, String where) {
        return querySync(isShow, where, 0, 0);
    }

    public List<T> querySync(boolean isShow, String where, int from, int to) {
        return querySync(isShow, where, 0, 0, null);
    }

    public List<T> querySync(boolean isShow, String where, int from, int to, String query) {
        String sql = SqlUtil.sqlQuery(tableName, where, from, to, query);
        List<T> data = JdbcUtil.querySync(conn, sql, fetchSize, new ProcessRunnable<T, ResultSet>(){
            @Override
            public T process(ResultSet rs) {
                T t = newInstance();
                JdbcUtil.get(rs, t, dbType);
                return t;
            }
        });
        return data;
    }

    public String queryDistinct(boolean isShow, String where, int from, int to, String query) {
        String sql = SqlUtil.sqlQueryDistinct(tableName, where, from, to, query);
        JdbcUtil.query(conn, sql, fetchSize, new DataCallback1<ResultSet>(){

            @Override
            public void onData(ResultSet rs) {
                try {
                    String str = rs.getString(query);
                    if(isShow) System.out.println(str);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
        return sql;
    }
    
    public String delete() {
        String sql = SqlUtil.sqlDelete(tableName);
        JdbcUtil.exec(conn, sql);
        return sql;
    }

    public String drop() {
        String sql = SqlUtil.sqlDrop(tableName);
        JdbcUtil.exec(conn, sql);
        return sql;
    }

    public String group(boolean isShow, String groupBy) {
        return  group(isShow, groupBy, null);
    }

    public String group(boolean isShow, String groupBy, String where) {
        String sql = SqlUtil.sqlGroup(tableName, groupBy, where);
        JdbcUtil.query(conn, sql, fetchSize, new DataCallback1<ResultSet>(){
            @Override
            public void onData(ResultSet rs) {
                try {
                    Object group = rs.getObject(groupBy);
                    int count = rs.getInt(2);
                    if(isShow) System.out.println(
                        "groupBy " + group + ", count=" +  count
                    );
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
        return sql;
    }

    public String count(boolean isShow) { 
        return count(isShow, null);
    }
    
    public String count(boolean isShow, DataCallback1<Integer> callback) {
        String sql = SqlUtil.sqlCount(tableName);
        JdbcUtil.query(conn, sql, fetchSize, new DataCallback1<ResultSet>(){
            @Override
            public void onData(ResultSet rs) {
                try {
                    int count = rs.getInt(1);
                    if(isShow) System.out.println("count = " + rs.getInt(1));
                    if(callback != null) callback.onData(count);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
        return sql;
    }

    public String countYearMonth(boolean isShow, String timeCol) {
        String sql = SqlUtil.sqlCountYearMonth(tableName, timeCol);
        JdbcUtil.query(conn, sql, fetchSize, new DataCallback1<ResultSet>(){
            @Override
            public void onData(ResultSet rs) {
                try {
                    Object year = rs.getObject(SqlUtil.ALIAS_YEAR);
                    Object month = rs.getObject(SqlUtil.ALIAS_MONTH);
                    Object count = rs.getObject(SqlUtil.ALIAS_COUNT);
                    if(isShow) System.out.println(
                        "year = " + year + ", month = " + month + ", count = " + count
                    );
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
        return sql;
    }

    public String min(boolean isShow, String key) {
        String sql = SqlUtil.sqlMin(tableName, key);
        JdbcUtil.query(conn, sql, fetchSize, new DataCallback1<ResultSet>(){
            @Override
            public void onData(ResultSet rs) {
                try {
                    Object min = rs.getObject(SqlUtil.ALIAS_NAME);
                    if(isShow) System.out.println("min = " + min);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
        return sql;
    }

    public String max(boolean isShow, String key) {
        String sql = SqlUtil.sqlMax(tableName, key);
        JdbcUtil.query(conn, sql, fetchSize, new DataCallback1<ResultSet>(){
            @Override
            public void onData(ResultSet rs) {
                try {
                    Object max = rs.getObject(SqlUtil.ALIAS_NAME);
                    if(isShow) System.out.println("max = " + max);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
        return sql;
    }

    public String average(boolean isShow, String key) {
        String sql = SqlUtil.sqlAvg(tableName, key);
        JdbcUtil.query(conn, sql, fetchSize, new DataCallback1<ResultSet>(){
            @Override
            public void onData(ResultSet rs) {
                try {
                    Object avg = rs.getObject(SqlUtil.ALIAS_NAME);
                    if(isShow) System.out.println("avg = " + avg);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
        return sql;
    }

    public String sum(boolean isShow, String sum) {
        return sum(isShow, sum, null);
    }

    public String sum(boolean isShow, String sum, String where) {
        String sql = SqlUtil.sqlSum(tableName, sum, where);
        JdbcUtil.query(conn, sql, fetchSize, new DataCallback1<ResultSet>(){
            @Override
            public void onData(ResultSet rs) {
                try {
                    Object value = rs.getObject("total");
                    if(isShow) System.out.println(sum + " sum = " + value);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
        return sql;
    }

    public String order(boolean isShow, String orderBy) {
        return order(isShow, orderBy, null);
    }

    public String order(boolean isShow, String orderBy, String where) {
        return order(isShow, orderBy, where, 0, 0);
    }

    public String order(boolean isShow, String orderBy, String where, int from, int to) {
        String sql = SqlUtil.sqlOrder(tableName, orderBy, where, from, to);
        JdbcUtil.query(conn, sql, fetchSize, new DataCallback1<ResultSet>(){
            @Override
            public void onData(ResultSet rs) {
                // try {
                //     long id = rs.getLong("id");
                //     boolean male = rs.getBoolean("male");
                //     String name = rs.getString("name");
                //     int age = rs.getInt("age");
                //     String phone = rs.getString("phone");
                //     String province = rs.getString("province");
                //     if(isShow) System.out.println(
                //         "id = " + id +
                //         "male = " + male +
                //         "name = " + name + 
                //         "age = " + age +
                //         "phone = " + phone +
                //         "province = " + province
                //     );
                // } catch (SQLException e) {
                //     e.printStackTrace();
                // }
            }
        });
        return sql;
    }

    public String join(String sql, String tableA, String tableB, String[] columnA, String[] columnB) {
        return join(sql, tableA, tableB, columnA, columnB, null);
    }
    public String join(String sql, String tableA, String tableB, String[] columnA, String[] columnB, DataCallback1<ResultSet> callback) {
        return join(sql, tableA, tableB, columnA, columnB, callback, 0, 0);
    }
    public String join(String sql, String tableA, String tableB, String[] columnA, String[] columnB, DataCallback1<ResultSet> callback, int from, int to) {
        return join(sql, tableA, tableB, columnA, columnB, callback, from, to, null); 
    }
    public String join(String sql, String tableA, String tableB, String[] columnA, String[] columnB, DataCallback1<ResultSet> callback, int from, int to, String addition) {
        sql = SqlUtil.sqlJoin(sql, tableA, tableB, columnA, columnB, from, to);
        JdbcUtil.query(conn, sql, fetchSize, callback);
        return sql;
    } 

    // public String createProducedure() {
    //     String sql = String.format(SQL_PROCE, getTableName());
    //     JdbcUtil.exec(conn, sql);
    //      return sql;
    // }

    // public String callProducedure(int count) {
    //     JdbcUtil.execCall(conn, SQL_PROCE_CALL, count, fetchSize, new DataCallback2<PreparedStatement,Integer>(){

    //         @Override
    //         public void onData(PreparedStatement stat, Integer i) {
    //             PersonBean person = PersonBean.random();
    //             try {
    //                 stat.setLong(1, person.getId());
    //                 stat.setBoolean(2, person.isMale());
    //                 stat.setString(3, person.getName());
    //                 stat.setLong(4, person.getAge());
    //                 stat.setString(5, person.getPhone());
    //                 stat.setString(6, person.getProvince());
    //             } catch (SQLException e) {
    //                 e.printStackTrace();
    //             }
    //         }
    //     });
    //      return sql;
    // }

    public void close() {
        JdbcUtil.close(conn, null, null);
    }
}
