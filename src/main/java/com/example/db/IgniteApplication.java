package com.example.db;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import com.example.db.test.Person;
import com.example.db.test.Score;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.SqlFieldsQuery;
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
		boolean debug = true;
		if(debug) return;

		/*
		 * IgniteConfiguration cfg = new IgniteConfiguration(); cfg.setClientMode(true);
		 * cfg.setPeerClassLoadingEnabled(false); String[] ips = new String[]{
		 * "127.0.0.1" }; if(ips != null && ips.length > 0) { ArrayList<String> ipList =
		 * new ArrayList<String>(); for(String ip : ips) { ipList.add(ip +
		 * ":47500..47509"); } TcpDiscoverySpi discoverySpi = new TcpDiscoverySpi();
		 * //Tcp多播发现地址 TcpDiscoveryMulticastIpFinder ipFinder = new
		 * TcpDiscoveryMulticastIpFinder(); ipFinder.setAddresses(ipList);
		 * 
		 * discoverySpi.setIpFinder(ipFinder); cfg.setDiscoverySpi(discoverySpi); }
		 * Ignite ignite = Ignition.start(cfg);
		 */

		// pom.xml里h2的版本必须为特定版本
		Ignite ignite = Ignition.start("/root/docker/apache-ignite-2.9.1-bin/config/rdbms-config2.xml");
		System.out.println("ignite start success ...... ");

		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					IgniteCache<Integer, Person> personCache = ignite.getOrCreateCache("PersonCache");
					if(personCache != null) {
						personCache.loadCache(null);
						System.out.println("ignite loadCache person success ......" + personCache);
					} else {
						System.out.println("personCache is null");
					}

					IgniteCache<Integer, Score> scoreCache = ignite.getOrCreateCache("ScoreCache");
					if(scoreCache != null) {
						scoreCache.loadCache(null);
						System.out.println("ignite loadCache score success ......" + scoreCache);
					} else {
						System.out.println("scoreCache is null");
					}

					QueryCursor<List<?>> cursor = personCache
							.query(new SqlFieldsQuery("select * from Person").setSchema("PersonCache"));
					cursor.getAll().stream().forEach((data) -> {
						System.out.println("start....");
						// data.stream().forEach(System.out::println);
						data.stream().forEach(item -> {
							System.out.print(item + "\t");
						});
						System.out.println("\nend....");
					});
					cursor.close();

					System.out.println("=======================================");
					String query = "SELECT p.ID, p.name, s.score FROM  \"PersonCache\".PERSON AS p LEFT JOIN \"ScoreCache\".SCORE AS s ON p.ID=s.fkId";
					cursor = personCache.query(new SqlFieldsQuery(query));
					System.out.println("start....");
					AtomicInteger aInteger = new AtomicInteger(0);
					cursor.getAll().stream().forEach((data) -> {
						System.out.print(String.format("%02d. ", aInteger.incrementAndGet()));
						// data.stream().forEach(System.out::println);
						data.stream().forEach(item -> {
							System.out.print(item + "\t");
						});
						System.out.println();
					});
					cursor.close();
					System.out.println("\nend....");

					//clear表示清空内存里的数据，下次load的时候会重新从磁盘中读取
					scoreCache.clear();

					//personCache.destroy();
					 personCache.close();
					//scoreCache.destroy();
					 scoreCache.close();

					try {
						Thread.sleep(5 * 1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					// IgniteCache<Integer, Score> scoreCache2 =
					// ignite.getOrCreateCache("ScoreCache");
					// ContinuousQuery<Integer, Score> continueQuery = new ContinuousQuery<>();
					// continueQuery.setLocalListener(new CacheEntryUpdatedListener<Integer,
					// Score>() {
					// @Override
					// public void onUpdated(Iterable<CacheEntryEvent<? extends Integer, ? extends
					// Score>> events)
					// throws CacheEntryListenerException {
					// System.out.println("ContinuousQuer yonUpdated *******************");
					// }
					// });
					// System.out.println("[[[[[[[[[[[[[[[[[[[[[[[[");
					// QueryCursor<Entry<Integer, Score>> cursor2 =
					// scoreCache2.query(continueQuery);
					// for (Cache.Entry<Integer, Score> item : cursor2) {
					// System.out.println(item.getKey() + " => " + item.getValue());
					// };
					// cursor2.close();
					// System.out.println("]]]]]]]]]]]]]]]]]]]]]]]]");
				}
			}
		}).start();
	}
}