package com.orvibo.homemate.view.custom;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.view.custom.wheelview.WheelBo;
import com.orvibo.homemate.view.custom.wheelview3d.LoopView;
import com.orvibo.homemate.view.custom.wheelview3d.OnItemSelectedListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Allen on 2015/4/17.
 */
public class WheelViewFragment extends Fragment {
    private LinearLayout first_ll;
    private LinearLayout second_ll;
    private TextView first_tv;
    private TextView second_tv;
    private LoopView mLoopView1;
    private LoopView mLoopView2;
    private int minutes = 0;
    private int second = 0;
    private int totalMinutes = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wheel_view_fragment, null);
        mLoopView1 = (LoopView) view.findViewById(R.id.firstWheelView);
        mLoopView2 = (LoopView) view.findViewById(R.id.secondWheelView);
        //设置字体大小
        mLoopView1.setTextSize(20);
        mLoopView2.setTextSize(20);
        first_ll = (LinearLayout) view.findViewById(R.id.first_ll);
        second_ll = (LinearLayout) view.findViewById(R.id.second_ll);

        first_tv = (TextView) view.findViewById(R.id.first_tv);
        second_tv = (TextView) view.findViewById(R.id.second_tv);

        mLoopView1.setListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int position) {
                if (getActivity() != null) {
                    minutes = position;
                    setTip();
                }
            }
        });

        mLoopView2.setListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int position) {
                if (getActivity() != null) {
                    second = position;
                    setTip();
                }
            }
        });
        return view;
    }

    private void setTip() {
        totalMinutes = minutes * 60 + second;
        int minute = totalMinutes / 60;
        int second = totalMinutes % 60;
        if (totalMinutes < 60) {
            String tip = getActivity().getString(R.string.scene_delay_time_tip);
            second_tv.setText(String.format(tip, second));
        } else {
            String tip2 = getActivity().getString(R.string.scene_delay_time_tip2);
            second_tv.setText(String.format(tip2, minute, second));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mLoopView1 != null) {
            mLoopView1.getSelectedItem();
        }
    }

    public void setFirstTip(String tip) {
        first_tv.setText(tip);
    }

    public void setSecondTip(String tip) {
        second_tv.setText(tip);
    }

    public void setWheelText(List<WheelBo> firstWheelBo, List<WheelBo> secondWheelBo) {
        if (firstWheelBo == null || firstWheelBo.isEmpty()) {
            hide(true);
        } else {
            ArrayList<String> items = new ArrayList<>();
            for (WheelBo item : firstWheelBo) {
                items.add(item.getName());
            }
            mLoopView1.setItems(items);
        }
        if (secondWheelBo == null || secondWheelBo.isEmpty()) {
            hide(false);
        } else {
            ArrayList<String> items = new ArrayList<>();
            for (WheelBo item : secondWheelBo) {
                items.add(item.getName());
            }
            mLoopView2.setItems(items);
        }
    }

    public void setFirstWheelText(List<WheelBo> firstWheelBo) {
        setWheelText(firstWheelBo, null);
    }

    public void setSecondWheelText(List<WheelBo> secondWheelBo) {
        setWheelText(null, secondWheelBo);
    }

    public void selectFirstIndex(int index) {
        if (mLoopView1 != null) {
            mLoopView1.setInitPosition(index);
            minutes = index;
            setTip();
        }
    }

    public void selectSecondIndex(int index) {
        if (mLoopView2 != null) {
            mLoopView2.setInitPosition(index);
            second = index;
            setTip();
        }
    }

    public int getFirstWheelSelectIndex() {
        if (mLoopView1 != null) {
            return mLoopView1.getSelectedItem();
        }
        return Constant.INVALID_NUM;
    }

    public int getSecondWheelSelectIndex() {
        if (mLoopView2 != null) {
            return mLoopView2.getSelectedItem();
        }
        return Constant.INVALID_NUM;
    }

    private void hide(boolean isFirst) {
        if (isFirst) {
            first_ll.setVisibility(View.GONE);
            first_tv.setVisibility(View.GONE);
        } else {
            second_ll.setVisibility(View.GONE);
            second_tv.setVisibility(View.GONE);
        }
    }
}
