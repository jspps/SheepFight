package com.sf.entity;

import java.util.Comparator;

/**
 * 排序 - 开始时间
 * 
 * @author Canyon
 */
public class CompGObjEnd implements Comparator<GObject> {
	@Override
	public int compare(GObject o1, GObject o2) {
		if (o1.getStartMs() > o2.getStartMs())
			return -1;
		if (o1.getStartMs() < o2.getStartMs())
			return 1;
		if (o1.getId() < o2.getId())
			return -1;
		if (o1.getId() > o2.getId())
			return 1;
		return 0;
	}

}
