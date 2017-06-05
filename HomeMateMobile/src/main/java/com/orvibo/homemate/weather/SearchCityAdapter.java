package com.orvibo.homemate.weather;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.MyLogger;
import com.orvibo.homemate.util.PhoneUtil;
import com.orvibo.homemate.util.StringUtil;

public class SearchCityAdapter extends BaseAdapter implements Filterable {

    private List<City> mAllCities;
    private List<City> mResultCities;
    private LayoutInflater mInflater;
    private Context mContext;
    private OnFilterCityListener onFilterCityListener;
    //    private long lastTime = 0;
//    private long currentTime;
    private String cityName;
    private final int FILTER_CITY_RESULT_WHAT = 0;

    // private String mFilterStr;

    public SearchCityAdapter(Context context, List<City> allCities) {
        mContext = context;
        mAllCities = allCities;
        mResultCities = new ArrayList<City>();
        mInflater = LayoutInflater.from(mContext);
    }

    Handler mIncomingHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case FILTER_CITY_RESULT_WHAT:
                    onFilterCityListener.onFilter(false, cityName);
                    break;
            }
            return true;
        }
    });

    @Override
    public int getCount() {
        return mResultCities.size();
    }

    @Override
    public City getItem(int position) {
        return mResultCities.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            //由于我们只需要将XML转化为View，并不涉及到具体的布局，所以第二个参数通常设置为null
            convertView = mInflater.inflate(R.layout.search_city_item, null);
            holder.provinceTv = (TextView) convertView.findViewById(R.id.search_province);
            holder.cityTv = (TextView) convertView.findViewById(R.id.column_title);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        MyLogger.sLog().d("mResultCities = " + mResultCities);
        if (PhoneUtil.isCN(mContext)) {
            holder.provinceTv.setText(mResultCities.get(position).getProvince());
            holder.cityTv.setText(mResultCities.get(position).getName());
        } else {
            holder.provinceTv.setText(mResultCities.get(position).getProvincepinyin());
            holder.cityTv.setText(mResultCities.get(position).getPinyin());
        }
        return convertView;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            protected void publishResults(CharSequence constraint,
                                          FilterResults results) {
                mResultCities = (ArrayList<City>) results.values;
                MyLogger.sLog().d("mResultCities = " + mResultCities);
                if (results.count > 0) {
                    notifyDataSetChanged();
                    onFilterCityListener.onFilter(true, cityName);
                    if (mIncomingHandler != null && mIncomingHandler.hasMessages(FILTER_CITY_RESULT_WHAT)) {
                        mIncomingHandler.removeMessages(FILTER_CITY_RESULT_WHAT);
                    }
                } else {
                    notifyDataSetInvalidated();
                    if (mIncomingHandler != null) {
                        if (mIncomingHandler.hasMessages(FILTER_CITY_RESULT_WHAT)) {
                            mIncomingHandler.removeMessages(FILTER_CITY_RESULT_WHAT);
                        }
                        mIncomingHandler.sendEmptyMessageDelayed(FILTER_CITY_RESULT_WHAT, 2000);
                    }
                }
            }

            protected FilterResults performFiltering(CharSequence s) {
                cityName = s.toString().toLowerCase();
                MyLogger.sLog().d("cityName = " + cityName);
                // mFilterStr = str;
                FilterResults results = new FilterResults();
                ArrayList<City> cityList = new ArrayList<City>();
                if (mAllCities != null && mAllCities.size() != 0) {
                    for (City cb : mAllCities) {
                        // 匹配全屏、首字母、和城市名中文
                        if (!StringUtil.isEmpty(cityName) && (cb.getPy().indexOf(cityName) > -1
                                || cb.getPinyin().indexOf(cityName) > -1
                                || cb.getName().indexOf(cityName) > -1
                                || cb.getProvincepinyin().indexOf(cityName) > -1
                                || cb.getProvincepy().indexOf(cityName) > -1
                                || cb.getProvince().indexOf(cityName) > -1)) {
                            cityList.add(cb);
                        }
                    }
                }
                results.values = cityList;
                results.count = cityList.size();
                return results;
            }
        };
        return filter;
    }

    // ViewHolder用于缓存控件
    class ViewHolder {
        public TextView provinceTv;
        public TextView cityTv;
    }

    public void setFilterCityListener(OnFilterCityListener onFilterCityListener) {
        this.onFilterCityListener = onFilterCityListener;
    }

    /**
     *
     */
    public interface OnFilterCityListener {
        /**
         * 是否在本地数据库匹配到了城市
         *
         * @param filtered true：匹配到；false没有匹配到
         */
        void onFilter(boolean filtered, String cityName);
    }
}
