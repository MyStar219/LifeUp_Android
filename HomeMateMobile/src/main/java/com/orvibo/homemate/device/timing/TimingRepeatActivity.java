//package com.orvibo.homemate.device.timing;
//
//import android.app.Activity;
//import android.content.Intent;
//import android.content.res.Configuration;
//import android.os.Bundle;
//import android.view.View;
//import android.widget.AdapterView;
//import android.widget.ListView;
//
//import com.smartgateway.app.R;
//import com.orvibo.homemate.data.Constant;
//import com.orvibo.homemate.util.BroadcastUtil;
//import com.orvibo.homemate.util.StringUtil;
//import com.orvibo.homemate.util.WeekUtil;
//import com.orvibo.homemate.view.custom.NavigationCocoBar;
//
//import java.util.Map;
//
//
//public class TimingRepeatActivity extends Activity implements NavigationCocoBar.OnLeftClickListener{
//	private static final String TAG = "TimingRepeatActivity";
//    private NavigationCocoBar navigationCocoBar;
//	private TimingRepeatAdapter timingRepeatAdapter;
//    private Map<Integer, Integer> weekMap;
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.timing_repeat_activity);
//        navigationCocoBar = (NavigationCocoBar) findViewById(R.id.navigationBar);
//        navigationCocoBar.setOnLeftClickListener(this);
//        int week = getIntent().getIntExtra(Constant.WEEK, 0);
//        weekMap = WeekUtil.weekIntToMap(week);
//		choiceWeek();
//	}
//
//	@Override
//	public void onConfigurationChanged(Configuration newConfig) {
//		super.onConfigurationChanged(newConfig);
//	}
//
//    @Override
//    public void onLeftClick(View v) {
//        Intent intent = new Intent();
//        intent.putExtra(Constant.WEEKS, timingRepeatAdapter.getWeeks());
////        TimingRepeatActivity.this.setResult(RESULT_OK, intent);
//        this.finish();
//    }
//
//    private void choiceWeek() {
//        if (timingRepeatAdapter == null) {
//            timingRepeatAdapter = new TimingRepeatAdapter(this,weekMap);
//            ListView listView = (ListView) findViewById(R.id.repeatListView);
//            listView.setAdapter(timingRepeatAdapter);
//
//            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//                @Override
//                public void onItemClick(AdapterView<?> parent, View view, int position,
//                                        long id) {
//                    timingRepeatAdapter.checkPosition(position);
//                    obtainWeekByPostion(position);
//                    choiceWeek();
//                    Intent intent = new Intent(TimingAddActivity.ME);
//                    intent.putExtra(Constant.EVENT, 0);
//                    intent.putExtra(Constant.WEEK, getWeekInt(weekMap));
//                    BroadcastUtil.sendBroadcast(TimingRepeatActivity.this, intent);
//                }
//            });
//        } else {
//            timingRepeatAdapter.choiceWeek(weekMap);
//        }
//	}
//
//    private void obtainWeekByPostion(int position) {
//        if(position == 0)
//            position = 7;
//
//        weekMap.put(0, 1);
//        if (weekMap.containsKey(position)) {
//            weekMap.remove(position);
//        } else if (position > 0) {
//            weekMap.put(position, 8 - position);
//        }
//
//        if(weekMap.size() == 1)
//        {
//            weekMap.put(0, 0);
//        }
//    }
//
//    public static void weekIntToMap(Map<Integer, Integer> weekMap, int week) {
//        if (week > 0) {
//            weekMap.clear();
//            weekMap.put(0, 1);
//            String weekStr = StringUtil.byte2BinaryString((byte) week);
//            byte[] weekByts = weekStr.getBytes();
//            int len = weekStr.length();
//            for (int i = 1; i < len; i++) {
//                char w = (char) weekByts[i];
//                if (w == '1') {
//                    weekMap.put(i, 8 - i);
//                }
//            }
//        } else if (week == 0) {
//            weekMap.clear();
//            weekMap.put(0, 0);
//        } else {
//            weekMap.clear();
//        }
//    }
//
//    public static int getWeekInt(Map<Integer, Integer> weekMap) {
//        int ret = -1;
//        if (weekMap.containsKey(0)) {
//            if (weekMap.get(0) == 0) {
//                ret = 0;
//            } else {
//                String[] tempArr = { "1", "0", "0", "0", "0", "0", "0", "0" };
//                for (int i = 1; i < 8; i++) {
//                    if (weekMap.containsKey(i)) {
//                        if (i == 7) {
//                            tempArr[1] = "" + 1;
//                        } else {
//                            tempArr[8 - i] = "" + 1;
//                        }
//                    }
//                }
//                StringBuffer weekStr = new StringBuffer();
//                for (int i = 0; i < tempArr.length; i++) {
//                    weekStr.append(tempArr[i]);
//                }
//
//                ret = StringUtil.binaryToDecimal(weekStr.toString());
//            }
//        }
//        return ret;
//    }
//
//
//}
