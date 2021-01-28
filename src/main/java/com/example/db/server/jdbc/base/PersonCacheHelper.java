package com.example.db.server.jdbc.base;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import com.example.db.entity.Person;
import com.example.db.util.CostUtil;
import com.example.db.util.IgniteUtil;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteAtomicSequence;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteCompute;
import org.apache.ignite.IgniteDataStreamer;
import org.apache.ignite.cache.query.FieldsQueryCursor;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.lang.IgniteRunnable;

public class PersonCacheHelper {

    /** 数据有效时长，单位小时 */
    private static final int EXPIRY_HOURS = 1;
    private String tableName;
    private Ignite ignite;
    private IgniteCache<Long, Person> igniteCache;
    private IgniteCompute igniteCompute;

    public PersonCacheHelper(boolean clientMode, String[] ips) {
        this.tableName = new Person().getTableName();
        this.ignite = IgniteUtil.start(clientMode, ips);
        IgniteUtil.broadcastRunnable(ignite, new Runnable() {

            @Override
            public void run() {
                String info = IgniteUtil.getLocalInfo(ignite);
                System.out.println(info);
            }
        });
    }

    public void createTable() {
        igniteCompute = ignite.compute(ignite.cluster().forServers());
        igniteCompute.broadcast(new IgniteRunnable() {
            private static final long serialVersionUID = 1L;

            @Override
            public void run() {
                if (igniteCache == null) {
                    igniteCache = IgniteUtil.getOrCreateCache(ignite, tableName, new Person(), EXPIRY_HOURS);
                }
            }
        });
    }

    /**
     * 根据主键查询（不能为空）
     */
    public void query(Set<Long> keys) {
        igniteCompute.broadcast(new IgniteRunnable() {

			@Override
			public void run() {
                //该方法会从各个节点一条一条的拉取数据
                //由于大量的网络往返，即使将数据保存在内存中，也和保存在硬盘上速度没有任何区别
                //igniteCache.get(1);
                //批量获取，通过减少网络时延加快速度
                Iterator<Person> it = igniteCache.getAll(keys).values().iterator();
                while(it.hasNext()) {
                    Person person = it.next();
                    System.out.println(person.toString());
                }
			}
        });
    }

    public void query() {
        igniteCompute.broadcast(new IgniteRunnable() {

			@Override
			public void run() {
                String sql = "SELECT * FROM " + tableName;
                SqlFieldsQuery query = new SqlFieldsQuery(sql);
                int count = igniteCache.query(query).getAll().size();
                System.out.println("query count = " + count);
			}
        });
    }

    public void insert(int count, int part) {
        igniteCompute.broadcast(new IgniteRunnable() {

			@Override
			public void run() {
                CostUtil costUtil = new CostUtil();
                costUtil.begin();
                List<Person> personList = new ArrayList<Person>();
                AtomicLong idGen = new AtomicLong();
                for(int i=0; i<count; i++) {
                    costUtil.pause();
                    Person person = new Person(idGen);
                    costUtil.resume();
                    //igniteCache.put(person.getId(), person);
                    personList.add(person);
                    /*
                    if(i % part == 0) {
                    // igniteCache.putAll(personList.stream().collect(Collectors.toMap(Person::getId, t -> t)));
                        IgniteAtomicSequence sequence = ignite.atomicSequence(tableName, 0, true);
                        try(IgniteDataStreamer<Long, Person> streamer = ignite.dataStreamer(tableName)) {
                            personList.stream().forEach(p -> streamer.addData(sequence.incrementAndGet(), p));
                            streamer.flush();
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                        personList.clear();
                    }
                    */
                }
                if(personList.size() != 0) {
                    //千万数据，单机270s
                    //igniteCache.putAll(personList.stream().collect(Collectors.toMap(Person::getId, t -> t)));
                    //千万数据，单机174s
                    IgniteAtomicSequence sequence = ignite.atomicSequence(tableName, 0, true);
                    try(IgniteDataStreamer<Long, Person> streamer = ignite.dataStreamer(tableName)) {
                        personList.stream().forEach(p -> streamer.addData(sequence.incrementAndGet(), p));
                        streamer.flush();
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("insert " + count + " row, cost " + costUtil.end() + "ms");
			}
        });
    }

    

    public void count() {
        igniteCompute.broadcast(new IgniteRunnable() {

			@Override
			public void run() {
                CostUtil cost = new CostUtil();
                //cache.setNam0e相当于是设置数据库名，而表名默认是类名
                String sql = "SELECT COUNT(*) FROM " + tableName;
                SqlFieldsQuery countQuery = new SqlFieldsQuery(sql);
                FieldsQueryCursor cursor = igniteCache.query(countQuery);
                List<List<Long>> countList = cursor.getAll();
                cursor.close();
                long count = 0;
                if (!countList.isEmpty()) {
                    count = countList.get(0).get(0);
                }
                System.out.println("count " + count + " row, cost " + cost.end() + "ms");
            }
        });
    }

    public void close() {
        if(igniteCache != null) {
            igniteCache.close();
            igniteCache = null;
        }
        if(ignite != null) {
            ignite.close();
            ignite = null;
        }
    }
}