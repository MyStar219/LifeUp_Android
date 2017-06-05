package com.orvibo.homemate.common.appwidget;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.widget.RemoteViews;

import com.smartgateway.app.R;
import com.orvibo.homemate.data.ArmType;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.user.LoginActivity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * 该类主要是一个广播接收类，只负责界面显示，当接收器接收到数据后根据类型进行处理
 */
public class SmartWidgetProvider extends android.appwidget.AppWidgetProvider {
	private static String TAG = SmartWidgetProvider.class.getSimpleName();

	List<WidgetItem> sceneWidgetList;
	List<WidgetItem> deviceWidgetList;
	List<WidgetItem> linkageWidgetList;
	List<WidgetItem> energyWidgetList;
	RemoteViews remoteView;

    private static  String  SERVICE_NAME      ="com.orvibo.homemate.common.appwidget.WidgetManageService";
	private static  String  MAINACTIVITY_NAME ="com.orvibo.homemate.common.MainActivity";
	public  static  String  MAIN_UPDATE_UI    = "main_activity_update_ui";  //Action

	private PendingIntent getPendingIntent(Context context, int buttonId) {

		Intent intent = new Intent();
		intent.setClass(context, SmartWidgetProvider.class);
		intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
		intent.setData(Uri.parse("harvic:" + buttonId));
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
		return pi;
	}

