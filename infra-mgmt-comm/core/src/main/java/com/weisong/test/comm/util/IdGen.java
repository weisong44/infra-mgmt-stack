package com.weisong.test.comm.util;

import java.util.Random;

public class IdGen {
	
	static private Random random = new Random();
	
	static public String next(String baseId) {
		return baseId + "-" + String.format("%05d", random.nextInt(100000)); 
	}
}
