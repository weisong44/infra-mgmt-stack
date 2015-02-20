package com.weisong.test.comm.impl;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import com.weisong.test.comm.CHub;
import com.weisong.test.comm.exception.CException;
import com.weisong.test.comm.transport.codec.CCodec;
import com.weisong.test.comm.transport.codec.CCodecFactory;
import com.weisong.test.comm.transport.pdu.CPdu;
import com.weisong.test.comm.util.HostAndPortResolver;
import com.weisong.test.comm.util.HostAndPortResolver.HostAndPort;

public class CRedisHub implements CHub {

	private class RedisPubSubAdaptor {
		
		private Logger logger = LoggerFactory.getLogger(getClass());		
		
		private RedisPubSubAdaptor() {
			scanner = new SubscriptionScanner();
			scanner.start();
		}
		
		private JedisPubSub pubsub = new JedisPubSub() {
			@Override
			public void onMessage(String topic, String msg) {
				CHub.Listener listener = listenerMap.get(topic);
				if(listener != null) {
					CPdu pdu = codec.decodePdu(msg.getBytes());
					listener.onHubPdu(pdu);
				}
			}
		};
		
		private class SubscriberThread extends Thread {
			public void run() {
				try (Jedis jedis = pool.getResource()) {
					logger.info("Starting subscriber thread ...");
					String topics[] = listenerMap.keySet().toArray(new String[listenerMap.size()]);
					jedis.subscribe(pubsub, topics);
					logger.info("Subscriber ended");
				};
			}
		}
		
		private class SubscriptionScanner extends Thread {
			public void run() {
				while(true) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					if(newSubscriptionAdded) {
						logger.info("Found new subscriptions, resubscribe ...");
						newSubscriptionAdded = false;
						try {
							pubsub.unsubscribe();
							subscriberThread.join();
						} catch (Exception e) {
							// Ignore
						}
						subscriberThread = new SubscriberThread();
						subscriberThread.start();
					}
				}
			}
		}

		private boolean newSubscriptionAdded;
		private SubscriberThread subscriberThread;
		private SubscriptionScanner scanner;
		private Map<String, CHub.Listener> listenerMap = new HashMap<>();
		
		public void subscribe(String topic, CHub.Listener listener) {
			listenerMap.put(topic, listener);
			newSubscriptionAdded = true;
		}
	}
	
	static private CCodec codec = CCodecFactory.getOrCreate();
	
	private JedisPool pool;
	
	public CRedisHub(String hostAndPort) {
		HostAndPort hap = HostAndPortResolver.resolve(hostAndPort, "localhost", 6379);
		while(true) {
			pool = new JedisPool(new JedisPoolConfig(), hap.host, hap.port);
			try (Jedis jedis = pool.getResource()) {
				jedis.keys("*");
				break;
			} catch (Exception e) {
				System.out.println("Failed to connect to Redis, retry in a while");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					// Ignore
				}
			}
		}
	}	
	
	private RedisPubSubAdaptor adaptor = new RedisPubSubAdaptor();
	
	@Override
	public void publish(String topicName, CPdu pdu) throws CException {
		try (Jedis jedis = pool.getResource()) {
			byte[] bytes = codec.encode(pdu);
			jedis.publish(topicName, new String(bytes));
		}
	}

	@Override
	public void publish(CPdu pdu) throws CException {
		publish(pdu.destAddr, pdu);
	}

	@Override
	public void register(String topic, CHub.Listener listener) {
		adaptor.subscribe(topic, listener);
	}
}
