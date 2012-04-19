package info.ericyue.web.listener;
import info.ericyue.web.listener.UpdateRank;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ContextListener implements ServletContextListener{  
	private java.util.Timer timerUpdateRank=null;  
	 
	private java.util.Timer timerUpdateSendWeek=null; 
	private java.util.Timer timerUpdateSendMonth=null; 
    public void contextInitialized(ServletContextEvent event){   
    	timerUpdateRank=new java.util.Timer(true);   
    	timerUpdateSendWeek=new java.util.Timer(true);  
    	timerUpdateSendMonth=new java.util.Timer(true);  
        event.getServletContext().log( "��ʱ�������� ");  
        timerUpdateRank.schedule(new UpdateRank(event.getServletContext()),0,30*60*1000);//30��ִ��һ��
        timerUpdateSendWeek.schedule(new UpdateSendWeek(event.getServletContext()),0,24*60*60*1000);//50��ִ��һ��
        timerUpdateSendMonth.schedule(new UpdateSendMonth(event.getServletContext()),0,7*24*60*60*1000);//10��ִ��һ��
        event.getServletContext().log( "�Ѿ����������ȱ� ");    
       }   
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		timerUpdateRank.cancel(); 
		timerUpdateSendWeek.cancel();  
		timerUpdateSendMonth.cancel();  
		arg0.getServletContext().log( "��ʱ������ ");   
	}


} 