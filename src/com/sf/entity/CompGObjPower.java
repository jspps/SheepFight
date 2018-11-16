package com.sf.entity;

import java.util.Comparator;

/**
 * 排序 - 体力，力量
 * 
 * @author Canyon
 */
public class CompGObjPower implements Comparator<GObject> {
	@Override
	public int compare(GObject o1, GObject o2) {
		int power1 = o1.getGobjType().getPower();
		int power2 = o2.getGobjType().getPower();
		if (power1 > power2)
			return -1;
		if (power1 < power2)
			return 1;
		if (o1.getId() < o2.getId())
			return -1;
		if (o1.getId() > o2.getId())
			return 1;
		return 0;
	}
}
