package com.orvibo.homemate.device.control;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.energy.EnergyUploadDay;
import com.orvibo.homemate.bo.energy.EnergyUploadMonth;
import com.orvibo.homemate.bo.energy.EnergyUploadWeek;
import com.orvibo.homemate.core.load.LoadParam;
import com.orvibo.homemate.core.load.LoadTarget;
import com.orvibo.homemate.dao.energy.EnergyUploadDayDao;
import com.orvibo.homemate.dao.energy.EnergyUploadMonthDao;
import com.orvibo.homemate.dao.energy.EnergyUploadWeekDao;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.TableName;
import com.orvibo.homemate.device.control.adapter.EnergyDayAdapter;
import com.orvibo.homemate.device.control.adapter.EnergyMonthAdapter;
import com.orvibo.homemate.device.control.adapter.EnergyWeekAdapter;
import com.orvibo.homemate.event.ViewEvent;
import com.orvibo.homemate.core.load.loadtable.LoadTable;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.view.custom.HorizontalListView;
import com.orvibo.homemate.view.custom.NavigationCocoBar;
import com.orvibo.homemate.view.custom.NavigationGreenBar;
import com.tencent.stat.StatService;

import java.util.Calendar;
import java.util.List;

/**
 * create wuliquan 2016-07-27
 * 用来显示电量统计
 */
