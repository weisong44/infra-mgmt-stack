package com.weisong.test.comm.util;

import java.util.concurrent.atomic.AtomicBoolean;

import junit.framework.Assert;

import org.junit.Test;

import com.weisong.test.comm.util.EvictableList;

public class EvictableListTest {
	@Test
	public void testOneEntry() throws Exception {
		final AtomicBoolean evicted = new AtomicBoolean();
		EvictableList<String> cache = new EvictableList<>(2500);
		cache.setListener(new EvictableList.Listener<String>() {
			@Override
			public void entryEvicted(String value) {
				evicted.set(true);
			}
		});
		cache.add("a1");
		Thread.sleep(1000); Assert.assertEquals(1, cache.size());
		Thread.sleep(1000); Assert.assertEquals(1, cache.size());
		Thread.sleep(2000); Assert.assertEquals(0, cache.size());
		
		Assert.assertEquals(true, evicted.get());
	}
	
	@Test
	public void testMoreEntries() throws Exception {
		EvictableList<String> cache = new EvictableList<>(2500);
		addAndSleep(cache, "a1", 1000);
		addAndSleep(cache, "a2", 1000);
		addAndSleep(cache, "a3", 1000);
		Thread.sleep(200);
		Assert.assertEquals(2, cache.size());
		
		addAndSleep(cache, "a2", 1000);
		Assert.assertEquals(2, cache.size());
		
		cache.add("a2");
		cache.add("a3");
		Thread.sleep(1000);
		Assert.assertEquals(2, cache.size());
		Thread.sleep(3000);
		Assert.assertEquals(0, cache.size());
	}
	
	private void addAndSleep(EvictableList<String> cache, String address, long delay) 
			throws InterruptedException {
		cache.add(address);
		Thread.sleep(delay);
	}
}
