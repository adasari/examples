package com.test.cache.loader;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteDataStreamer;
import org.apache.ignite.lang.IgniteCallable;
import org.apache.ignite.resources.IgniteInstanceResource;

import com.test.cache.entity.Test;

public class CacheLoader implements IgniteCallable<Long>{

	private int num;
	
	@IgniteInstanceResource
	private Ignite ignite;
	
	public CacheLoader(int num){
		this.num = num;
	}
	
	
	@Override
	public Long call() throws Exception {
	
		IgniteDataStreamer<String, Test> dataStreamer = ignite.dataStreamer("TEST_CACHE");
		
		for (int i = 1; i < 1000000; i++){
			dataStreamer.addData(num+"P"+i, new Test(("Test"+i), ("p"+i)));
			System.out.println("cahce loaded "+ i);
		}
		dataStreamer.close();
		return null;
	}

}
