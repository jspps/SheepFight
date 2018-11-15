package com.sf.entity;

import java.util.Comparator;

public class CompGObjEnd implements Comparator<GObject> {
	@Override
	public int compare(GObject o1, GObject o2) {
		if (o1.getStartRunTime() > o2.getStartRunTime())
			return -1;
		if (o1.getStartRunTime() < o2.getStartRunTime())
			return 1;
		if (o1.getId() < o2.getId())
			return -1;
		if (o1.getId() > o2.getId())
			return 1;
		return 0;
	}

}