public class EnergyStatisticActivity extends BaseControlActivity implements View.OnClickListener,
        NavigationCocoBar.OnRightClickListener, LoadTable.OnLoadTableListener {
    private static final String TAG = EnergyStatisticActivity.class.getSimpleName();
    private static final String LOCK = "lock";

    private HorizontalListView horizontalDayList;
    private HorizontalListView horizontalWeekList;
    private HorizontalListView horizontalMonthList;

    private EnergyDayAdapter mDayAdapter;
    private EnergyMonthAdapter mMonthAdapter;
    private EnergyWeekAdapter mWeekAdapter;

    private ImageView day_btn, week_btn, month_btn;
    private RelativeLayout average_layout;
    private TextView leve1_title, leve2_title, leve3_title;
    private TextView statistic_time, statistic_count,
            statistic_level_average, statistic_count_average,
            statistic_level2_average, statistic_count2_average;

    private EnergyUploadDayDao energyUploadDayDao;
    private EnergyUploadWeekDao energyUploadWeekDao;
    private EnergyUploadMonthDao energyUploadMonthDao;

    private List<EnergyUploadDay> dayEnergyList;
    private List<EnergyUploadWeek> weekEnergyList;
    private List<EnergyUploadMonth> monthEnergyList;

    private int oldDaySelectPosition;
    private int oldWeekSelectPosition;
    private int oldMonthSelectPosition;

    //用来标识当前属于“日”“周”或者“月”
    private int selectTableFlag;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_energy_statistic);
        LoadTable.getInstance(getApplicationContext()).setOnLoadTableListener(EnergyStatisticActivity.this);

        NavigationGreenBar navigationBar = (NavigationGreenBar) findViewById(R.id.navigationBar);
        navigationBar.setText(getResources().getString(R.string.enerty_statistic));


        day_btn = (ImageView) findViewById(R.id.day_btn);
        week_btn = (ImageView) findViewById(R.id.week_btn);
        month_btn = (ImageView) findViewById(R.id.month_btn);

        average_layout = (RelativeLayout) findViewById(R.id.average_layout);
        statistic_time = (TextView) findViewById(R.id.statistic_time);
        statistic_count = (TextView) findViewById(R.id.statistic_count);

        leve1_title = (TextView) findViewById(R.id.leve1_title);
        leve2_title = (TextView) findViewById(R.id.leve2_title);
        leve3_title = (TextView) findViewById(R.id.leve3_title);

        statistic_level_average = (TextView) findViewById(R.id.statistic_level_average);
        statistic_count_average = (TextView) findViewById(R.id.statistic_count_average);
        statistic_level2_average = (TextView) findViewById(R.id.statistic_level2_average);
        statistic_count2_average = (TextView) findViewById(R.id.statistic_count2_average);


        horizontalDayList = (HorizontalListView) findViewById(R.id.horizontalDayList);
        horizontalWeekList = (HorizontalListView) findViewById(R.id.horizontalWeekList);
        horizontalMonthList = (HorizontalListView) findViewById(R.id.horizontalMonthList);
        day_btn.setOnClickListener(this);
        week_btn.setOnClickListener(this);
        month_btn.setOnClickListener(this);

        int spanCount = 1; // 只显示一行
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.HORIZONTAL);
        layoutManager.setReverseLayout(true);
        StaggeredGridLayoutManager layoutManager1 = new StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.HORIZONTAL);
        layoutManager1.setReverseLayout(true);
        StaggeredGridLayoutManager layoutManager2 = new StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.HORIZONTAL);
        layoutManager2.setReverseLayout(true);


        horizontalDayList.setLayoutManager(layoutManager);
        horizontalWeekList.setLayoutManager(layoutManager1);
        horizontalMonthList.setLayoutManager(layoutManager2);

        horizontalDayList.setCheckIndex(3);
        horizontalWeekList.setCheckIndex(1);
        horizontalMonthList.setCheckIndex(1);

        energyUploadDayDao = EnergyUploadDayDao.getInstance();
        energyUploadWeekDao = EnergyUploadWeekDao.getInstance();
        energyUploadMonthDao = EnergyUploadMonthDao.getInstance();

        dayEnergyList = energyUploadDayDao.selEnergyUploadDays(deviceId);
        weekEnergyList = energyUploadWeekDao.selEnergyUploadWeeks(deviceId);
        monthEnergyList = energyUploadMonthDao.selEnergyUploadMonths(deviceId);

        mDayAdapter = new EnergyDayAdapter(this, dayEnergyList);
        mWeekAdapter = new EnergyWeekAdapter(this, weekEnergyList);
        mMonthAdapter = new EnergyMonthAdapter(this, monthEnergyList);

        mDayAdapter.setOnItemClickLitener(new EnergyDayAdapter.OnItemClickLitener() {
            @Override
            public void onItemClick(View view, int position) {

                int countScreen = mDayAdapter.getCountScreen();
                int showPosition = horizontalDayList.getPosition();
                if (showPosition != -1 & position > showPosition) {
                    horizontalDayList.setCheckPosition(position + countScreen - horizontalDayList.getCheckIndex() - 1);
                } else {
                    horizontalDayList.setCheckPosition(showPosition - (showPosition - position) - horizontalDayList.getCheckIndex());
                }
            }
        });

        mWeekAdapter.setOnItemClickLitener(new EnergyWeekAdapter.OnItemClickLitener() {
            @Override
            public void onItemClick(View view, int position) {

                int countScreen = mWeekAdapter.getCountScreen();
                int showPosition = horizontalWeekList.getPosition();
                if (showPosition != -1 & position > showPosition) {
                    horizontalWeekList.setCheckPosition(position + countScreen - horizontalWeekList.getCheckIndex() - 2);
                } else {
                    horizontalWeekList.setCheckPosition(showPosition - (showPosition - position) - horizontalWeekList.getCheckIndex() - 1);
                }
            }
        });

        mMonthAdapter.setOnItemClickLitener(new EnergyMonthAdapter.OnItemClickLitener() {
            @Override
            public void onItemClick(View view, int position) {

                int countScreen = mMonthAdapter.getCountScreen();
                int showPosition = horizontalMonthList.getPosition();
                if (showPosition != -1 & position > showPosition) {
                    horizontalMonthList.setCheckPosition(position + countScreen - horizontalMonthList.getCheckIndex() - 1);
                } else {
                    horizontalMonthList.setCheckPosition(showPosition - (showPosition - position) - horizontalMonthList.getCheckIndex());
                }
            }
        });

        /**
         * 设置适配器
         */
        horizontalDayList.setAdapter(mDayAdapter);
        horizontalWeekList.setAdapter(mWeekAdapter);
        horizontalMonthList.setAdapter(mMonthAdapter);


        //默认显示“日”统计
        setSelectEffect(1);

        horizontalDayList.setOnItemScrollChangeListener(new HorizontalListView.OnItemScrollChangeListener() {
            @Override
            public void onChange(View view, int position) {
                oldDaySelectPosition = position;
                EnergyUploadDay energyUploadDay = ((EnergyDayAdapter) horizontalDayList.getAdapter()).getEnergyUploadDay(position);
                if (energyUploadDay != null) {
                    statistic_count.setText(energyUploadDay.getWorkingTime() / 60 + "h" + energyUploadDay.getWorkingTime() % 60 + "min");
                    statistic_count_average.setText(decodeFloatValue(Float.parseFloat(energyUploadDay.getEnergy())) + "kW·h");
                }

            }
        });

        horizontalWeekList.setOnItemScrollChangeListener(new HorizontalListView.OnItemScrollChangeListener() {
            @Override
            public void onChange(View view, int position) {
                oldWeekSelectPosition = position;
                EnergyUploadWeek energyUploadWeek = ((EnergyWeekAdapter) horizontalWeekList.getAdapter()).getEnergyUploadWeek(position);
                if (energyUploadWeek != null) {
                    statistic_count.setText(energyUploadWeek.getWorkingTime() / 60 + "h" + energyUploadWeek.getWorkingTime() % 60 + "min");
                    Calendar c = Calendar.getInstance();
                    int year = c.get(Calendar.YEAR);
                    int week_index = c.get(Calendar.WEEK_OF_YEAR);
                    //默认除以7天
                    int day = 7;
                    if (energyUploadWeek.getWeek() != null) {
                        if (energyUploadWeek.getWeek().contains("_")) {
                            String[] week_temp = energyUploadWeek.getWeek().split("_");
                            if (week_temp[0].equals(year + "")) {
                                if (Integer.parseInt(week_temp[1]) == week_index) {
                                    c.setFirstDayOfWeek(Calendar.MONDAY);
                                    int index = c.get(Calendar.DAY_OF_WEEK);
                                    if (index == 1) {
                                        day = 7;
                                    } else {
                                        day = index - 1;
                                    }
                                }
                            }
                        }
                    }

                    float energy = Float.parseFloat(energyUploadWeek.getEnergy());

                    if ((Float.parseFloat(energyUploadWeek.getEnergy()) / day) > 0.01) {
                        statistic_count_average.setText(decodeFloatValue(((float) Math.round((energy / day) * 100)) / 100f) + "kW·h");
                    } else {
                        statistic_count_average.setText(decodeFloatValue(((float) Math.round((energy / day) * 10000)) / 10000f) + "kW·h");
                    }

                    statistic_count2_average.setText(decodeFloatValue(energy) + "kW·h");
                }

            }
        });

        horizontalMonthList.setOnItemScrollChangeListener(new HorizontalListView.OnItemScrollChangeListener() {
            @Override
            public void onChange(View view, int position) {
                oldMonthSelectPosition = position;
                EnergyUploadMonth energyUploadMonth = ((EnergyMonthAdapter) horizontalMonthList.getAdapter()).getEnergyUploadMonth(position);
                if (energyUploadMonth != null) {
                    statistic_count.setText(energyUploadMonth.getWorkingTime() / 60 + "h" + energyUploadMonth.getWorkingTime() % 60 + "min");

                    Calendar c = Calendar.getInstance();
                    int year = c.get(Calendar.YEAR);
                    int month_index = c.get(Calendar.MONTH) + 1;
                    //默认除以30天
                    int day = 30;
                    //如果是当月，没有过完该月，则除以过了的天数
                    if (energyUploadMonth.getMonth() != null) {
                        if (energyUploadMonth.getMonth().contains("-")) {
                            String[] month_temp = energyUploadMonth.getMonth().split("-");
                            if (month_temp[0].equals((year + ""))) {
                                if (Integer.parseInt(month_temp[1]) == month_index) {
                                    day = c.get(Calendar.DAY_OF_MONTH);
                                }
                            }
                        }
                    }

                    if ((Float.parseFloat(energyUploadMonth.getEnergy()) / day) > 0.01) {
                        statistic_count_average.setText(decodeFloatValue(((float) Math.round((Float.parseFloat(energyUploadMonth.getEnergy()) / day) * 100) / 100f)) + "kW·h");
                    } else {
                        statistic_count_average.setText(decodeFloatValue(((float) Math.round((Float.parseFloat(energyUploadMonth.getEnergy()) / day) * 10000) / 10000f)) + "kW·h");
                    }

                    statistic_count2_average.setText(decodeFloatValue(Float.parseFloat(energyUploadMonth.getEnergy())) + "kW·h");
                }

            }
        });


    }


    @Override
    protected void onResume() {
        super.onResume();
        new DataLoad(false).execute();
    }


    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.day_btn:
                setSelectEffect(1);
                break;
            case R.id.week_btn:
                setSelectEffect(2);
                break;
            case R.id.month_btn:
                setSelectEffect(3);
                break;
        }
    }

    /**
     * 用来切换“日”，“周”，“月”切换
     *
     * @param flag
     */
    private void setSelectEffect(int flag) {

        selectTableFlag = flag;

        day_btn.setBackgroundResource(flag == 1 ? R.drawable.icon_day_current : R.drawable.icon_day_not_selected);
        week_btn.setBackgroundResource(flag == 2 ? R.drawable.icon_week_current : R.drawable.icon_week_not_selected);
        month_btn.setBackgroundResource(flag == 3 ? R.drawable.icon_month_current : R.drawable.icon_month_not_selected);
        horizontalDayList.setVisibility(flag == 1 ? View.VISIBLE : View.GONE);
        horizontalWeekList.setVisibility(flag == 2 ? View.VISIBLE : View.GONE);
        horizontalMonthList.setVisibility(flag == 3 ? View.VISIBLE : View.GONE);
        setSelectTextLayout(flag);
        setChartTable(flag);
    }

    /**
     * 用来切换下面的TextVie显示
     *
     * @param flag
     */
    private void setSelectTextLayout(int flag) {
        statistic_count.setText("");
        statistic_count_average.setText("");
        statistic_count2_average.setText("");
        statistic_time.setText(flag == 1 ? getString(R.string.work_time) : getString(R.string.work_total_time));

        switch (flag) {
            case 1:
                average_layout.setVisibility(View.GONE);
                statistic_level_average.setText(getString(R.string.consumption));
                statistic_level2_average.setText("");
                EnergyUploadDay energyUploadDay = ((EnergyDayAdapter) horizontalDayList.getAdapter()).getEnergyUploadDay(oldDaySelectPosition);
                if (energyUploadDay != null) {
                    statistic_count.setText(energyUploadDay.getWorkingTime() / 60 + "h" + energyUploadDay.getWorkingTime() % 60 + "min");
                    statistic_count_average.setText(decodeFloatValue(Float.parseFloat(energyUploadDay.getEnergy())) + "kW·h");
                }

                break;
            case 2:
                average_layout.setVisibility(View.VISIBLE);
                statistic_level_average.setText(getString(R.string.consumption_of_day));
                statistic_level2_average.setText(getString(R.string.total_of_week));
                EnergyUploadWeek energyUploadWeek = ((EnergyWeekAdapter) horizontalWeekList.getAdapter()).getEnergyUploadWeek(oldWeekSelectPosition);
                if (energyUploadWeek != null) {
                    statistic_count.setText(energyUploadWeek.getWorkingTime() / 60 + "h" + energyUploadWeek.getWorkingTime() % 60 + "min");

                    Calendar c = Calendar.getInstance();
                    int year = c.get(Calendar.YEAR);
                    int week_index = c.get(Calendar.WEEK_OF_YEAR);
                    //默认除以7天
                    int day = 7;
                    if (energyUploadWeek.getWeek() != null) {
                        if (energyUploadWeek.getWeek().contains("_")) {
                            String[] week_temp = energyUploadWeek.getWeek().split("_");
                            if (week_temp[0].equals(year + "")) {
                                if (Integer.parseInt(week_temp[1]) == week_index) {
                                    c.setFirstDayOfWeek(Calendar.MONDAY);
                                    int index = c.get(Calendar.DAY_OF_WEEK);
                                    if (index == 1) {
                                        day = 7;
                                    } else {
                                        day = index - 1;
                                    }
                                }
                            }
                        }
                    }
                    if ((Float.parseFloat(energyUploadWeek.getEnergy()) / day) > 0.01) {
                        statistic_count_average.setText(decodeFloatValue(((float) Math.round((Float.parseFloat(energyUploadWeek.getEnergy()) / day) * 100)) / 100f) + "kW·h");
                    } else {
                        statistic_count_average.setText(decodeFloatValue(((float) Math.round((Float.parseFloat(energyUploadWeek.getEnergy()) / day) * 10000)) / 10000f) + "kW·h");
                    }

                    statistic_count2_average.setText(decodeFloatValue(Float.parseFloat(energyUploadWeek.getEnergy())) + "kW·h");
                }
                break;
            case 3:
                average_layout.setVisibility(View.VISIBLE);
                statistic_level_average.setText(getString(R.string.consumption_of_day));
                statistic_level2_average.setText(getString(R.string.total_of_month));
                EnergyUploadMonth energyUploadMonth = ((EnergyMonthAdapter) horizontalMonthList.getAdapter()).getEnergyUploadMonth(oldMonthSelectPosition);
                if (energyUploadMonth != null) {
                    statistic_count.setText(energyUploadMonth.getWorkingTime() / 60 + "h" + energyUploadMonth.getWorkingTime() % 60 + "min");

                    Calendar c = Calendar.getInstance();
                    int month_index = c.get(Calendar.MONTH) + 1;
                    //默认除以30天
                    int day = 30;
                    //如果是当月，没有过完该月，则除以过了的天数
                    if (energyUploadMonth.getMonth() != null && energyUploadMonth.getMonth().contains("-")
                            && Integer.parseInt(energyUploadMonth.getMonth().split("-")[1]) == month_index) {
                        day = c.get(Calendar.DAY_OF_MONTH);
                    }

                    if ((Float.parseFloat(energyUploadMonth.getEnergy()) / day) > 0.01) {
                        statistic_count_average.setText(decodeFloatValue(((float) Math.round((Float.parseFloat(energyUploadMonth.getEnergy()) / day) * 100) / 100f)) + "kW·h");
                    } else {
                        statistic_count_average.setText(decodeFloatValue(((float) Math.round((Float.parseFloat(energyUploadMonth.getEnergy()) / day) * 10000) / 10000f)) + "kW·h");
                    }

                    statistic_count2_average.setText(decodeFloatValue(Float.parseFloat(energyUploadMonth.getEnergy())) + "kW·h");
                }
                break;
            default:
                break;
        }
    }

    /**
     * 用来处理图表分度值
     *
     * @param flag
     */
    private void setChartTable(int flag) {
        switch (flag) {
            case 1:
                float dayMaxScale = calcuteMaxScale(getDayMaxEnergy(dayEnergyList));
                //构造方法的字符格式这里如果小数不足2位,会以0补足.
                leve1_title.setText(decodeScale(dayMaxScale, dayMaxScale));
                leve2_title.setText(decodeScale(dayMaxScale, dayMaxScale * 2 / 3));
                leve3_title.setText(decodeScale(dayMaxScale, dayMaxScale / 3));
                break;
            case 2:
                float weekMaxScale = calcuteMaxScale(getWeekMaxEnergy(weekEnergyList));
                //构造方法的字符格式这里如果小数不足2位,会以0补足.
                leve1_title.setText(decodeScale(weekMaxScale, weekMaxScale));
                leve2_title.setText(decodeScale(weekMaxScale, weekMaxScale * 2 / 3));
                leve3_title.setText(decodeScale(weekMaxScale, weekMaxScale / 3));
                break;
            case 3:
                float monthMaxScale = calcuteMaxScale(getMonthMaxEnergy(monthEnergyList));
                //构造方法的字符格式这里如果小数不足2位,会以0补足.
                leve1_title.setText(decodeScale(monthMaxScale, monthMaxScale));
                leve2_title.setText(decodeScale(monthMaxScale, monthMaxScale * 2 / 3));
                leve3_title.setText(decodeScale(monthMaxScale, monthMaxScale / 3));
                break;
        }
    }

    /**
     * 获得“日”数据最大值
     *
     * @param baseEnergyLis
     * @return
     */
    private float getDayMaxEnergy(List<EnergyUploadDay> baseEnergyLis) {
        float max = 0f;
        if (baseEnergyLis != null) {
            for (EnergyUploadDay energyUploadDay : baseEnergyLis) {
                String energy = energyUploadDay.getEnergy();
                if (energy != null) {
                    float value = Float.parseFloat(energy);
                    if (value > max) {
                        max = value;
                    }
                }
            }
            return max;
        }
        return max;
    }

    /**
     * 用来获得周最大值
     *
     * @param baseEnergyLis
     * @return
     */
    private float getWeekMaxEnergy(List<EnergyUploadWeek> baseEnergyLis) {
        float max = 0f;
        if (baseEnergyLis != null) {
            for (EnergyUploadWeek energyUploadWeek : baseEnergyLis) {
                String energy = energyUploadWeek.getEnergy();
                if (energy != null) {
                    float value = Float.parseFloat(energy);
                    if (value > max) {
                        max = value;
                    }
                }
            }
            return max;
        }
        return max;
    }

    /**
     * 用来获取月最大值
     *
     * @param baseEnergyLis
     * @return
     */
    private float getMonthMaxEnergy(List<EnergyUploadMonth> baseEnergyLis) {
        float max = 0f;
        if (baseEnergyLis != null) {
            for (EnergyUploadMonth energyUploadMonth : baseEnergyLis) {
                String energy = energyUploadMonth.getEnergy();
                if (energy != null) {
                    float value = Float.parseFloat(energy);
                    if (value > max) {
                        max = value;
                    }
                }
            }
            return max;
        }
        return max;
    }

    // 计算出最大电量刻度
    private float calcuteMaxScale(float maxValue) {

        // 最大刻度值
        float maxPowerScale = 0;

        // 求出位数
        int max = (int) maxValue;
        int max_length = String.valueOf(max).length();

        // 比例
        double scale = 0.03;

        // 小于0
        if (maxValue < 1 && maxValue >= 0.01f) {
            scale = 0.03;
        } else if (maxValue < 0.01f) {
            scale = 0.003;
        }
        // 大于0
        else {
            for (int i = 0; i < max_length; i++) {
                scale = scale * 10;
            }
            // 如果大于 10 , 去掉小数, 然后＋1 eg: 10.99 ---> 11
            if (maxValue > 10) {
                maxValue = (int) maxValue + 1;
            }
        }
        int powerValue1_3 = (int) (maxValue / scale);

        if (powerValue1_3 * scale == maxValue) {
            maxPowerScale = maxValue;         // 如果除尽，则直接为最大刻度
        } else {
            maxPowerScale = (float) ((powerValue1_3 + 1) * scale);
        }

        return maxPowerScale;
    }

    /**
     * 处理分度值显示
     *
     * @param max
     * @param scale
     * @return
     */
    private String decodeScale(float max, float scale) {
        String result = "0";
        if (max >= 10) {
            result = ((int) scale) + "";
        } else {
            String value = ((int) (scale * 10000)) + "";
            if (value.length() == 5) {
                result = value.substring(0, 1) + "." + value.substring(1, value.length());
            } else if (value.length() == 4) {
                result = "0." + value;
            } else if (value.length() == 3) {
                result = "0.0" + value;
            } else if (value.length() == 2) {
                result = "0.00" + value;
            } else if (value.length() == 1) {
                result = "0.000" + value;
            } else {
                result = "0";
            }
        }
        return deleteEndZero(result);
    }

    /**
     * 日/周/月/日均用电量≥0.01，小数点后最多显示2位，四舍五入，
     * （如果用电量为12/1.1,显示为12/1.1,而不是12.00/1.10）
     * <0.01,  小数点后最多显示4位，四舍五入
     * （如果用电量是0.03，显示为0.03,而不是0.030）
     *
     * @return
     */
    private String decodeFloatValue(float value) {
        String result = "";
        if (value >= 0.01f) {
            String value1 = (int) (value * 100 + 0.5) + "";
            if (value1.length() == 1) {
                result = "0.0" + value1;
            } else if (value1.length() == 2) {
                result = "0." + (value1.endsWith("0") ? value1.substring(0, 1) : value1);
            } else {
                result = value1.substring(0, value1.length() - 2) + "." + value1.substring(value1.length() - 2, value1.length());

            }
        } else {
            String value1 = (int) (value * 10000 + 0.5) + "";
            if (value1.length() == 1) {
                result = "0.000" + value1;
            } else {
                result = "0.00" + value1;
            }
        }
        return deleteEndZero(result);
    }

    /**
     * 、
     * 去掉字符串末尾的“0”
     *
     * @param string
     * @return
     */
    private String deleteEndZero(String string) {
        if (!string.contains(".")) {
            return string;
        }
        if (string.endsWith("0")) {
            String result = string.substring(0, string.length() - 1);
            if (result.endsWith("0")) {
                return deleteEndZero(result);
            } else if (result.endsWith(".")) {
                return result.substring(0, result.length() - 1);
            } else {
                return result;
            }
        } else {
            return string;
        }
    }

    @Override
    protected void onRefresh(ViewEvent event) {
        super.onRefresh(event);
    }

    @Override
    public void onRightClick(View v) {

    }

    @Override
    public void leftTitleClick(View v) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_COCO_Back), null);
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        LoadTable.getInstance(mAppContext).removeListener(this);
        super.onDestroy();
    }

    @Override
    public void onLoadTableFinish(LoadTarget loadTarget, int result) {
        LogUtil.d(TAG, "onLoadTableFinish()-loadTarget:" + loadTarget + ",result:" + result);
        if (result == ErrorCode.SUCCESS && loadTarget.uid.equals(uid)) {
            freshEnergyDataToView();
        }
    }

    /**
     * 异步加载数据
     */
    private class DataLoad extends AsyncTask<Void, Void, Void> {
        public boolean isReload = false;

        public DataLoad(boolean isReload) {
            this.isReload = isReload;
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (!isReload) {
                startLoadData();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
        }
    }

    //更新数据到界面
    private void freshEnergyDataToView() {
        List<EnergyUploadDay> energyUploadDayList = energyUploadDayDao.selEnergyUploadDays(deviceId);
        List<EnergyUploadWeek> energyUploadWeekList = energyUploadWeekDao.selEnergyUploadWeeks(deviceId);
        List<EnergyUploadMonth> energyUploadMonthList = energyUploadMonthDao.selEnergyUploadMonths(deviceId);

        if (energyUploadDayList != null) {
            dayEnergyList = energyUploadDayList;
            mDayAdapter.freshData(energyUploadDayList);
        }
        if (energyUploadWeekList != null) {
            weekEnergyList = energyUploadWeekList;
            mWeekAdapter.freshData(energyUploadWeekList);
        }
        if (energyUploadMonthList != null) {
            monthEnergyList = energyUploadMonthList;
            mMonthAdapter.freshData(energyUploadMonthList);
        }

        setChartTable(selectTableFlag);
    }

    /**
     * 去服务器下载数据
     */
    private void startLoadData() {
        LoadParam loadDayParam = new LoadParam();
        LoadTarget loadDayTarget = new LoadTarget();
        loadDayTarget.target = deviceId;
        loadDayTarget.uid = uid;
        loadDayTarget.deviceId = deviceId;
        loadDayTarget.tableName = TableName.ENERGYUPLOADDAY;
        loadDayParam.loadTarget = loadDayTarget;
        LoadTable.getInstance(getApplicationContext()).load(loadDayParam);

        LoadParam loadWeekParam = new LoadParam();
        LoadTarget loadWeekTarget = new LoadTarget();
        loadWeekTarget.target = deviceId;
        loadWeekTarget.uid = uid;
        loadWeekTarget.deviceId = deviceId;
        loadWeekTarget.tableName = TableName.ENERGYUPLOADWEEK;
        loadWeekParam.loadTarget = loadWeekTarget;
        LoadTable.getInstance(getApplicationContext()).load(loadWeekParam);

        LoadParam loadMonthParam = new LoadParam();
        LoadTarget loadMonthTarget = new LoadTarget();
        loadMonthTarget.target = deviceId;
        loadMonthTarget.uid = uid;
        loadMonthTarget.deviceId = deviceId;
        loadMonthTarget.tableName = TableName.ENERGYUPLOADMONTH;
        loadMonthParam.loadTarget = loadMonthTarget;
        LoadTable.getInstance(getApplicationContext()).load(loadMonthParam);

    }
}
