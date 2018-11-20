package com.sf.test;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledFuture;

import org.json.JSONObject;

import com.bowlong.lang.ByteEx;
import com.bowlong.lang.NumEx;
import com.bowlong.lang.RndEx;
import com.bowlong.net.http.uri.HttpUriPostEx;
import com.bowlong.net.http.urlcon.HttpUrlConEx;
import com.bowlong.reflect.JsonHelper;
import com.sf.entity.BeanOrigin;
import com.sf.entity.ETGObj;
import com.sf.entity.GObjConfig;
import com.sf.entity.GObject;

public class SFTest extends BeanOrigin implements Runnable {
	private static final long serialVersionUID = 1L;
	static String host = "http://%s:8080/SheepFight/Svlet/Game";
	static Map<String, Object> params = new HashMap<String, Object>();
	static long sesid = 0;
	static ScheduledFuture<?> objSF = null;
	static long end = 0;
	static boolean isEnd = false;
	static int nEnd = 0;

	public static void main(String[] args) throws Exception {
		// test_swids();
		// test_enum();
		// test_net();
		// test_queue();
		// test_rnd();
		// test_game();
		// test_rnd_number();
		test_get_time();
	}

	static void test_get_time() throws Exception {
		String ip = "127.0.0.1";
		ip = "60.205.217.89";
		params.put("cmd", 100);
		byte[] buffer = null;
		String str = "";
		buffer = HttpUrlConEx.postParams(String.format(host, ip), params, "utf-8");
		str = ByteEx.toStr(buffer, "utf-8");
		System.out.println(str);
		buffer = HttpUriPostEx.postMap(String.format(host, ip), params, "utf-8");
		System.out.println(String.format("test_get_time =[%s]", ByteEx.toStr(buffer, "utf-8")));
	}

	static void test_rnd_number() {
		String val = "";
		String fmt = "i = %s,j = %s,rndInt = [%s],rnd = [%s]";
		int k, v;
		double dv = 0;
		for (int i = 1; i <= 5; i++) {
			for (int j = 1; j <= 5; j++) {
				k = 1;
				v = 5;
				dv = RndEx.nextDouble(k, v, 3);
				val = String.format(fmt, k, v, RndEx.nextInt(k, v), dv);
				System.out.println(val);
				System.out.println(NumEx.roundDecimal(dv, 3));
			}
		}
	}

	static void test_game() {
		gameLogin();
		if (sesid > 0) {
			end = GObjConfig.NMax_RoomTime + now();
			objSF = com.bowlong.lang.task.SchedulerEx.fixedRateMS(new SFTest(), 200, 500);
		}
	}

	static void test_rnd() {
		for (int i = 0; i < 100; i++) {
			System.out.println(RndEx.nextInt(2));
		}
	}

	static void test_queue() {
		Queue<GObject> queEnd = new ConcurrentLinkedQueue<GObject>();
		GObject poll = queEnd.poll();
		System.out.println(poll);
	}

	static private void gameLogin() {
		String ip = "127.0.0.1";
		ip = "60.205.217.89";
		params.put("cmd", 1000);
		params.put("lgid", "11111");
		params.put("lgpwd", "222222");
		byte[] ins = null;
		ins = HttpUrlConEx.postParams(String.format(host, ip), params, "utf-8");
		String strJson = ByteEx.toStr(ins, "utf-8");
		System.out.println(String.format("lg =[%s]", strJson));
		try {
			JSONObject json = JsonHelper.toJSON(strJson);
			JSONObject jsonMsg = json.getJSONObject("msg");
			sesid = jsonMsg.getLong("sesid");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static private void gameHeart() {
		try {
			String ip = "127.0.0.1";
			ip = "60.205.217.89";
			byte[] ins = null;
			String strJson = null;
			params.clear();
			params.put("cmd", 1001);
			params.put("sesid", sesid);
			ins = HttpUrlConEx.postParams(String.format(host, ip), params, "utf-8");
			strJson = ByteEx.toStr(ins, "utf-8");
			System.out.println(String.format("heart =[%s]", strJson));
			JSONObject json = JsonHelper.toJSON(strJson);
			JSONObject jsonMsg = json.getJSONObject("msg");
			if (jsonMsg.has("isWin")) {
				isEnd = true;
				nEnd = 3;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	static void test_net() {
		gameLogin();
		try {
			for (int i = 0; i < 30; i++) {
				gameHeart();
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
			str = String.format("%s_%s_%s", GObjConfig.SW_SID.nextId(), GObjConfig.SW_RID.nextId(),
					GObjConfig.SW_GID.nextId());
			System.out.println(str);
		}
	}

	@Override
	public void run() {
		if ((isEnd && nEnd <= 0) || (end < now())) {
			if (objSF != null) {
				objSF.cancel(true);
				objSF = null;
			}
			isEnd = false;
		}
		if (sesid > 0) {
			nEnd--;
			gameHeart();
		}
	}
}
