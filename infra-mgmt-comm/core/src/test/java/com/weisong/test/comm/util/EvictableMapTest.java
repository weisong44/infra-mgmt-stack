package com.weisong.test.comm.util;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

public class EvictableMapTest {
	
	private EvictableMap<String, Integer> map;
	
	@Before
	public void prepare() {
		map = new EvictableMap<>(2500);
	}
	
	@Test
	public void testOneEntry() throws Exception {
		// Preparation
		final AtomicBoolean evicted = new AtomicBoolean();
		map.setListener(new EvictableMap.Listener<String, Integer>() {
			@Override
			public void entryEvicted(String key, Integer value) {
				evicted.set(true);
			}
		});
		
		// Eviction check
		put("a1");
		Thread.sleep(1000); Assert.assertEquals(1, map.size());
		Thread.sleep(1000); Assert.assertEquals(1, map.size());
		Thread.sleep(2000); Assert.assertEquals(0, map.size());
		
		// Listener check
		Assert.assertEquals(true, evicted.get());
	}
	
	@Test
	public void testRefresh() throws Exception {
		put("a1");
		Thread.sleep(1000); Assert.assertEquals(1, map.size());
		map.refresh("a1"); // Resets the clock
		Thread.sleep(1000); Assert.assertEquals(1, map.size());
		Thread.sleep(1000); Assert.assertEquals(1, map.size());
		Thread.sleep(2000); Assert.assertEquals(0, map.size());
	}
	
	@Test
	public void testMoreEntries() throws Exception {
		putAndSleep("a1", 1000);
		putAndSleep("a2", 1000);
		putAndSleep("a3", 1000);
		Thread.sleep(200);
		Assert.assertEquals(2, map.size());
		
		putAndSleep("a2", 1000);
		Assert.assertEquals(2, map.size());
		
		put("a2");
		put("a3");
		Thread.sleep(1000);
		Assert.assertEquals(2, map.size());
		Thread.sleep(3000);
		Assert.assertEquals(0, map.size());
	}
	
	private void put(String key) 
			throws InterruptedException {
		map.put(key, nextValue());
	}
	
	private void putAndSleep(String key, long delay) 
			throws InterruptedException {
		put(key);
		Thread.sleep(delay);
	}
	
	static private AtomicInteger v = new AtomicInteger();
	private Integer nextValue() {
		return v.incrementAndGet();
	}
}
