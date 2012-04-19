package info.ericyue.es.activity;

import java.util.ArrayList;
import java.util.HashMap;


import info.ericyue.es.R;
import info.ericyue.es.util.HttpUtil;
import info.ericyue.es.util.TutorialDialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;


public class ExpressSensorClient extends Activity{
	private int id;
	private Bundle bundle;
	private Intent i;
	private ListView listView;
	private TextView infoWall;
	private ProgressBar progressbar;
	private serviceReceiver receiver;
	private boolean show_tutorial;
	private long exitTime = 0;
	@Override  
	public boolean onKeyDown(int keyCode, KeyEvent event) {  
	    if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){  
	    	if((System.currentTimeMillis()-exitTime) > 2000){  
	    		Toast.makeText(getApplicationContext(), "�ٰ�һ���˳�����", Toast.LENGTH_SHORT).show();                                exitTime = System.currentTimeMillis();  
	    	}  
	    	else{  
	    		finish();
	          Intent i = new Intent( ExpressSensorClient.this, LocationListenService.class );
	          /* ��stopService�����ر�Intent */
	          stopService(i);
	    	  System.exit(0);  
	    	}  
	    	return true;  
	    }  
	    return super.onKeyDown(keyCode, event);  
	}  
	final void showTutorial() {
		boolean showTutorial = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("show_tutorial", true);
		if (showTutorial) {
			final TutorialDialog dlg = new TutorialDialog(this);
			dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					CheckBox cb = (CheckBox) dlg.findViewById(R.id.show_tutorial);
					if (cb != null && cb.isChecked()) {
						SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ExpressSensorClient.this);
						prefs.edit().putBoolean("show_tutorial", false).commit();
					}
				}
			});
			dlg.show();
		} else {

		}
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//����ʾ������
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
		show_tutorial = SettingsActivity.getShowTutorial(this);
		progressbar=(ProgressBar) findViewById(R.id.ProgressBar);
		infoWall=(TextView) findViewById(R.id.infoWall);
 		headInfo(false,"��ӭ����Express Sensor(�ͻ���)��");
		listView = (ListView)findViewById(R.id.HomeListView);
        listView.setCacheColorHint(0);

		bundle=this.getIntent().getExtras();
		id=Integer.parseInt(getIdByUsername(bundle.getString("username")));
		bundle.putString("id", id+"");//�û���ID
		if(!isClient()){
			showDialog("�˻�������ð汾�ͻ��˲�ƥ��");
		}
		else{
	        //���ListView
			fillItemList();
			if(show_tutorial){
				showTutorial();
			}
			/**
			 * ����ϵͳ���� �����û����ͻ�Ա�˵�λ��ֱ�߾��롣
			 */
			i = new Intent(this, LocationListenService.class );
	        i.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
	        i.putExtras(bundle);
	        startService(i); 
		}
		
	}
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {}
	/**
	 * MenuInflater ��������������menuĿ¼�µĲ˵������ļ�
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.home, menu);
		return super.onCreateOptionsMenu(menu);
	}
	/**
	 * ���²˵������Ӧ
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.about_menu_item:
			About.launch(ExpressSensorClient.this,bundle);
			break;
		case R.id.about_menu_min:
			Intent i= new Intent(Intent.ACTION_MAIN);                
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.addCategory(Intent.CATEGORY_HOME);
            startActivity(i);
			break;
		case R.id.settings_menu_item:
			SettingsActivity.launch(ExpressSensorClient.this,bundle);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void showDialog(String msg){
		AlertDialog.Builder builder=new AlertDialog.Builder(this);
		builder.setTitle("��ݴ���").setMessage(msg).setCancelable(false).setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				Intent intent = new Intent(ExpressSensorClient.this, LoginActivity.class);
				intent.putExtras(bundle);
				startActivity(intent);
				finish();
			}
		});
		AlertDialog alert=builder.create();
		alert.show();
	}
	private void fillItemList(){
		ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();  
		String[] itemStr={"��������","������Ϣ","WIFI����ǩ�տ���","ϵͳ����","����"};
		Integer[] iconBag={R.drawable.trace,R.drawable.nfc,R.drawable.qrcode,R.drawable.settings,R.drawable.about};
 		for(int i=0;i<5;i++){
			HashMap<String, Object> map = new HashMap<String, Object>();  
		    map.put("ItemImage", iconBag[i]);//ͼ����Դ��ID  
		    map.put("ItemTitle", itemStr[i]);
		    listItem.add(map); 
		}
	    SimpleAdapter listItemAdapter = new SimpleAdapter(this,listItem,//����Դ   
	    	R.layout.purple_row,  
	    	//��̬������ImageItem��Ӧ������          
	    	new String[] {"ItemImage","ItemTitle"},   
	    	//ImageItem��XML�ļ������һ��ImageView,����TextView ID  
	    	new int[] {R.id.leftHead,R.id.PurpleRowTextView}  
	    ); 
		listView.setAdapter(listItemAdapter);
		listView.setOnItemClickListener(itemListener);
	}
	private String getIdByUsername(String username) {
		String queryString="username="+username;
    	String url=HttpUtil.BASE_URL+"servlet/QueryUser?"+queryString;
    	String id=HttpUtil.queryStringForPost(url);
    	return id;
	}
	public boolean isClient(){
    	return HttpUtil.queryRole(id).equals("0");
    }
	public void headInfo(boolean run,String msg){
		if(!run)
			progressbar.setVisibility(View.GONE);
		else 
			progressbar.setVisibility(View.VISIBLE);
		infoWall.setText(msg);
	}
	private OnItemClickListener itemListener = new OnItemClickListener(){
		@Override
		public void onItemClick(AdapterView<?> adapterView, View view, int position,
				long id) {
			switch(position){
			case 0:
				TraceRecord.launch(ExpressSensorClient.this,bundle);
				break;
			case 1:
				PersonInfo.launch(ExpressSensorClient.this,bundle);
				break;
			case 2:
				boolean wifi=SettingsActivity.getWIFIReceive(ExpressSensorClient.this);
				SettingsActivity.setWIFIReceive(ExpressSensorClient.this,!wifi);
				Toast.makeText(ExpressSensorClient.this, "WIFI����ǩ��"+ (!wifi==true?"����":"�ر�"), Toast.LENGTH_SHORT).show();				
				break;	
			case 3:
				SettingsActivity.launch(ExpressSensorClient.this,bundle);
 				break;
			case 4:
				About.launch(ExpressSensorClient.this,bundle);
				
				break;
 			}	
		}
	};
	public void turnWIFI(){
		
	}
	public class serviceReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			try{
		        /* ȡ�����Ժ�̨������Broadcast�Ĳ��� */
		        Bundle b = intent.getExtras();
		        String message = b.getString("MESSAGE");
		        Toast.makeText(ExpressSensorClient.this, message, Toast.LENGTH_LONG).show();
		        stopService(i);
		        HttpUtil.DeleteCurrentUserLocation(id+""); 
		     }catch(Exception e){
		        e.getStackTrace();
		      }
		}
	  
	}
	@Override
	protected void onResume(){
	    // TODO Auto-generated method stub
	    try{
	      /* ǰ�������������ڿ�ʼ */
	      IntentFilter filter;
	      /* �Զ���Ҫ���˵Ĺ㲥ѶϢ(DavidLanz) */
	      filter = new IntentFilter("SENT");
	      receiver = new serviceReceiver();
	      registerReceiver(receiver, filter);
	    }catch(Exception e){
	      e.getStackTrace();
	    }
	    super.onResume();
	  }
	@Override
	protected void onPause(){
	    // TODO Auto-generated method stub
	    /* ǰ�������������ڽ������������ϵͳע���Receiver */
	    unregisterReceiver(receiver);
	    super.onPause();
	  }
}
