package com.ujigu.secure.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.ujigu.secure.common.utils.LogUtils;

public class SqlTest {

	public static void main(String[] args) {
		String nick = "sdfs2018";
//		String nick = "'; delete from test_user where sid = 3 and '1' = '1";
//		String nick = "' or '1' = '1";
		Connection conn = null;
		Statement stmt = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			
			conn = DriverManager.getConnection("jdbc:mysql://210.73.209.76:3306/csaimall_insure_db?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&allowMultiQueries=true", 
					"dbuser_insure", "B57e94oD02z3");
			stmt = conn.createStatement();
			rs = stmt.executeQuery("select * from test_user where nick = '" + nick + "'");
			
			/*pstmt = conn.prepareStatement("select * from test_user where nick = ?");
			pstmt.setString(1, nick);
			rs = pstmt.executeQuery();*/
			while(rs.next()){
				System.out.println(rs.getInt("sid") + "===" + rs.getString("status"));
			}
			
			/*pstmt = conn.prepareStatement("update test_user set company = ? where nick = ?");
			pstmt.setString(1, nick);
			pstmt.setString(2, "sdfs2018");
			pstmt.executeUpdate();*/
			
			
		} catch (ClassNotFoundException e) {
			LogUtils.error(e.getMessage(), e);
		} catch (SQLException e) {
			LogUtils.error(e.getMessage(), e);
		} finally {
			if(rs != null){
				try {
					rs.close();
				} catch (SQLException e) {
					LogUtils.error(e.getMessage(), e);
				}
			}
			if(stmt != null){
				try {
					stmt.close();
				} catch (SQLException e) {
					LogUtils.error(e.getMessage(), e);
				}
			}
			if(conn != null){
				try {
					conn.close();
				} catch (SQLException e) {
					LogUtils.error(e.getMessage(), e);
				}
			}
		}

	}

}
