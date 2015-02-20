package com.weisong.test.comm;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Assert;

import org.junit.Test;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Snapshot;
import com.weisong.test.comm.message.CResponse;
import com.weisong.test.comm.message.builtin.CCommonMsgs;

abstract public class CBaseDriverPerformanceTest extends CBaseDriverTest {

	final static protected int oneMillion = 1000000; 
	
	final protected int numberPerRound = 100000;
	
	@Test
	public void testManySends() throws Exception {
		doSend(true, 10, 5);
	}
	
	@Test
	public void testManySendsAsync() throws Exception {
		doSend(false, 10, 5);
	}
	
	static private class SendWorker extends Thread {
		
		static private int idSeed = 0;
		
		private boolean shutdown;
		private boolean sync;
		private Histogram histogram;
		private AtomicInteger pending, count;
		private CDriver[] drivers;
		private String[] epAddrs;
		
		public SendWorker(CDriver[] drivers, String[] epAddrs, Histogram histogram, AtomicInteger count, AtomicInteger pending, boolean sync) {
			this.drivers = drivers;
			this.epAddrs = epAddrs;
			this.histogram = histogram;
			this.count = count;
			this.pending = pending;
			this.sync = sync;
			
			setName(String.format("%s-send-worker-%d", sync ? "sync" : "async", ++idSeed));
		}
		
		public void shutdown() {
			shutdown = true;
		}
		
		public void run() {
			while(shutdown == false) {
				for(final CDriver driver : drivers) {
					for(int i = 0; i < epAddrs.length; i++) {
						try {
							final CCommonMsgs.Ping.Request request = new CCommonMsgs.Ping.Request(driver.getAddress(), epAddrs[i]);
							request.timeout = 10000L; // 10 sec
							if(sync) {
								CResponse response = driver.send(request);
								Assert.assertNotNull(response);
								count.incrementAndGet();
								Long time = System.nanoTime() - request.timestamp;
								histogram.update(time);
							}
							else {
								if(pending.intValue() > 10000) {
									Thread.sleep(100);
								}
								pending.incrementAndGet();
								driver.sendAsync(request, new CDriver.Callback<CCommonMsgs.Ping.Response>() {
									@Override
									public void onSuccess(CCommonMsgs.Ping.Response response) {
										pending.decrementAndGet();
										count.incrementAndGet();
										Long time = System.nanoTime() - request.timestamp;
										histogram.update(time);
										Assert.assertNotNull(response);
									}

									@Override
									public void onFailure(Throwable ex) {
										pending.decrementAndGet();
										count.incrementAndGet();
										Long time = System.nanoTime() - request.timestamp;
										histogram.update(time);
										System.err.println("Async invocation failed, " + ex.getMessage());
									}
								});
							}
						}
						catch(Exception ex) {
							ex.printStackTrace();
						}
					}
				}
			}
			System.out.println(getName() + " stopped");
		}
	}

	private void doSend(boolean sync, int numOfWorkers, int numOfRounds) throws Exception {

		AtomicInteger count = new AtomicInteger(1);
		AtomicInteger pending = new AtomicInteger(0);
		
		Histogram histogram = createHistogram();
		
		printStatsHeader();

		SendWorker[] workers = new SendWorker[numOfWorkers];
		for(int i = 0; i < workers.length; i++) {
			workers[i] = new SendWorker(drivers, epAddrs, histogram, count, pending, sync);
			workers[i].start();
		}

		int rounds = 0;
		long st = System.currentTimeMillis();
		while(rounds < numOfRounds) {
			if(count.intValue() % numberPerRound == 0) {
				long t = System.currentTimeMillis();
				long throughput = 1000 * count.intValue() / (t - st);
				printStats(throughput, histogram);
				createHistogram();
				st = t; count.set(1); ++rounds;
			}
		}
		
		for(SendWorker w : workers) {
			w.shutdown();
		}
		for(SendWorker w : workers) {
			w.join();
		}
	}
	
	protected MetricRegistry registry = new MetricRegistry();
	private Histogram createHistogram() {
		return registry.histogram(UUID.randomUUID().toString());
	}
	
	private void printStatsHeader() {
		System.out.println("Throughput   min        mean       75%        95%        max");
		System.out.println("---------------------------------------------------------------");
	}
	
	private void printStats(long throughput, Histogram histogram) {
		Snapshot ss = histogram.getSnapshot();
		System.out.println(String.format("%5d/s %8.2fms %8.2fms %8.2fms %8.2fms %8.2fms",
				throughput,
				1.0f * ss.getMin() / oneMillion, 
				ss.getMean() / oneMillion, 
				ss.get75thPercentile() / oneMillion, 
				ss.get95thPercentile() / oneMillion, 
				1.0f * ss.getMax() / oneMillion
		));
	}
	
}
