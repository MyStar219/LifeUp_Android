package com.orvibo.homemate.device.allone2.add;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.kookong.app.data.BrandList;
import com.smartgateway.app.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuwei on 2016/3/25.
 * 设备品牌列表适配器
 */
public class DeviceBrandListAdapter extends BaseAdapter implements SectionIndexer {

    private List<BrandList.Brand> deviceBrandList;
    private LayoutInflater inflater;
    private boolean isShowIndex;//是否展示字母

    public void setDeviceBrandList(List<BrandList.Brand> deviceBrandList) {
        if (deviceBrandList == null)
            this.deviceBrandList = new ArrayList<>();
        else
            this.deviceBrandList = deviceBrandList;

    }

    public DeviceBrandListAdapter(Context context, List<BrandList.Brand> data, boolean isShowIndex) {
        inflater = LayoutInflater.from(context);
        this.isShowIndex = isShowIndex;
        setDeviceBrandList(data);
    }

    public void updateListData(List<BrandList.Brand> deviceBrandList, boolean isShowIndex) {
        this.isShowIndex = isShowIndex;
        setDeviceBrandList(deviceBrandList);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return deviceBrandList.size();
    }

    @Override
    public Object getItem(int position) {
        //monkey测试防止越界
        return deviceBrandList.get(position < 0 ? 0 : position);
    }

    @Override
    public long getItemId(int position) {
        return deviceBrandList.get(position).brandId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.sortlistview_item, null);
            holder = new ViewHolder();
            holder.letter_name_layout = (RelativeLayout) convertView.findViewById(R.id.letter_name_layout);
            holder.tv_initial = (TextView) convertView.findViewById(R.id.catalog);
            holder.tv_cname = (TextView) convertView.findViewById(R.id.device_name_zh);
            holder.tv_ename = (TextView) convertView.findViewById(R.id.device_name_en);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        BrandList.Brand deviceBrand = deviceBrandList.get(position);
        //根据position获取分类的首字母的Char ascii值
        int section = getSectionForPosition(position);
        //如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
        if (position == getPositionForSection(section) && isShowIndex) {
            holder.letter_name_layout.setVisibility(View.VISIBLE);
            holder.tv_initial.setText(deviceBrand.initial);
        } else {
            holder.letter_name_layout.setVisibility(View.GONE);
            holder.tv_initial.setText("");
        }
        holder.tv_cname.setText(deviceBrand.cname);
        holder.tv_ename.setText(deviceBrand.ename);
        return convertView;
    }

    @Override
    public int getSectionForPosition(int position) {
        return deviceBrandList.get(position).initial.charAt(0);
    }

    @Override
    public Object[] getSections() {
        return null;
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        for (int i = 10; i < getCount(); i++) {
            String sortStr = deviceBrandList.get(i).initial;
            char firstChar = sortStr.toUpperCase().charAt(0);
            if (firstChar == sectionIndex) {
                return i;
            }
        }
        return -1;
    }

    class ViewHolder {
        RelativeLayout letter_name_layout;
        TextView tv_initial;
        TextView tv_cname;
        TextView tv_ename;
    }
}
