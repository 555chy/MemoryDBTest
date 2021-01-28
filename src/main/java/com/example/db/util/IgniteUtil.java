package com.example.db.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;

import com.example.db.callback.ExecuteCallback;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteCluster;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cluster.ClusterMetrics;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.lang.IgniteCallable;
import org.apache.ignite.lang.IgniteRunnable;
import org.apache.ignite.resources.IgniteInstanceResource;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;

/**
 * 内存访问比磁盘IO要快得多，以至于许多人希望通过部署分布式内存集群来获得惊人的性能提升
 * 但不要忽略应用是通过网络与集群节点互联的事实，如果大量数据通过网络连续传输，会抵消内存访问性能高的优势
 * 
 * 部署可扩展的水平Ignite集群的目的是充分利用分布在所有主机上的RAM和CPU资源，并且尽可能小的受到网络的影响
 */
public class IgniteUtil {

    /**
     * 通过配置项启动ignite
     * @param ips 集群IP
     */
    public static Ignite start(boolean clientMode, String[] ips) {
        // 使用JavaAPI配置Ignite
        IgniteConfiguration cfg = new IgniteConfiguration();
        // 以客户端节点模式启动
        cfg.setClientMode(clientMode);

        /*
        peerClass对等类
        对等类加载（P2P类加载）实现的，它是Ignite中的一个特别的分布式类加载器，实现了节点间的字节码交换。
        当对等类加载启用时，不需要在网格内的每个节点上手工地部署Java或者Scala代码，也不需要每次在发生变化时重新部署。
        工作原理：
        1.Ignite会检查类是否在本地CLASSPATH中可用（是否在系统启动时加载），如果有效，就会被返回，这时不会发生从对等节点加载类的行为。
        2.如果类在本地不可用，会向发起节点发送一个提供类定义的请求，发起节点会发送类字节码定义然后在工作节点上加载。这个过程每个类只会发生一次，即一旦一个节点上一个类定义被加载了，它就不会再次加载了。
        如果不采用对等类方式，则需要显示部署jar包到各个集群节点，可以将它们拷贝进每个集群节点的libs文件夹，Ignite会在启动时自动加载所有的libs文件夹中的jar文件。
        */
        cfg.setPeerClassLoadingEnabled(false);
        if(ips != null && ips.length > 0) {
            ArrayList<String> ipList = new ArrayList<String>();
            for(String ip : ips) {
                ipList.add(ip + ":47500..47509");
            }
            TcpDiscoverySpi discoverySpi = new TcpDiscoverySpi();
            //Tcp多播发现地址
            TcpDiscoveryMulticastIpFinder ipFinder = new TcpDiscoveryMulticastIpFinder();
            ipFinder.setAddresses(ipList);
           
            // TcpDiscoveryVmIpFinder ipFinder = new TcpDiscoveryMulticastIpFinder();
            // ipFinder.setAddresses(ipList);
            
            discoverySpi.setIpFinder(ipFinder);
            cfg.setDiscoverySpi(discoverySpi);
        }
        //使用配置启动ignite
        Ignite ignite = Ignition.start(cfg);
        return ignite;
    }

    /**
     * 初始化Ignite缓存表
     * @param <T>           数据类型
     * @param tableName     表名
     * @param expiryHours   有效期（单位：小时）
     */
    public static <T> IgniteCache<Long, T> getOrCreateCache(Ignite ignite, String tableName, T t, int expiryHours) {
        CacheConfiguration<Long, T> cfg = new CacheConfiguration<Long, T>();
        cfg.setName(tableName);
        //如果在查询过程 中想以表的形式查询（ignite提供了多种Sql的查询方式）
		//那么必须使用这一句说明索引类，否则只能以key-alue方式查询
		cfg.setIndexedTypes(Long.class, t.getClass());
        //存储方式，分布式存储（缓存有三种方式 Local，Backup，partition）
        cfg.setCacheMode(CacheMode.PARTITIONED);
        //CAP原则又称CAP定理，指的是在一个分布式系统中，一致性（Consistency）、可用性（Availability）、分区容错性（Partition tolerance）
        //Ignite默认情况下是以CacheAtomicityMode.ATOMIC模式,其实是不支持事务的，但是Ignite是会维护最终一致性的。所以，这时的系统其实是CAP下的AP模式，与绝大多数的内存数据库一样。
        //我们可以在CacheConfiguration中通过setAtomicityMode设置为TRANSACTIONAL事务模式，此时的系统就是CAP下的CP模式，它具有事务功能，Ignite也维护其最终一致性。
        cfg.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);
        //根据配置创建缓存
        IgniteCache<Long, T> cache = ignite.getOrCreateCache(cfg);
        //给缓存数据设置一个过期时间
        cache.withExpiryPolicy(new CreatedExpiryPolicy(new Duration(TimeUnit.HOURS, expiryHours)));
        return cache;
    }

    /**
     * 获取当前节点的信息
     */
    public static String getLocalInfo(Ignite ignite) {
        IgniteCluster cluster = ignite.cluster();
        ClusterNode localNode = cluster.localNode(); 
        UUID uuid = localNode.id();
        String osName = System.getProperty("os.name");
        String javaRuntime = System.getProperty("java.runtime.name");
        Collection<String> addresses = localNode.addresses();
        ClusterMetrics metrics = localNode.metrics();
        double cpuLoad = metrics.getCurrentCpuLoad();
        long memUsed = metrics.getHeapMemoryUsed();
        int totalCpus = metrics.getTotalCpus();
        int totalNodes = metrics.getTotalNodes();
        int activeJobs = metrics.getCurrentActiveJobs();
        StringBuilder sb = new StringBuilder();
        StringUtil.appendLine(sb, "uuid", uuid.toString());
        StringUtil.appendLine(sb, "osName", osName);
        StringUtil.appendLine(sb, "javaRuntime", javaRuntime);
        sb.append("address: ");
        for(String ip : addresses) {
            sb.append(ip);
            sb.append(", ");
        }
        sb.append("\n");
        StringUtil.appendLine(sb, "cpuLoad", cpuLoad);
        StringUtil.appendLine(sb, "heapMemoryUsed", memUsed);
        StringUtil.appendLine(sb, "totalCpus", totalCpus);
        StringUtil.appendLine(sb, "totalNodes", totalNodes);
        StringUtil.appendLine(sb, "activeJobs", activeJobs);
        return sb.toString();
    }

    /**
     * 并置执行
     * 将计算包装到Ignite的计算任务中，该任务将在服务端节点的本地数据集上执行，
     * 应用仅从服务端接收结果，而不再提取实际数据
     */
    public static void broadcastRunnable(Ignite _ignite, Runnable runnable) {
        _ignite.compute(_ignite.cluster().forServers()).broadcast(new IgniteRunnable(){
            private static final long serialVersionUID = -515591004282329436L;
            @IgniteInstanceResource
            Ignite ignite;

            @Override
            public void run() {
                runnable.run();
            }
        });
    }

    /**
     * 并置计算（带结果）
     */
    public static <T> Collection<T> broadcastCallable(Ignite ignite, ExecuteCallback<T> callback) {
        return ignite.compute(ignite.cluster().forServers()).broadcast(new IgniteCallable<T>(){
            private static final long serialVersionUID = 4396442574829737393L;
            @IgniteInstanceResource
            Ignite ignite;
            
            @Override
			public T call() throws Exception {
				return callback.execute();
			}
        });
    };
}