	/**
	 * 更新所有的 widget 其中model_9 ,model_10, model_11 为灯光控制位置
	 * @param context
	 * @param appWidgetManager
	 * @param
     */
	private void pushUpdate(Context context, AppWidgetManager appWidgetManager,HashMap<String ,List<WidgetItem>> hashMap) {
		TypedArray icons = context.getResources().obtainTypedArray(R.array.widget_icons);
		remoteView = new RemoteViews(context.getPackageName(), R.layout.panel_widget);


        //初始化控件，每次刷新都要初始化
		remoteView.setTextViewText(R.id.text_view0, context.getResources().getString(R.string.no_set_widget));
		remoteView.setImageViewBitmap(R.id.icon_img0,null);
		remoteView.setTextViewText(R.id.text_view1, context.getResources().getString(R.string.no_set_widget));
		remoteView.setImageViewBitmap(R.id.icon_img1,null);
		remoteView.setTextViewText(R.id.text_view2, context.getResources().getString(R.string.no_set_widget));
		remoteView.setImageViewBitmap(R.id.icon_img2,null);
		remoteView.setTextViewText(R.id.text_view3, context.getResources().getString(R.string.no_set_widget));
		remoteView.setImageViewBitmap(R.id.icon_img3,null);
		remoteView.setTextViewText(R.id.text_view4, context.getResources().getString(R.string.no_set_widget));
		remoteView.setImageViewBitmap(R.id.icon_img4,null);

		remoteView.setTextViewText(R.id.text_view7, context.getResources().getString(R.string.no_set_widget));
		remoteView.setImageViewBitmap(R.id.icon_img7,null);
		remoteView.setTextViewText(R.id.text_view8, context.getResources().getString(R.string.no_set_widget));
		remoteView.setImageViewBitmap(R.id.icon_img8,null);
		remoteView.setTextViewText(R.id.text_view9, context.getResources().getString(R.string.no_set_widget));
		remoteView.setImageViewBitmap(R.id.icon_img9,null);
		remoteView.setTextViewText(R.id.text_view10, context.getResources().getString(R.string.no_set_widget));
		remoteView.setImageViewBitmap(R.id.icon_img10,null);
		remoteView.setTextViewText(R.id.text_view11, context.getResources().getString(R.string.no_set_widget));
		remoteView.setImageViewBitmap(R.id.icon_img11,null);


		if(hashMap!=null) {
			if (hashMap.get("scene") != null) {
				sceneWidgetList = hashMap.get("scene");
			}
			if (hashMap.get("device") != null) {
				deviceWidgetList = hashMap.get("device");
			}
			if (hashMap.get("linkage") !=null) {
				linkageWidgetList = hashMap.get("linkage");
			}
			if(hashMap.get("energy")!=null){
				energyWidgetList = hashMap.get("energy");
			}


			//绑定情景模式桌面部件
			if (sceneWidgetList != null) {
					if (sceneWidgetList.size() > 0) {
						int position = -1;
						for (WidgetItem scene : sceneWidgetList) {
							position++;
							switch (position) {
								case 0:
									BitmapDrawable drawable0 = (BitmapDrawable)icons.getDrawable(scene.getSrcId());
									remoteView.setImageViewBitmap(R.id.icon_img0,drawable0.getBitmap());
									remoteView.setOnClickPendingIntent(R.id.model_0,getPendingIntent(context, R.id.model_0));
									remoteView.setTextViewText(R.id.text_view0, scene.getWidgetName());
									break;
								case 1:
									BitmapDrawable drawable1 = (BitmapDrawable)icons.getDrawable(scene.getSrcId());
									remoteView.setImageViewBitmap(R.id.icon_img1,drawable1.getBitmap());
									remoteView.setOnClickPendingIntent(R.id.model_1,getPendingIntent(context, R.id.model_1));
									remoteView.setTextViewText(R.id.text_view1, scene.getWidgetName());
									break;
								case 2:
									BitmapDrawable drawable2 = (BitmapDrawable)icons.getDrawable(scene.getSrcId());
									remoteView.setImageViewBitmap(R.id.icon_img2,drawable2.getBitmap());
									remoteView.setOnClickPendingIntent(R.id.model_2,getPendingIntent(context, R.id.model_2));
									remoteView.setTextViewText(R.id.text_view2, scene.getWidgetName());
									break;
								case 3:
									BitmapDrawable drawable3 = (BitmapDrawable)icons.getDrawable(scene.getSrcId());
									remoteView.setImageViewBitmap(R.id.icon_img3,drawable3.getBitmap());
									remoteView.setOnClickPendingIntent(R.id.model_3,getPendingIntent(context, R.id.model_3));
									remoteView.setTextViewText(R.id.text_view3, scene.getWidgetName());
									break;
								case 4:
									BitmapDrawable drawable4 = (BitmapDrawable)icons.getDrawable(scene.getSrcId());
									remoteView.setImageViewBitmap(R.id.icon_img4,drawable4.getBitmap());
									remoteView.setOnClickPendingIntent(R.id.model_4,getPendingIntent(context, R.id.model_4));
									remoteView.setTextViewText(R.id.text_view4, scene.getWidgetName());
									break;
							}
						}
					}
			}

			//由于“外出安防”和“在家安防”是成对出现的，故使用如下设置方法
			if(linkageWidgetList!=null){
				int position = 0;
				WidgetItem out_widget = null;
				WidgetItem in_widget = null;
				for(WidgetItem linkage:linkageWidgetList){
					position++;
					switch (position){
						case 1:
							out_widget = linkage;
							remoteView.setTextViewText(R.id.out_safe_tv,linkage.getWidgetName());
							remoteView.setOnClickPendingIntent(R.id.out_safe_tv, getPendingIntent(context, R.id.out_safe_tv));
							remoteView.setTextColor(R.id.out_safe_tv,linkage.getIsArm()== ArmType.ARMING?
									context.getResources().getColor(R.color.yellow):context.getResources().getColor(R.color.white));
							break;
						case 2:
							in_widget= linkage;
							remoteView.setTextViewText(R.id.in_safe_tv,linkage.getWidgetName());
							remoteView.setOnClickPendingIntent(R.id.in_safe_tv,getPendingIntent(context, R.id.in_safe_tv));
							remoteView.setTextColor(R.id.in_safe_tv,linkage.getIsArm()== ArmType.ARMING?
									context.getResources().getColor(R.color.yellow):context.getResources().getColor(R.color.white));
							break;

					}
				}

				remoteView.setOnClickPendingIntent(R.id.leave_safe_tv,getPendingIntent(context, R.id.leave_safe_tv));
				if(out_widget!=null&&in_widget!=null) {
					remoteView.setTextColor(R.id.leave_safe_tv, out_widget.getIsArm() == in_widget.getIsArm() && in_widget.getIsArm() == ArmType.DISARMING ?
							context.getResources().getColor(R.color.yellow) : context.getResources().getColor(R.color.white));
				}

			}

			//绑定灯或者插座桌面部件
			if (deviceWidgetList != null) {
					int position = 6;
					for (WidgetItem device:deviceWidgetList) {
						position++;
						switch (position) {
							case 7:
								remoteView.setOnClickPendingIntent(R.id.model_7,getPendingIntent(context, R.id.model_7));
								remoteView.setImageViewResource(R.id.icon_img7,getResId(device.getDeviceType(),device.getStatus()));
								remoteView.setTextViewText(R.id.text_view7, device.getWidgetName()+(device.getStatus().equals("0")?"关闭":"打开"));
								break;
							case 8:
								remoteView.setOnClickPendingIntent(R.id.model_8,getPendingIntent(context, R.id.model_8));
								remoteView.setImageViewResource(R.id.icon_img8,getResId(device.getDeviceType(),device.getStatus()));
								remoteView.setTextViewText(R.id.text_view8, device.getWidgetName()+(device.getStatus().equals("0")?"关闭":"打开"));
								break;
							case 9:
								remoteView.setOnClickPendingIntent(R.id.model_9,getPendingIntent(context, R.id.model_9));
								remoteView.setImageViewResource(R.id.icon_img9,getResId(device.getDeviceType(),device.getStatus()));
								remoteView.setTextViewText(R.id.text_view9, device.getWidgetName()+(device.getStatus().equals("0")?"关闭":"打开"));
								break;
							case 10:
								remoteView.setOnClickPendingIntent(R.id.model_10,getPendingIntent(context, R.id.model_10));
								remoteView.setImageViewResource(R.id.icon_img10,getResId(device.getDeviceType(),device.getStatus()));
								remoteView.setTextViewText(R.id.text_view10, device.getWidgetName()+(device.getStatus().equals("0")?"关闭":"打开"));
								break;
							case 11:
								remoteView.setOnClickPendingIntent(R.id.model_11,getPendingIntent(context, R.id.model_11));
								remoteView.setImageViewResource(R.id.icon_img11,getResId(device.getDeviceType(),device.getStatus()));
								remoteView.setTextViewText(R.id.text_view11, device.getWidgetName()+(device.getStatus().equals("0")?"关闭":"打开"));
								break;
						}
					}
			}

			if(energyWidgetList!=null){
				remoteView.setOnClickPendingIntent(R.id.all_close_tv,getPendingIntent(context, R.id.all_close_tv));
				remoteView.setTextViewText(R.id.open_num_tv,context.getString(R.string.current_open_num)+energyWidgetList.size());

			}
		}

		// 相当于获得所有本程序创建的appwidget
		ComponentName componentName = new ComponentName(context,SmartWidgetProvider.class);
		appWidgetManager.updateAppWidget(componentName, remoteView);
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
						 int[] appWidgetIds) {

		pushUpdate(context,appWidgetManager,null);

		//启动时，判断service是否启动
		if(!isServiceWork(context,SERVICE_NAME)){
			Intent intent = new Intent(context,WidgetManageService.class);
			context.startService(intent);
		}
		else{
			WidgetUpdateEvent event = 	new WidgetUpdateEvent(0);
			EventBus.getDefault().post(event);
		}


        super.onUpdate(context,appWidgetManager,appWidgetIds);

	}

