package com.weisong.test.comm.util;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import lombok.Getter;
import lombok.Setter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author weisong
 */
public class EvictableMap<K, V> {
	
	static private class Entry<V> implements Comparable<Entry<V>>{
		private V value;
		private Long lastModified;
		private Entry(V value) {
			this.value = value;
			lastModified = System.currentTimeMillis();
		}
		@Override
		public int compareTo(Entry<V> e) {
			return lastModified.compareTo(e.lastModified);
		}
	}
	
	@SuppressWarnings("rawtypes")
	static private class Evictor extends Thread {
		
		private Logger logger = LoggerFactory.getLogger(getClass());
		
		private List<EvictableMap> caches = new LinkedList<>();

		private void add(EvictableMap cache) {
			caches.add(cache);
		}
		
		public void run() {
			setName("CEvictableMap.Evictor");
			while(true) {
				try {
					for(EvictableMap c : caches) {
						process(c);
					}
					Thread.sleep(1000);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		@SuppressWarnings("unchecked")
		private void process(EvictableMap map) {
			synchronized (map.map) {
				long now = System.currentTimeMillis();
				while(map.map.isEmpty() == false) {
					Object key = map.map.keySet().iterator().next();
					Entry<?> e = (Entry<?>) map.map.get(key);
					if(e.lastModified + map.entryTimeToLive < now) {
						map.map.remove(key);
						if(logger.isDebugEnabled()) {
							logger.debug(String.format("Evicting %s [%d]", key, map.size()));
						}
						if(map.listener != null) {
							map.listener.entryEvicted(key, e.value);
						}
					}
					else {
						break;
					}
				}
			}
		}
	}
	
	static public class Listener<K, V> {
		public void entryAdded(K key, V value) {}
		public void entryRefreshed(K key, V value) {}
		public void entryEvicted(K key, V value) {}
		public void cleared() {}
	}
	
	static private Evictor evictor;
	
	static {
		evictor = new Evictor();
		evictor.start();
	}
	
	@Getter @Setter 
	private long entryTimeToLive = 5000L;
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Setter
	private Listener<K, V> listener;
	
	private Map<K, Entry<V>> map = new TreeMap<>();
	
	public EvictableMap() {
		evictor.add(this);
	}
	
	public EvictableMap(int entryTimeToLive) {
		this.entryTimeToLive = entryTimeToLive;
		evictor.add(this);
	}
	
	public V remove(String key) {
		synchronized (map) {
			Entry<V> e = map.remove(key);
			return e == null ? null : e.value;
		}
	}

	public void refresh(K key) {
		synchronized (map) {
			Entry<V> e = map.remove(key);
			if(e != null) {
				e.lastModified = System.currentTimeMillis();
				map.put(key, e);
			}
		}
	}
	
	public void put(K key, V value) {
		synchronized (map) {
			Entry<V> e = map.remove(key);
			if(e != null) {
				e.value = value;
				e.lastModified = System.currentTimeMillis();
				if(listener != null) {
					listener.entryRefreshed(key, e.value);
				}
			}
			else {
				e = new Entry<V>(value);
				if(listener != null) {
					listener.entryAdded(key, e.value);
				}
			}
			map.put(key, e);
		}
	}

	public int size() {
		synchronized (map) {
			return map.size();
		}
	}

	public void clear() {
		synchronized (map) {
			map.clear();
		}
		if(logger.isDebugEnabled()) {
			logger.debug("clearing");
		}
		if(listener != null) {
			listener.cleared();
		}
	}

	public boolean isEmpty() {
		synchronized (map) {
			return size() == 0;
		}
	}
	
	public V get(K key) {
		synchronized (map) {
			Entry<V> e = map.get(key);
			return e == null ? null : e.value;
		}
	}
	
	public boolean containsKey(K key) {
		synchronized (map) {
			return map.containsKey(key);
		}
	}
	
	public Collection<V> values() {
		List<V> list = new LinkedList<>();
		synchronized (map) {
			for(Entry<V> e : map.values()) {
				list.add(e.value);
			}
		}
		return list;
	}
}
