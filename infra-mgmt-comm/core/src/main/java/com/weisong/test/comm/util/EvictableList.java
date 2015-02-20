package com.weisong.test.comm.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import lombok.Getter;
import lombok.Setter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Be mindful that this cache is only suitable for storing a small 
 * number of objects, e.g. 5
 * 
 * @author weisong
 */
public class EvictableList<V> {
	
	static private Random random = new Random();
	
	static private class Entry<V> {
		private V value;
		private Long lastModified;
		private Entry(V value) {
			this.value = value;
			lastModified = System.currentTimeMillis();
		}
	}
	
	@SuppressWarnings("rawtypes")
	static private class Evictor extends Thread {
		
		private Logger logger = LoggerFactory.getLogger(getClass());
		
		private List<EvictableList> caches = new LinkedList<>();
		
		private void add(EvictableList cache) {
			synchronized (caches) {
				caches.add(cache);
			}
		}
		
		public void run() {
			setName("CEvictableList.Evictor");
			while(true) {
				try {
					synchronized (caches) {
						for(EvictableList c : caches) {
							process(c);
						}
					}
					Thread.sleep(1000);
				}
				catch (Throwable t) {
					t.printStackTrace();
				}
			}
		}
		
		@SuppressWarnings("unchecked")
		private void process(EvictableList list) {
			synchronized (list.entries) {
				long now = System.currentTimeMillis();
				for(Iterator<Entry> i = list.entries.iterator(); i.hasNext();) {
					Entry e = i.next();
					if(e.lastModified + list.entryTimeToLive < now) {
						i.remove();
						if(logger.isDebugEnabled()) {
							logger.debug(String.format("Evicting %s [%d]", e.value, list.size()));
						}
						if(list.listener != null) {
							list.listener.entryEvicted(e.value);
						}
					}
				}
			}
		}
	}

	static public class Listener<V> {
		public void entryAdded(V value) {}
		public void entryRefreshed(V value) {}
		public void entryEvicted(V value) {}
		public void cleared() {}
	}

	static private Evictor evictor;
	
	static {
		evictor = new Evictor();
		evictor.start();
	}
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Getter @Setter 
	private long entryTimeToLive = 5000L;
	
	private LinkedList<Entry<V>> entries = new LinkedList<>();
	
	@Setter 
	private Listener<V> listener;
	
	public EvictableList() {
		evictor.add(this);
	}
	
	public EvictableList(int entryTimeToLive) {
		this.entryTimeToLive = entryTimeToLive;
		evictor.add(this);
	}
	
	public boolean add(V value) {
		synchronized (entries) {
			for(Iterator<Entry<V>> i = entries.iterator(); i.hasNext();) {
				Entry<V> e = i.next();
				if(e.value.equals(value)) {
					e.lastModified = System.currentTimeMillis();
					if(listener != null) {
						listener.entryRefreshed(e.value);
					}
					return false;
				}
			}
			entries.add(new Entry<V>(value));
			if(listener != null) {
				listener.entryAdded(value);
			}
			return true;
		}
	}

	public void clear() {
		synchronized (entries) {
			entries.clear();
			if(logger.isDebugEnabled()) {
				logger.debug("clearing");
			}
			if(listener != null) {
				listener.cleared();
			}
		}
	}
	
	public int size() {
		synchronized (entries) {
			return entries.size();
		}
	}

	public boolean isEmpty() {
		return size() == 0;
	}
	
	public V getOne() {
		synchronized (entries) {
			if(isEmpty()) {
				return null;
			}
			int index = random.nextInt(entries.size());
			return entries.get(index).value;
		}
	}

	public Collection<V> getAll() {
		synchronized (entries) {
			List<V> list = new LinkedList<>();
			for(int i = 0; i < size(); i++) {
				list.add(entries.get(i).value);
			}
			return list;
		}
	}
}