	// widget被删除时调用
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {

		super.onDeleted(context, appWidgetIds);
	}

	// 接收广播的回调函数
	@Override
	public void onReceive(Context context, Intent intent) {

		String action = intent.getAction();

		if (intent.hasCategory(Intent.CATEGORY_ALTERNATIVE)) {
			Uri data = intent.getData();
			int buttonId = Integer.parseInt(data.getSchemeSpecificPart());
			switch (buttonId) {
				case R.id.model_0:
					pushAction(context,0,1);
					break;
				case R.id.model_1:
					pushAction(context,1,1);
					break;
				case R.id.model_2:
					pushAction(context,2,1);
					break;
				case R.id.model_3:
					pushAction(context,3,1);
					break;
				case R.id.model_4:
					pushAction(context,4,1);
					break;
				case R.id.model_7:
					pushAction(context,0,2);
					break;
				case R.id.model_8:
					pushAction(context,1,2);
					break;
				case R.id.model_9:
					pushAction(context,2,2);
					break;
				case R.id.model_10:
					pushAction(context,3,2);
					break;
				case R.id.model_11:
					pushAction(context,4,2);
					break;
				case R.id.out_safe_tv:
					pushAction(context,0,3);
					break;
				case R.id.in_safe_tv:
					pushAction(context,1,3);
					break;
				case R.id.leave_safe_tv:
					pushAction(context,2,4);
					break;
				case R.id.all_close_tv:
					pushAction(context,2,5);
					break;
			}
		}else if (MAIN_UPDATE_UI.equals(action)){
			HashMap<String ,List<WidgetItem>> hashMap = new HashMap<>();
			hashMap= (HashMap<String ,List<WidgetItem>>) intent.getSerializableExtra("hashmap");
			if (hashMap!=null) {
				pushUpdate(context, AppWidgetManager.getInstance(context), hashMap);
			}

		}

		super.onReceive(context, intent);
	}

