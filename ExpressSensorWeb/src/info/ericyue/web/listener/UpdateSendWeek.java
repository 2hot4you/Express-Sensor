package info.ericyue.web.listener;

import info.ericyue.web.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.TimerTask;

import javax.servlet.ServletContext;

import com.mysql.jdbc.Statement;

public class UpdateSendWeek extends TimerTask {
	private ServletContext context = null;
	private static boolean isRunning=false; 
	public UpdateSendWeek(ServletContext context){
		  this.context=context;
	}
	@Override
	public void run() {
		if(!isRunning)   { 
			context.log( "��ʼ����ÿ��ͳ��"); 
			DBUtil util=new DBUtil();
			String sql="select id from statistics";
			Connection conn=util.openConnection();
			try {
				Statement pstmt = (Statement) conn.createStatement();
				ResultSet rs=pstmt.executeQuery(sql);
				while(rs.next()){
					String id=rs.getString("id");
					String update="UPDATE statistics SET week_total = week_total+today_sent where id="+id;
					Statement total = (Statement) conn.createStatement();
					total.executeUpdate(update);
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			isRunning = false; 
			context.log( "ÿ��ͳ������ִ�н��� "); 
		}
		else{ 
			context.log( "ÿ��ͳ����һ������ִ�л�δ���� "); 
		} 
	}

}
