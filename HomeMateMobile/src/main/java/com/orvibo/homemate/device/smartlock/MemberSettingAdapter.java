package com.orvibo.homemate.device.smartlock;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.DoorUserData;
import com.orvibo.homemate.dao.DoorUserDao;

import java.util.List;

/**
 * 门锁成员设置
 * Created by snown on 2015/11/27
 */
public class MemberSettingAdapter extends BaseAdapter {

    private List<DoorUserData> doorUserDatas;
    private int[] icons = {R.drawable.record_pic_fingerprint, R.drawable.record_pic_password, R.drawable.record_pic_card, R.drawable.record_pic_password, R.drawable.record_pic_gate_lock};

    public MemberSettingAdapter(List<DoorUserData> doorUserData) {
        this.doorUserDatas = doorUserData;
    }


    // 获取数据量
    @Override
    public int getCount() {
        return doorUserDatas.size();
    }

    // 获取对应ID项对应的Item
    @Override
    public DoorUserData getItem(int position) {
        return doorUserDatas.get(position);
    }

    // 获取对应项ID
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        DoorUserData doorUserData = getItem(position);
        if (convertView == null || ((ViewHolder) convertView.getTag()).flag != position) {
            holder = new ViewHolder();
            holder.flag = position;
            convertView = View.inflate(parent.getContext(), R.layout.item_lock_member_set, null);
            holder.typeImage = (ViewGroup) convertView.findViewById(R.id.typeImage);
            holder.memberName = (TextView) convertView.findViewById(R.id.memberName);
            for (int type : doorUserData.getTypes()) {
                ImageView imageView = new ImageView(parent.getContext());
                imageView.setPadding(10, 0, 0, 0);
                imageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                if (type <= icons.length && type > 0)
                    imageView.setImageResource(icons[type - 1]);
                else
                    imageView.setImageResource(icons[0]);
                holder.typeImage.addView(imageView);
            }
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (TextUtils.isEmpty(doorUserData.getName())) {
            holder.memberName.setText(parent.getContext().getString(R.string.linkage_action_no) + "(" + parent.getContext().getString(R.string.lock_number) + doorUserData.getAuthorizedId() + ")");
        } else if (doorUserData.getType() == DoorUserDao.TYPE_TMP_USER) {
            holder.memberName.setText(doorUserData.getName());
        } else
            holder.memberName.setText(doorUserData.getName() + "(" + parent.getContext().getString(R.string.lock_number) + doorUserData.getAuthorizedId() + ")");
        return convertView;
    }

    // ViewHolder用于缓存控件
    static class ViewHolder {
        public ViewGroup typeImage;
        public TextView memberName;
        public int flag;//防止自定义添加的图片滑动错位
    }


}