	private void pushAction(Context contex,int position,int type) {
		if(isServiceWork(contex,SERVICE_NAME)){
			WidgetUpdateEvent event = 	new WidgetUpdateEvent(position,type);
		    EventBus.getDefault().post(event);
	}
		else{
			if(isActivityRunning(contex,MAINACTIVITY_NAME)){
				//添加测试widget功能
				Intent intent1 = new Intent(contex,WidgetManageService.class);
				contex.startService(intent1);
			}else{
				Intent intent = new Intent(contex, LoginActivity.class);
				intent.putExtra(Constant.LOGIN_ENTRY, Constant.GuideActivity);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				contex.startActivity(intent);
			}

		}
	}

	/**
	 * 判断某个服务是否正在运行的方法
	 *
	 * @param mContext
	 * @param serviceName
	 * \
	 * @return true代表正在运行，false代表服务没有正在运行
	 */
	public boolean isServiceWork(Context mContext, String serviceName) {
		boolean isWork = false;
		ActivityManager myAM = (ActivityManager) mContext
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> myList = myAM.getRunningServices(100);
		if (myList.size() <= 0) {
			return false;
		}
		int size = myList.size();
		for (int i = 0; i <size; i++) {
			String mName = myList.get(i).service.getClassName().toString();
			if (mName.equals(serviceName)) {
				isWork = true;
				break;
			}
		}
		return isWork;
	}

	//判断指定Activity是否运行
	public static boolean isActivityRunning(Context mContext, String activityClassName){
		ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> info = activityManager.getRunningTasks(20);
		if(info != null && info.size() > 0){
			Iterator<ActivityManager.RunningTaskInfo> iterator = info.iterator();
			while (iterator.hasNext()){
				ActivityManager.RunningTaskInfo task=iterator.next();
				if(task.baseActivity.getClassName().equals(activityClassName)){
					return true;
				}
			}
		}
		return false;
	}


	private int getResId(int deiceType,String status){
		switch (deiceType){

			case DeviceType.DIMMER:
			case DeviceType.RGB:
			case DeviceType.COLOR_TEMPERATURE_LAMP:
			case DeviceType.LAMP:
				if(status.equals("1")){
					return  R.drawable.damp_close;
				}else{
					return R.drawable.damp_open;
				}
			case DeviceType.OUTLET:
			case DeviceType.S20:
				if(status.equals("1")){
					return  R.drawable.icon_socket_close;
				}else{
					return R.drawable.icon_socket_open;
				}
		}
		return 0;
	}
}
