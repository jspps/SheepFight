package com.sf.entity;

import com.bowlong.tool.SnowflakeIdWorker;

/**
 * 游戏常量
 * @author Canyon
 * @version createtime：2018-11-11上午10:34:34
 */
public class GObjConfig {
	static final public SnowflakeIdWorker SW_SID = new SnowflakeIdWorker(1,0); // session_id
	static final public SnowflakeIdWorker SW_RID = new SnowflakeIdWorker(1,1); // room_id
	static final public SnowflakeIdWorker SW_GID = new SnowflakeIdWorker(1,2); // gobj_id
	
	static final public String Fmt_JsonMsgStr = "{\"code\":\"%s\",\"msg\":\"%s\"}"; // msg 是一个字符串
	static final public String Fmt_JsonMsg = "{\"code\":\"%s\",\"msg\":%s}"; // msg 是一个对象
	static final public String Fmt_JsonState = "{\"code\":\"%s\"}";
	static final public String S_Fails = "fails";
	static final public String S_Success = "success";
	static final public String S_Wait = "wait";
	static final public String K_SesID = "sesid"; // sessionID的key值
	static final public int LMS_Net = 5 * 1000; // 5秒
	static final public int LmtN_Net = 50; // 每个session对象每 LMS_Net 秒限定最多请求数
	static final public int LMS_RoomMatching = 15 * 1000; // 匹配玩家倒计时 - 15秒
	static final public int LMS_RoomUpFirst = 10 * 2; // 房间数据首次更新 - 20 毫秒
	static final public int LMS_RoomUpDelay = 100 * 1; // 房间数据间隔更新 - 100 毫秒
	static final public int LMS_RoomEnd = 3 * 1000; // 房间战斗结束倒计时 - 3秒
	static final public int LMS_NextNewSheep = 5 * 1000; // 下一次随机得羊的时间
	static final public int NMax_OnLine = 100000; // 同时在线人数(超过了就排队)
	static final public int NMax_Room = 2; // 一个房间最大人数
	static final public int NMax_Runway = 5; // 总共有多少跑道(N = Num)
	static final public int LenMax_Runway = 10; // 跑道总长度
	static final public int NI_Forage = 100; // 初始草料值
	static final public int NI_Sheep_Wait = 3; // 等待区 - 初始羊数(NI = NumInit)
	static final public int NMax_Sheep_Wait = 3; // 等待区 - 最大羊数
	static final public double NMin_SpeedWolf = 1.0; // 狼最小随机速度
	static final public double NMax_SpeedWolf = 3.0; // 狼最大随机速度
}
