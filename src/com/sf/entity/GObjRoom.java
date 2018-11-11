package com.sf.entity;


/**
 * 房间数据
 * 
 * @author Canyon
 * @version createtime：2018-11-11下午2:48:18
 */
public class GObjRoom extends BeanOrigin {
	private static final long serialVersionUID = 1L;
	long roomid = 0;
	long psid1 = 0;
	long psid2 = 0;

	public long getRoomid() {
		return roomid;
	}

	public void setRoomid(long roomid) {
		this.roomid = roomid;
	}

	public long getPsid1() {
		return psid1;
	}

	public void setPsid1(long psid1) {
		this.psid1 = psid1;
	}

	public long getPsid2() {
		return psid2;
	}

	public void setPsid2(long psid2) {
		this.psid2 = psid2;
	}

	public GObjRoom() {
		super();
	}

	public GObjRoom(long roomid) {
		super();
		this.roomid = roomid;
	}
	
	public long getOther(long psid){
		return psid == psid1 ? psid2 : psid1;
	}
	
	public boolean setOne(long psid){
		if(psid1 == 0){
			psid1 = psid;
			return true;
		}else if(psid2 == 0){
			psid2 = psid;
			return true;
		}
		return false;
	}
	
	public boolean clearOne(long psid){
		if(psid1 == psid){
			psid1 = 0;
			return true;
		}else if(psid2 == psid){
			psid2 = 0;
			return true;
		}
		return false;
	}
	
	public boolean changeOne(long psid,boolean isAdd){
		if(isAdd){
			return setOne(psid);
		}else{
			return clearOne(psid);
		}
	}
	
	public boolean isFull(){
		return psid1 > 0 && psid2 > 0;
	}
}
