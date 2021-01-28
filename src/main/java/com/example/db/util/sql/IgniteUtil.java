package com.example.db.util.sql;

import com.example.db.entity.Person;

import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheWriteSynchronizationMode;
import org.apache.ignite.client.ClientCache;
import org.apache.ignite.client.ClientCacheConfiguration;
import org.apache.ignite.client.ClientException;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.configuration.ClientConfiguration;

public class IgniteUtil {
    public static ClientCache<Long, Person> getClient(String address) {
        ClientCache<Long, Person> clientCache = null;
        ClientConfiguration cfg = new ClientConfiguration();
        cfg.setAddresses(address);
        try(IgniteClient client = Ignition.startClient(cfg)) {
            ClientCacheConfiguration cacheCfg = new ClientCacheConfiguration();
            cacheCfg.setName("References");
            cacheCfg.setWriteSynchronizationMode(CacheWriteSynchronizationMode.FULL_ASYNC);
            clientCache = client.getOrCreateCache(cacheCfg);
        } catch(ClientException e) {
            e.printStackTrace();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return clientCache;
    }
}