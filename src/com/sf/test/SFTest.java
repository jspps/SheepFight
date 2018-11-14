package com.sf.test;

import com.sf.entity.GObjConfig;

public class SFTest {
	public static void main(String[] args) {
//		test_swids();
	}
	
	static void test_swids(){
		String str = "";
		for (int i = 0; i < 50; i++) {
			str = String.format("%s_%s_%s",
					GObjConfig.SW_SID.nextId(),
					GObjConfig.SW_RID.nextId(),
					GObjConfig.SW_GID.nextId()
					);
			System.out.println(str);
		}
	}
}
