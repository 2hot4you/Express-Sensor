/**
 * Copyright:   Moonlight(Eric yue)
 * Email:       hi.moonlight@gmail.com
 * Website:     www.ericyue.info
 * File name:   SystemInfoImpl.java 
 * Project Name:ExpressSensorWeb
 * Create Date: 2011-7-22
 */
package info.ericyue.web.dao.impl;
import java.sql.*;
import info.ericyue.web.dao.SystemInfoDao;
import info.ericyue.web.util.DBUtil;

public class SystemInfoDaoImpl implements SystemInfoDao{
	public String getInfo(int type,int query){
		String sql="select * from system where type=?";
		//type=0 ��ʾ�ͻ�Ա�Ŀͻ���
		//type=1 ��ʾ��ͨ�û��Ŀͻ���
		
		//query=0��ʾversion
		//query=1��ʾuser_amount
		//query=2��ʾupdate_date
		DBUtil util=new DBUtil();
		String tmp="";
		Connection conn=util.openConnection();
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);			
			switch(query){
			case 0:
				tmp="version";
				break;
			case 1:
				tmp="user_amount";
				break;
			case 2:
				tmp="update_date";
				break;
			}
			pstmt.setInt(1, type);
			ResultSet rs=pstmt.executeQuery();			
		if(rs.next()){
			return rs.getString(tmp);
		}	
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
