package com.orvibo.homemate.device.control;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.FillFormatter;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.smartgateway.app.R;
import com.orvibo.homemate.bo.DataStatus;
import com.orvibo.homemate.common.ViHomeProApp;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.util.MyLogger;

import java.util.ArrayList;

/**
 * Created by snow on 2016/2/24.
 *
 * @描述 自定义曲线图界面
 */
public class TemperatureAndHumidityFragment extends Fragment {
    private LineChart mChart;
    private int deviceType;
    private static int LIMIT_TIME = 1439;//X轴时间间隔
    private DataStatus dataStatus;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        deviceType = getArguments().getInt("deviceType", DeviceType.TEMPERATURE_SENSOR);
        dataStatus = (DataStatus) getArguments().getSerializable("dataStatus");
        if(dataStatus==null)
            dataStatus = new DataStatus(deviceType);
        View v = inflater.inflate(R.layout.temperature_and_humidity_fragment, container, false);

        mChart = (LineChart) v.findViewById(R.id.chart1);
        mChart.setDrawGridBackground(false);

        // no description text
        mChart.setDescription("");
        mChart.setNoDataTextDescription("You need to provide data for the chart.");

        // enable touch gestures
        mChart.setTouchEnabled(false);

        // enable scaling and dragging
        mChart.setDragEnabled(false);
        mChart.setScaleEnabled(false);


        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);//设置是否画x坐标对应的竖线
        xAxis.setLabelsToSkip(119);
        xAxis.setAxisLineColor(R.color.gray);
        xAxis.setTextColor(ViHomeProApp.getContext().getResources().getColor(R.color.gray));
        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
        int minValue = dataStatus.getMinValue() - 2;
        if (deviceType == DeviceType.HUMIDITY_SENSOR) {
            if (dataStatus.getMinValue() <= 1)
                minValue = 0;
        }
        int maxValue = minValue + dataStatus.getAverage() * 4;
        if (deviceType == DeviceType.HUMIDITY_SENSOR) {
            if (dataStatus.getMaxValue() >= 99)
                maxValue = 100;
        }
        leftAxis.setAxisMaxValue(maxValue);//设置Y最高值
        leftAxis.setAxisMinValue(minValue);//设置Y最低值
        leftAxis.setShowOnlyMinMax(true);
        leftAxis.setDrawAxisLine(false);//Y轴边缘线不画
        leftAxis.setDrawZeroLine(false);
        leftAxis.setLabelCount(5, true);
        leftAxis.setGridColor(R.color.gray_white);
        leftAxis.setValueFormatter(new YAxisValueFormatter() {
            @Override
            public String getFormattedValue(float v, YAxis yAxis) {
                return String.valueOf((int) v);
            }
        });
        leftAxis.setTextColor(ViHomeProApp.getContext().getResources().getColor(R.color.gray));
        mChart.getAxisRight().setEnabled(false);
        mChart.getLegend().setEnabled(false);
        // add data
        setData(LIMIT_TIME);

        mChart.animateX(2500, Easing.EasingOption.EaseInOutQuart);
        return v;
    }

    private void setData(int count) {

        ArrayList<String> xVals = new ArrayList<String>();
        for (int i = 0; i <= count; i++) {
            if (i % 120 == 0) {
                xVals.add((i * 2 / 120) + "");
            } else {
                xVals.add((i) + "");
            }
        }

        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(dataStatus.getEntries(), null);
        set1.setDrawCubic(true);
        set1.setCubicIntensity(0.2f);
        //set1.setDrawFilled(true);
        set1.setDrawCircles(false);
        set1.setLineWidth(1.8f);
        set1.setCircleRadius(4f);
        set1.setCircleColor(Color.BLACK);
        set1.setHighLightColor(Color.rgb(244, 117, 117));
        set1.setFillColor(Color.WHITE);
        set1.setFillAlpha(100);
        set1.setDrawHorizontalHighlightIndicator(false);
        if (deviceType == DeviceType.TEMPERATURE_SENSOR) {
            set1.setColor(ViHomeProApp.getContext().getResources().getColor(R.color.yellow_deep));//设置弧线颜色
            //设置弧线内数据颜色
            Drawable drawable = ContextCompat.getDrawable(getActivity(), R.drawable.fade_yellow);
            set1.setFillDrawable(drawable);
            set1.setDrawFilled(true);
        } else {
            set1.setColor(ViHomeProApp.getContext().getResources().getColor(R.color.green_deep));
            Drawable drawable = ContextCompat.getDrawable(getActivity(), R.drawable.fade_green);
            set1.setFillDrawable(drawable);
            set1.setDrawFilled(true);
        }
        /**
         * 设置渐变色格式
         */
        set1.setFillFormatter(new FillFormatter() {
            @Override
            public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
                return 0;
            }
        });

        // create a data object with the datasets
        LineData data = new LineData(xVals, set1);
        data.setValueTextSize(9f);
        data.setDrawValues(false);
        // set data
        mChart.setData(data);
    }

}
