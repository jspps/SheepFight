package com.sf.entity;

import com.bowlong.tool.SnowflakeIdWorker;

/**
 * 游戏常量
 * @author Canyon
 * @version createtime：2018-11-11上午10:34:34
 */
public class GObjConfig {
	static final public SnowflakeIdWorker SW_ID = new SnowflakeIdWorker(1,0);  
	static final public SnowflakeIdWorker SW_RoomID = new SnowflakeIdWorker(1,1);
	
	static final public String S_Fails = "fails";
	static final public String S_Success = "success";
	static final public String S_Wait = "wait";
	static final public String K_SesID = "sesid"; // sessionID的key值
	static final public int LMS_Net = 5 * 1000; // 5秒
	static final public int LMS_Matching = 15 * 1000; // 15秒
	static final public int LN_Net = 50; // 每个session对象每5秒限定最多请求数(LN = LimitNum)
	static final public String Fmt_JsonMsgStr = "{\"code\":\"%s\",\"msg\":\"%s\"}"; // msg 是一个字符串
	static final public String Fmt_JsonMsg = "{\"code\":\"%s\",\"msg\":%s}"; // msg 是一个对象
	static final public String Fmt_JsonState = "{\"code\":\"%s\"}";
	static public int NM_Runway = 5; // 总共有多少跑道(NM = NumMax)
	static public int NM_Room = 2; // 一个房间最大人数
	static public int LM_Runway = 10; // 跑道总长度(LM = LengthMax)
	static public int NM_OnLine = 100000; // 同时在线人数(超过了就排队)
	static public int N_Init_Forage = 100; // 初始草料值
}
