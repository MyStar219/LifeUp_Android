//package com.orvibo.vihome.adapter;
//
//import android.content.Context;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.view.animation.Animation;
//import android.view.animation.Animation.AnimationListener;
//import android.widget.BaseAdapter;
//import android.widget.GridView;
//import android.widget.TextView;
//
//import com.smartgateway.app.R;
//import com.orvibo.homemate.bo.Floor;
//import com.orvibo.homemate.bo.Room;
//import com.orvibo.homemate.util.AnimationHelper;
//import com.orvibo.homemate.util.LogUtil;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//
///**
// * @author Smagret
// * @data 2015/03/28
// */
//public class FloorListAdapter extends BaseAdapter {
//	// private List<Floor> floors;
//	private LayoutInflater inflater;
//	private Context context;
//	private final static String TAG = FloorListAdapter.class.getSimpleName();
//	private ArrayList<HashMap<Floor, List<Room>>> floorsList;
//	private static boolean DELETE_REFRESH = false;
//	private boolean DELETEIMG_SHOW = true;
//
//	public FloorListAdapter(Context context,
//			ArrayList<HashMap<Floor, List<Room>>> floorsList,
//			boolean deleteImg_show) {
//		inflater = LayoutInflater.from(context);
//		this.floorsList = floorsList;
//		context = context;
//		DELETEIMG_SHOW = deleteImg_show;
//	}
//
//	@Override
//	public int getCount() {
//		return floorsList.size();
//	}
//
//	@Override
//	public Object getItem(int position) {
//		return floorsList.get(position).keySet().toArray()[0];
//	}
//
//	@Override
//	public long getItemId(int position) {
//		return position;
//	}
//
//	@Override
//	public View getView(int position, View convertView, ViewGroup parent) {
//		// LogUtil.d(TAG, "getView() - floors" +
//		// floorsList.get(position).keySet().toArray()[0]);
//		DeviceTypeHolder holder = null;
//		if (convertView == null) {
//			holder = new DeviceTypeHolder();
//			convertView = inflater.inflate(R.layout.floor_list_item, null);
//
//			holder.floorName_tv = (TextView) convertView
//					.findViewById(R.id.floorName_tv);
//			holder.gridView = (GridView) convertView
//					.findViewById(R.id.rooms_gv);
//			convertView.setTag(holder);
//		} else {
//			holder = (DeviceTypeHolder) convertView.getTag();
//		}
//
//		if (floorsList != null && floorsList.size() > 0) {
//
//			Floor floor = (Floor) floorsList.get(position).keySet().toArray()[0];
//			LogUtil.d(TAG, "getView() - floor" + floorsList.get(position));
//			String floorName = floor.getFloorName();
//			holder.floorName_tv.setText(floorName);
//			holder.floorName_tv.setTag(floor);
//			holder.floorName_tv.setContentDescription(String.valueOf(position));
//		}
//
//		@SuppressWarnings("unchecked")
//		HashMap<Floor, List<Room>> roomHashMap = (HashMap<Floor, List<Room>>) floorsList
//				.get(position);
//		RoomGridViewAdapter roomGridViewAdapter = new RoomGridViewAdapter(
//				context, roomHashMap,position, DELETEIMG_SHOW);
//		holder.gridView.setAdapter(roomGridViewAdapter);
//
//		if (position == floorsList.size() - 1) {
//
//			checkIfItemHasBeenMarkedAsDeleted(convertView);
//		}
//		return convertView;
//	}
//
//	private class DeviceTypeHolder {
//		private TextView floorName_tv;
//		private GridView gridView;
//	}
//
//	/**
//	 *
//	 * @param deleteRefresh
//	 *            true 表示有删除动画;false 没有删除动画
//	 */
//	public void deleteRefresh(boolean deleteRefresh) {
//		DELETE_REFRESH = deleteRefresh;
//		notifyDataSetChanged();
//	}
//
//	private void checkIfItemHasBeenMarkedAsDeleted(View view) {
//		if (itemIsDeletable()) {
//			LogUtil.e(TAG, "checkIfItemHasBeenMarkedAsDeleted()");
//			if (floorsList.size() > 2) {
//				Animation fadeout = AnimationHelper.createFadeoutAnimation();
//				deleteOnAnimationComplete(fadeout);
//				animate(view, fadeout);
//			} else {
//				floorsList.remove(floorsList.size() - 1);
//				notifyDataSetChanged();
//			}
//
//			DELETE_REFRESH = false;
//		}
//	}
//
//	private static boolean itemIsDeletable() {
//		return DELETE_REFRESH == true;
//	}
//
//	private void deleteOnAnimationComplete(Animation fadeout) {
//		fadeout.setAnimationListener(new AnimationListener() {
//			@Override
//			public void onAnimationStart(Animation animation) {
//			}
//
//			@Override
//			public void onAnimationRepeat(Animation animation) {
//			}
//
//			@Override
//			public void onAnimationEnd(Animation animation) {
//				floorsList.remove(floorsList.size() - 1);
//				LogUtil.e(TAG, "deleteOnAnimationComplete() - floorSize"
//						+ floorsList.size());
//				notifyDataSetChanged();
//			}
//		});
//	}
//
//	private static void animate(View view, Animation animation) {
//		view.startAnimation(animation);
//		LogUtil.e(TAG, "animate() view = " + view);
//	}
//}
