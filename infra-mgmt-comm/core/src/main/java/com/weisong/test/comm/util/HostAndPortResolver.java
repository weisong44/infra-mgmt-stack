package com.weisong.test.comm.util;

public class HostAndPortResolver {
	
	static public class HostAndPort {
		public String host;
		public int port;
		public HostAndPort(String host, int port) {
			this.host = host;
			this.port = port;
		}
		public String toString() {
			return String.format("%s:%d", host, port);
		}
	}
	
	/**
	 * @param s the input string, e.g. "1.1.1.1:2222", "2.2.2.2"
	 */
	static public HostAndPort resolve(String s, String defaultHost, int defaultPort) {
		HostAndPort hap = new HostAndPort(defaultHost, defaultPort);
		if(s == null || s.length() <= 0) {
			return hap;
		}
		String[] tokens = s.split(":");
		hap.host = tokens[0];
		if(tokens.length == 2) {
			hap.port = Integer.valueOf(tokens[1]);
		}
		return hap;
	}

	/**
	 * @param s input string, e.g. "1.1.1.1:2222,2.2.2.2"
	 */
	static public HostAndPort[] resolveMultiple(String s, String defaultHost, int defaultPort) {
		String[] tokens = s.split(",");
		if(s == null || s.length() <= 0) {
			return new HostAndPort[] {
				new HostAndPort(defaultHost, defaultPort)
			};
		}
		HostAndPort[] hapArray = new HostAndPort[tokens.length];
		for(int i = 0; i < tokens.length; i++) {
			hapArray[i] = resolve(tokens[i], defaultHost, defaultPort);
		}
		return hapArray;
	}
}
