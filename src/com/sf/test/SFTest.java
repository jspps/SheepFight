package com.sf.test;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.json.JSONObject;

import com.bowlong.net.http.uri.HttpUriPostEx;
import com.bowlong.net.http.urlcon.HttpUrlConEx;
import com.bowlong.reflect.JsonHelper;
import com.sf.entity.ETGObj;
import com.sf.entity.GObjConfig;
import com.sf.entity.GObject;

public class SFTest {
	public static void main(String[] args) {
		// test_swids();
		// test_enum();
		// test_net(true);
		test_queue();
	}
	
	static void test_queue(){
		Queue<GObject> queEnd = new ConcurrentLinkedQueue<GObject>();
		GObject poll = queEnd.poll();
		System.out.println(poll);
	}

	static void test_net(boolean isUrl) {
		String host = "http://127.0.0.1:8080/SheepFight/Svlet/Game";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("cmd", 1000);
		params.put("lgid", "abcdabcd");
		params.put("lgpwd", "222222");
		InputStream ins = null;
		if (isUrl) {
			ins = HttpUrlConEx.postParams(host, params, "utf-8");
		} else {
			ins = HttpUriPostEx.postMap(host, params, "utf-8");
		}
		String strJson = HttpUriPostEx.inps2Str(ins, "utf-8");
		System.out.println("====1====");
		System.out.println(strJson);
		System.out.println("====2====");
		try {
			ins.close();
			JSONObject json = JsonHelper.toJSON(strJson);
			JSONObject jsonMsg = json.getJSONObject("msg");

			params.clear();
			params.put("cmd", 1001);
			params.put("sesid", jsonMsg.get("sesid"));
			for (int i = 0; i < 30; i++) {
				if (isUrl) {
					ins = HttpUrlConEx.postParams(host, params, "utf-8");
				} else {
					ins = HttpUriPostEx.postMap(host, params, "utf-8");
				}
				strJson = HttpUriPostEx.inps2Str(ins, "utf-8");
				System.out.println(strJson);
				ins.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	static void test_enum() {
		ETGObj gt1 = ETGObj.SheepBig;
		System.out.println(gt1);
		System.out.println(gt1.getName());
		System.out.println(gt1.getIndex());
		System.out.println(gt1.toStr());
		gt1.setIndex(9);
		System.out.println(gt1.getIndex());
		System.out.println(gt1.toStr());
	}

	static void test_swids() {
		String str = "";
		for (int i = 0; i < 50; i++) {
			str = String.format("%s_%s_%s", GObjConfig.SW_SID.nextId(),
					GObjConfig.SW_RID.nextId(), GObjConfig.SW_GID.nextId());
			System.out.println(str);
		}
	}
}
