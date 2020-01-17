package com.test.cache.loader;

import java.util.UUID;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.configuration.CacheConfiguration;

import com.test.cache.entity.Test;

import io.vertx.core.Vertx;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.spi.cluster.ClusterManager;

public class CacheManager {
	private static Ignite instance;
	
	public static Ignite getInstance(Vertx vertx){
		if (null == instance){
			synchronized (CacheManager.class) {
				if (null == instance) {
					ClusterManager clusterManager = ((VertxInternal) vertx).getClusterManager();
					String uuid = clusterManager.getNodeID();
					instance = Ignition.ignite(UUID.fromString(uuid));
//					getTestCache();
				}
			}
		}
		
		return instance;
	}
	
	public static Ignite getInstance(){
		return instance;
	}
	
	public static IgniteCache<String, Test> getTestCache(){
/*		CacheConfiguration<String, Test> dummyConfig = new CacheConfiguration<String, Test>();
		dummyConfig.setName("TEST_CACHE");		
		dummyConfig.setIndexedTypes(String.class, Test.class);
		dummyConfig.setBackups(0);
		dummyConfig.setQueryParallelism(2);
		dummyConfig.setCacheMode(CacheMode.PARTITIONED);
		dummyConfig.setCopyOnRead(false);		
*/		
		return getInstance().getOrCreateCache("TEST_CACHE");
	}
}
