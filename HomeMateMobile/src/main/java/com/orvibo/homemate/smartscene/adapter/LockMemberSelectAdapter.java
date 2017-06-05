
package com.orvibo.homemate.smartscene.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.DoorUserData;
import com.orvibo.homemate.dao.DoorUserDao;
import com.orvibo.homemate.util.LogUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 智能场景 联动选择门锁的用户Id
 * @author Smagret
 * @date 2016/04/26
 */
public class LockMemberSelectAdapter extends BaseAdapter {
    private static final String TAG = LockMemberSelectAdapter.class.getSimpleName();
    private LayoutInflater mInflater;
    private final Resources mRes;
    private final int mFontNormalColor;
    private final int mFontOfflineColor;

    private Set<String> mCheckedDoorUserDatas = new HashSet<String>();
    private List<DoorUserData> mDoorUserDatas;


    /**
     * @param context
     * @param doorUserDatas
     * @param checkedAuthorizedIds
     */
    public LockMemberSelectAdapter(Context context, List<DoorUserData> doorUserDatas, List<Integer> checkedAuthorizedIds) {
        mInflater = LayoutInflater.from(context);
        mDoorUserDatas = doorUserDatas;
        setCheckedLockMembers(checkedAuthorizedIds);
        mRes = context.getResources();
        mFontNormalColor = mRes.getColor(R.color.font_black);
        mFontOfflineColor = mRes.getColor(R.color.font_offline);
    }

    public void setCheckedLockMembers(List<Integer> checkedAuthorizedIds) {
        if (checkedAuthorizedIds == null || checkedAuthorizedIds.isEmpty()) {
            mCheckedDoorUserDatas.clear();
        } else {
            mCheckedDoorUserDatas.clear();
            for (Integer authorizedId : checkedAuthorizedIds) {
                final String key = getKey(authorizedId);
                mCheckedDoorUserDatas.add(key);
            }
        }
    }


    private String getKey(int authorizedId) {
        return "_" + authorizedId;
    }

    // 获取数据量
    @Override
    public int getCount() {
        return mDoorUserDatas.size();
    }

    // 获取对应ID项对应的Item
    @Override
    public DoorUserData getItem(int position) {
        return mDoorUserDatas.get(position);
    }

    // 获取对应项ID
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DoorUserData doorUserData = mDoorUserDatas.get(position);
        //  LogUtil.e(TAG, "getView()-mDoorUserDatas:" + mDoorUserDatas);
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.lock_member_item_check,
                    parent, false);
            holder.memberNameTextView = (TextView) convertView
                    .findViewById(R.id.memberNameTextView);
            holder.memberCheckBox = (CheckBox) convertView
                    .findViewById(R.id.memberCheckBox);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (TextUtils.isEmpty(doorUserData.getName())) {
            holder.memberNameTextView.setText(parent.getContext().getString(R.string.linkage_action_no) + "(" + parent.getContext().getString(R.string.lock_number) + doorUserData.getAuthorizedId() + ")");
        } else if (doorUserData.getType() == DoorUserDao.TYPE_TMP_USER) {
            holder.memberNameTextView.setText(doorUserData.getName());
        } else
            holder.memberNameTextView.setText(doorUserData.getName() + "(" + parent.getContext().getString(R.string.lock_number) + doorUserData.getAuthorizedId() + ")");

        final String checkedKey = getKey(doorUserData.getAuthorizedId());
        LogUtil.d(TAG, "getView()-checkedKey:" + checkedKey + ",mCheckedDoorUserDatas:" + mCheckedDoorUserDatas);
        if (mCheckedDoorUserDatas.contains(checkedKey)) {
            holder.memberCheckBox.setChecked(true);
        } else {
            holder.memberCheckBox.setChecked(false);
        }
        holder.memberCheckBox.setTag(R.id.tag_doorUserData, doorUserData);

        convertView.setTag(R.id.tag_doorUserData, doorUserData);
        return convertView;
    }

    private class ViewHolder {
        private TextView memberNameTextView;
        private CheckBox memberCheckBox;
    }
}

