package com.orvibo.homemate.device.timing;

import android.app.Activity;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartgateway.app.R;

import java.util.HashMap;
import java.util.Map;

public class TimingRepeatAdapter extends BaseAdapter {
	private String[] weeksArr;
	private String every;

	private LayoutInflater inflater;
	private Resources res;
    private Map<Integer, Integer> checkPositions = new HashMap<Integer, Integer>();
    private Map<Integer, Integer> weekMap;

	public TimingRepeatAdapter(Activity activity, Map<Integer, Integer> weekMap) {
        this.weekMap = weekMap;
		res = activity.getResources();
		weeksArr = res.getStringArray(R.array.week);
		inflater = LayoutInflater.from(activity);
		every = res.getString(R.string.timing_every);
	}

    /**
     *
     * @param weekMap
     *
     */
    public void choiceWeek(Map<Integer, Integer> weekMap) {
        this.weekMap = weekMap;
        this.notifyDataSetChanged();
    }

	@Override
	public int getCount() {
		return weeksArr.length;
	}

	@Override
	public Object getItem(int position) {
		return weeksArr[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		convertView = inflater.inflate(R.layout.timing_reqeat_item, null);
        convertView.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, parent.getResources().getDimensionPixelOffset(R.dimen.list_item_height)));

		TextView week_tv = (TextView) convertView.findViewById(R.id.repeatTextView);
		week_tv.setText(every + "" + weeksArr[position]);
		ImageView selectWeek_iv = (ImageView) convertView
				.findViewById(R.id.repeatCheckedTextView);

//        if (checkPositions.containsKey(position)) {
//            selectWeek_iv.setVisibility(View.VISIBLE);
//        } else {
//            selectWeek_iv.setVisibility(View.INVISIBLE);
//        }
        if (checkChoice(position)) {
            selectWeek_iv.setVisibility(View.VISIBLE);
        } else {
            selectWeek_iv.setVisibility(View.GONE);
        }
		return convertView;
	}

    public void checkPosition(int position) {
        if (checkPositions.containsKey(position)) {
            checkPositions.remove(position);
        } else {
            checkPositions.put(position, position);
        }
//        this.notifyDataSetChanged();
    }

    public int[] getWeeks() {
        int[] weeksArr = new int[checkPositions.size()];
        int i = 0;
        for (int w : checkPositions.values()) {
            weeksArr[i++] = w + 1;
        }
        return weeksArr;
    }

    private boolean checkChoice(int pos) {
        boolean isChecked = false;
        if (pos == 0)
            pos = 7;
        if (weekMap.containsKey(pos)) {
            isChecked = true;
        }

        if (pos == 0 && weekMap.get(0) != null && weekMap.get(0) == 0) {
            isChecked = true;
        } else if (pos > 0 && weekMap.containsKey(pos)) {
            isChecked = true;
        }
        return isChecked;
    }
}