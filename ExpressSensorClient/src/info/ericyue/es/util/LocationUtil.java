/**
 * Copyright:   Moonlight(Eric yue)
 * Email:       hi.moonlight@gmail.com
 * Website:     www.ericyue.info
 * File name:   LocationUtil.java 
 * Project Name:ExpressSensor
 * Create Date: 2011-8-18
 */
package info.ericyue.es.util;


import info.ericyue.es.activity.SettingsActivity;
import info.ericyue.es.activity.TradeStatus;
import info.ericyue.es.activity.WiFi;

import com.google.android.maps.GeoPoint;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;

public class LocationUtil{
	private SensorManager sm ;
	protected long curTime;
	protected long lastTime;
	protected long duration;
	protected float last_x;
	protected float last_y;
	protected float last_z;
	protected long initTime;
	protected float shake;
	protected float totalShake;
	private int sensorType;
	
	private String TYPE="GSM";
	private LocationManager locationManager;
	private String user_id,user_pwd,username;
	private Context context;
	private WiFi wifi;
	private String SERVERIP="192.168.43.55";
	private int SERVERPORT=8192;
	private String verifyMessage;
	private long[] pattern = {1800,300}; 
	private NotificationManager notificationManager; 

	public LocationUtil(String id,String username,String userpwd,LocationManager locationManager,Context context){
		this.locationManager=locationManager;
		this.context=context;
		this.user_id=id;
		this.username=username;
		this.wifi=new WiFi(context);
		this.user_pwd=userpwd;
		notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		////////////////////////
		sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensorType = android.hardware.Sensor.TYPE_ACCELEROMETER;
        
	}
	public String GetWorkerID(){
		Log.i("id", user_id+"");
		return HttpUtil.queryTradeInfo("receiver_id", user_id, "deliverer_id");
	}
	public String generateVerifyMessage(){
		/**
		 * �����û���֤��Ϣ��Ȼ��MD5���ܣ����������Ա��������ȶԡ�
		 * id/username/password/truename
		 */
		String worker_id=GetWorkerID();
		String user_truename=HttpUtil.queryTradeInfo("receiver_id",user_id,"receiver_name");
		String str=worker_id+user_id+username+user_pwd+user_truename;
		try {
			str=EncryptString.encryptString(str, "MD5");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return str;
	}
	 public void updateWithNewLocation(Location location) {   
         String latLongString;   
         if (location != null) {   
        	 double lat = location.getLatitude();   
        	 double lng = location.getLongitude();   
        	 latLongString = "γ��:" + lat + "   ����:" + lng;   
         } else {   
        	 latLongString = "�޷���ȡ������Ϣ";   
         }   
         Log.i("�ͻ��˼���", "����ǰ��λ����:" + latLongString);   
	 }   
	public final LocationListener locationListener = new LocationListener() {   
        public void onLocationChanged(Location location) {   
        updateWithNewLocation(location);   
        }   
        public void onProviderDisabled(String provider){   
        updateWithNewLocation(null);   
        }   
        public void onProviderEnabled(String provider){ }   
        public void onStatusChanged(String provider, int status,   
        Bundle extras){ }   
	}; 
	public GeoPoint GetUserLocation(){	
		 	Criteria criteria = new Criteria();   
	        criteria.setAccuracy(Criteria.ACCURACY_FINE);   
	        criteria.setAltitudeRequired(false);   
	        criteria.setBearingRequired(false);   
	        criteria.setCostAllowed(true);   
	        criteria.setPowerRequirement(Criteria.POWER_LOW);   
	        String provider = locationManager.getBestProvider(criteria, true);   
	        Location location = locationManager.getLastKnownLocation(provider);   
	        updateWithNewLocation(location);   
	        locationManager.requestLocationUpdates(provider, 2000, 10,   
	                        locationListener);  
	        if(location!=null){
	        	Log.i("Update", "location����");
	        	HttpUtil.UpdateUserCurrentLocation(user_id,""+location.getLatitude(),""+location.getLongitude());
				double geoLatitude = location.getLatitude()*1E6; 
				double geoLongitude = location.getLongitude()*1E6; 
				return new GeoPoint((int) geoLatitude, (int) geoLongitude);
			}
			else{
				return new GeoPoint(0,0);
			}
	}
	public double GetDistance(GeoPoint gp1,GeoPoint gp2){
		double Lat1r = ConvertDegreeToRadians(gp1.getLatitudeE6()/1E6);
		double Lat2r = ConvertDegreeToRadians(gp2.getLatitudeE6()/1E6);
		double Long1r= ConvertDegreeToRadians(gp1.getLongitudeE6()/1E6);
		double Long2r= ConvertDegreeToRadians(gp2.getLongitudeE6()/1E6);
		/* ����뾶(KM) */
		double R = 6371;
		double d = 
				Math.acos(Math.sin(Lat1r)*Math.sin(Lat2r)+
				Math.cos(Lat1r)*Math.cos(Lat2r)*
                Math.cos(Long2r-Long1r))*R;
		return d*1000;
	}
	public double ConvertDegreeToRadians(double degrees){
		return (Math.PI/180)*degrees;
	}
	public boolean CloseToEachOther(GeoPoint gp1,GeoPoint gp2){
		Log.i("��ǰ���:", GetDistance(gp1,gp2)+"��");
		if(GetDistance(gp1,gp2)<200)
			return true;
		else  
			return false;
	}
	public void executeListen(){
		
		String loc=HttpUtil.QueryWorkerCurrentLocation(GetWorkerID());
		
		Log.i("id:",GetWorkerID()+"   loc="+loc);
		if(loc==null||loc.length()==0||loc.equals("")||loc.equals(" ")){
			return;
		}
		else{
			Log.i("�û���:","���Աλ��: "+loc);		
			String tmp[]=loc.split(",");
			int lat=(int) (Double.parseDouble(tmp[0])*1E6);
			int lng=(int) (Double.parseDouble(tmp[1])*1E6);
			if(CloseToEachOther(new GeoPoint(lat,lng),GetUserLocation())){
				Log.i("�û���", "�û������Ա����ӽ���200��");
				if(SettingsActivity.getWIFIReceive(context)){
					while(true){
						if(wifi.connectToESMobile()){
					    	sm.registerListener(mySensorEventListener, sm.getDefaultSensor(sensorType),SensorManager.SENSOR_DELAY_FASTEST);
							break;
						}
					}
				}else{
					/**
					 * û�п���WIFIȷ���ջ�
					 */
			    	sm.registerListener(mySensorEventListener, sm.getDefaultSensor(sensorType),SensorManager.SENSOR_DELAY_FASTEST);
				}
			}
//			if(CloseToEachOther(new GeoPoint(lat,lng),GetUserLocation())){
//			    Log.i("�û���", "�û������Ա����ӽ�");
//			/**
//			 * �û������Ա����ӽ�
//			 * ��ʼ��֤�û���Ϣ
//			 * ˫���գ�WIFI�ж�
//			 */
//			    if(SettingsActivity.getReceiveNetWork(context).equals("GSM")){
//			    	Log.i("�û���", "GSM����ģʽ");
//			    	/**
//			    	 * ֱ��GSM���գ�����ı�����״̬
//			    	 */
//			    	//����ҡ���ֻ�����Ϊ
//			    	sm.registerListener(mySensorEventListener, sm.getDefaultSensor(sensorType),SensorManager.SENSOR_DELAY_FASTEST);
//			    	return;
//			    }
//			    else if(SettingsActivity.getReceiveNetWork(context).equals("WIFI")){
//			    	TYPE="WIFI";
//			    	if(wifi.connectToESMobile()){
//						/**
//						 * ������Ա��WIFI��Χ�ڡ����ӳɹ�;
//						 * ��������Socket���ӿ��Ա���ֻ��������ˡ�
//						 * ����ɹ���֪ͨ����������֤��ϣ�ǩ����ϣ��������ݿ��и������ݡ���
//						 */
//				    	Log.i("WIFI����", "�ֻ��ź���������WIFI�ȵ㽻��...");
//						try {
//							 Log.i("Socket�ͻ���", "����������...");
//							 Socket socket = new Socket(SERVERIP, SERVERPORT);
//							 PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true );
//							 verifyMessage=generateVerifyMessage();
//							 out.println(verifyMessage);
//							 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//							 String clientContent = in.readLine();
//							 if(clientContent.equals("1")){
//								 //ǩ�ճɹ���
//							     sm.registerListener(mySensorEventListener, sm.getDefaultSensor(sensorType),SensorManager.SENSOR_DELAY_FASTEST);
//								 return;
//							 }
//							 else{
//					     		HttpUtil.UpdateTradeStatus(HttpUtil.queryTradeInfo("receiver_id", user_id, "trade_number"), "4");
//								 Log.i("Socket��֤ʧ��", "ǩ��ʧ��");
//								 return;
//							 }
//						} catch (UnknownHostException e) {
//							e.printStackTrace();
//						} catch (IOException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//				}
//			    }
//				
//			}
		}
			
	}

	private void onVibrator() {
		  Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		  if (vibrator == null) {
		   Vibrator localVibrator = (Vibrator) context.getApplicationContext().getSystemService("vibrator");
		   vibrator=localVibrator;
		  }
		  vibrator.vibrate(100L);
	  }
	 public void initShake() {
		  lastTime = 0;
		  duration = 0;
		  curTime = 0;
		  initTime = 0;
		  last_x = 0.0f;
		  last_y = 0.0f;
		  last_z = 0.0f;
		  shake = 0.0f;
		  totalShake = 0.0f;
	}
	 public final SensorEventListener mySensorEventListener = new SensorEventListener() {
		  @Override
		  public void onAccuracyChanged(android.hardware.Sensor sensor,int accuracy) {}
		  @Override
		  public void onSensorChanged(SensorEvent event) {
			  if (event.sensor.getType() == android.hardware.Sensor.TYPE_ACCELEROMETER) {
				    // ��ȡ���ٶȴ���������������
				    float x = event.values[SensorManager.DATA_X];
				    float y = event.values[SensorManager.DATA_Y];
				    float z = event.values[SensorManager.DATA_Z];
				    // ��ȡ��ǰʱ�̵ĺ�����
				    curTime = System.currentTimeMillis();
				    // 100������һ��
				    float aa = curTime - lastTime;
				    if ((curTime - lastTime) > 100) {
				     duration = (curTime - lastTime);
				     // ���ǲ��Ǹտ�ʼ�ζ�
				     if (last_x == 0.0f && last_y == 0.0f && last_z == 0.0f) {
				      // last_x��last_y��last_zͬʱΪ0ʱ����ʾ�ոտ�ʼ��¼
				      initTime = System.currentTimeMillis();
				     } else {
				      // ���λζ�����
				      shake = (Math.abs(x - last_x) + Math.abs(y - last_y) + Math.abs(z - last_z));
				      // / duration * 1000;
				     }
				     // ��ÿ�εĻζ�������ӣ��õ�����ζ�����
				     totalShake += shake;
				     // �ж��Ƿ�Ϊҡ��
				     if (shake > 20) {
				    	agreeGSMReceive();
				    	onVibrator();
				    	initShake();
				     }
				     last_x = x;
				     last_y = y;
				     last_z = z;
				     lastTime = curTime;
				    }
				   }
				  }
				 };
		 public void agreeGSMReceive(){
			 /**
     		 *����Activity���͹㲥
     		 */
     		Intent fine = new Intent("SENT");
     		String number=HttpUtil.queryTradeInfo("receiver_id",user_id,"trade_number");
     		String name=HttpUtil.queryTradeInfo("receiver_id",user_id,"receiver_name");
             fine.putExtra("MESSAGE","�ͻ�["+name+"]����,��İ���ǩ�ճɹ�");
             context.sendBroadcast(fine);
             
     		HttpUtil.UpdateTradeStatus(HttpUtil.queryTradeInfo("receiver_id", user_id, "trade_number"), "2");	
     		HttpUtil.UpdateStatistics(HttpUtil.queryTradeInfo("receiver_id", user_id, "deliverer_id"),"today_sent","up");
    		HttpUtil.UpdateStatistics(HttpUtil.queryTradeInfo("receiver_id", user_id, "deliverer_id"),"for_send","down");
    		HttpUtil.UpdateCash(HttpUtil.queryTradeInfo("receiver_id", user_id, "deliverer_id"),
    				HttpUtil.queryTradeInfo("id", HttpUtil.queryTradeInfo("receiver_id", user_id, "deliverer_id"), "trade_cash"));
            
    		 int icon = android.R.drawable.stat_notify_more;
             long when = System.currentTimeMillis();
             // ��һ������Ϊͼ��,�ڶ�������Ϊ����,������Ϊ֪ͨʱ��
             Notification notification = new Notification(icon, null, when);
             Intent openintent = new Intent(context, TradeStatus.class);
             Bundle bundle =new Bundle();
             bundle.putString("user_id",user_id);
             bundle.putString("user_name",name);
             bundle.putString("trade_number",number);
             openintent.putExtras(bundle);
             // �������Ϣʱ�ͻ���ϵͳ����openintent��ͼ
             PendingIntent contentIntent = PendingIntent.getActivity(context, 0,openintent, 0);
             notification.setLatestEventInfo(context,"�����ʰ�"+number+"ǩ�����", "�����鿴���鲢���Ծܾ�ǩ��", contentIntent);
             notification.defaults=Notification.DEFAULT_SOUND|Notification.DEFAULT_VIBRATE;  
             notification.vibrate=pattern;
             notificationManager.notify(0, notification);
		     Log.i("�û���", "GSMǩ����ϣ��������������");
		     HttpUtil.DeleteCurrentUserLocation(user_id);
		     sm.unregisterListener(mySensorEventListener);
		}
}
