package com.sf.entity;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import com.bowlong.util.CalendarEx;

import java.sql.Connection;

/**
 * 实体 根对象
 * 
 * @author Canyon
 * @version createtime：2018-7-19下午3:25:56
 */
public class BeanOrigin implements Serializable {
	private static final long serialVersionUID = 1L;
	public static String insVal = "INSERT INTO `%s` (%s) VALUES (%s)";
	public static String selectHead = "SELECT * FROM `%s` WHERE 1 = 1 ";

	protected int Insert(String tbName, String column, String vals, Object[] objs, Connection connection) {
		PreparedStatement pstmt = null;
		try {
			String sql = String.format(insVal, tbName, column, vals);

			pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

			for (int i = 0; i < objs.length; i++) {
				pstmt.setObject(i + 1, objs[i]);
			}

			pstmt.executeUpdate();
			ResultSet rs = pstmt.getGeneratedKeys();

			if (rs.next()) {
				return rs.getInt(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (pstmt != null) {
					pstmt.close();
				}
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return -1;
	}

	protected Map<String, Object> map = new HashMap<String, Object>();

	public Map<String, Object> toMap(Map<String, Object> map) {
		return map;
	}

	public Map<String, Object> toMap() {
		map.clear();
		return toMap(map);
	}

	static final public long now() {
		return CalendarEx.now();
	}

	static final public double round(double org, int acc) {
		double pow = 1;
		for (int i = 0; i < acc; i++) {
			pow *= 10;
		}

		double temp = (int) (org * pow + 0.5);
		return temp / pow;
	}
}
