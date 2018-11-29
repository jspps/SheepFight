package com.sf.entity;

import java.util.Comparator;

/**
 * 排序 - 所在跑道
 * 
 * @author Canyon
 */
public class CompGObjWay implements Comparator<GObject> {
	@Override
	public int compare(GObject o1, GObject o2) {
		if (o1.getRunway() > o2.getRunway())
			return 1;
		if (o1.getRunway() < o2.getRunway())
			return -1;
		if (o1.getId() < o2.getId())
			return -1;
		if (o1.getId() > o2.getId())
			return 1;
		return 0;
	}

}
