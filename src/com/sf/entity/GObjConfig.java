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
	static final public int NMax_OnLine = 100000; // 同时在线人数(超过了就排队)
	static final public int LMS_RoomMatching = 10 * 1000; // 匹配玩家倒计时
	static final public int LMS_RoomUpFirst = 10 * 2; // 房间 首次更新 - 毫秒
	static final public int LMS_RoomUpDelay = 10 * 2; // 房间 间隔更新 - 毫秒
	static final public int LmtN_Net = LMS_Net / LMS_RoomUpDelay + 10; // 每个session对象每 LMS_Net 秒限定最多请求数
	static final public int NMax_Runway = 5; // 总共有多少跑道(N = Num)
	static final public int NI_Forage = 100; // 初始草料值
	static final public int NI_Sheep_Wait = 3; // 等待区 - 初始羊数(NI = NumInit)
	static final public int NMax_Sheep_Wait = 3; // 等待区 - 最大羊数
	static final public int LMS_RoomWaitEnd = 2 * 1000; // 房间战斗结束倒计时 - 2秒
	static final public int NMax_RoomTime = 20 * 60 * 1000; // 房间战斗最长时间  (数字1) - 分钟
	static final public int NMin_RoomTime = 30 * 1000; // 判断胜负的最小时间
	static final public int LMS_NextNewSheep = 5000; // 下一次随机得羊的时间
	static final public int NMax_RobotAI_Basic = 100; // 机器人AI的随机底数
	static final public int NL_RobotAI_Middle = 30; // 机器人AI - 中级AI所占有值
	static final public int LMS_FirstRobotDownSheep = 5000; // 首次 机器人 放羊時間
	static final public int LMS_NextRobotDownSheep = 5000; // 机器人 放羊時間 间隔
	static final public int LenMax_Runway = 10; // 跑道总长度
	static final public double NMax_CollidDistance = 0.08; // 判断相碰的最小距离
	static final public double NL_BackSpeedMultiples = 2.5; // 羊碰撞后返回自己的速度倍率
	static final public double NI_PosWolf = 5; // 狼的初始位置
	static final public double NMin_SpeedWolf = 2; // 狼最小随机速度
	static final public double NMax_SpeedWolf = 2; // 狼最大随机速度
	static final public int LMS_NextLive_Wolf = 3500; // 狼消失后，下次刷新间隔
	static final public int LMS_NextRndWay_Wolf = 4000; // 狼所在跑道，没羊来，随机下一个跑道的时间
	static final public double NI_PosNeutral = 5; // 叛变羊的初始位置
	static final public double NMin_SpeedNeutral = 1.0; // 叛变羊最小随机速度
	static final public double NMax_SpeedNeutral = 1.0; // 叛变羊最大随机速度
	static final public int NMax_NeutralMutiny = 3; // 叛变羊最大叛变次数
	static final public int LMS_NextLive_Neutral = 3000; // 叛变羊消失后，下次刷新间隔
	static final public double NMin_PosSpinach = 5; // 菠菜罐头位置
	static final public double NMax_PosSpinach = 5; // 菠菜罐头位置
	static final public int LMS_NextLive_Spinach = 5000; // 菠菜罐头消失后，下次刷新间隔
}
