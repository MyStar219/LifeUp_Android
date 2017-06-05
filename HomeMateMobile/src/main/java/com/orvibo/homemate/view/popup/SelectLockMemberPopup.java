package com.orvibo.homemate.view.popup;

import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.DoorUserData;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.smartscene.adapter.LockMemberSelectAdapter;
import com.orvibo.homemate.util.PopupWindowUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 智能场景,汇泰龙和霸菱选择用户Id
 *
 * @author smagret
 * @date 2015/10/16
 */
public abstract class SelectLockMemberPopup implements AdapterView.OnItemClickListener {
    private PopupWindow             popup;
    private LockMemberSelectAdapter mLockMemberSelectAdapter;
    private List<DoorUserData>      mDoorUserDatas;
    private List<Integer> mCheckedAuthorizedIds = new ArrayList<>();

    public SelectLockMemberPopup() {

    }

    /**
     * @param context
     * @param doorUserDatas
     * @param checkedAuthorizedIds
     */
    public void showPopup(Activity context, List<DoorUserData> doorUserDatas, final List<Integer> checkedAuthorizedIds) {
        dismiss();
        View view = LayoutInflater.from(context).inflate(R.layout.popup_select_lock_member,
                null);

        TextView cancelTextView = (TextView) view.findViewById(R.id.cancelTextView);
        TextView confirmTextView = (TextView) view.findViewById(R.id.confirmTextView);

        cancelTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelClicked();
                dismiss();
            }
        });

        confirmTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmClicked(mCheckedAuthorizedIds);
                dismiss();
            }
        });

        mDoorUserDatas = doorUserDatas;
        mCheckedAuthorizedIds.clear();
        mCheckedAuthorizedIds.addAll(checkedAuthorizedIds);

        mLockMemberSelectAdapter = new LockMemberSelectAdapter(context, mDoorUserDatas, mCheckedAuthorizedIds);
        ListView lockMemberListView = (ListView) view.findViewById(R.id.lockMemberListView);
        lockMemberListView.setOnItemClickListener(this);
        lockMemberListView.setAdapter(mLockMemberSelectAdapter);

        popup = new PopupWindow(view, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        PopupWindowUtil.initPopup(popup,
                context.getResources().getDrawable(R.drawable.gray_ligth), 1);
        popup.showAtLocation(view, Gravity.CENTER, 0, 0);

    }

    public void dismiss() {
        PopupWindowUtil.disPopup(popup);
    }

    public boolean isShowing() {
        if (popup != null && popup.isShowing()) {
            return true;
        }
        return false;
    }


    private boolean isContainLockMember(DoorUserData selectedDoorUserData) {
        boolean selected = false;
        if (mCheckedAuthorizedIds != null && !mCheckedAuthorizedIds.isEmpty() && selectedDoorUserData != null) {
            int selectedAuthorizedId = selectedDoorUserData.getAuthorizedId();
            for (Integer authorizedId : mCheckedAuthorizedIds) {
                if (selectedAuthorizedId == authorizedId) {
                    selected = true;
                    break;
                }
            }
        }
        return selected;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        DoorUserData doorUserData = (DoorUserData) view.getTag(R.id.tag_doorUserData);

        CheckBox checkBox = (CheckBox) view.findViewById(R.id.memberCheckBox);

        checkBox.performClick();
        if (checkBox.isChecked()) {
            if (!isContainLockMember(doorUserData)) {
                mCheckedAuthorizedIds.add(doorUserData.getAuthorizedId());
            }
        } else {
            if (isContainLockMember(doorUserData)) {
                if (mCheckedAuthorizedIds != null && !mCheckedAuthorizedIds.isEmpty()) {
                    int pos = Constant.INVALID_NUM;
                    final int size = mCheckedAuthorizedIds.size();
                    final int authorizedId = doorUserData.getAuthorizedId();
                    for (int i = 0; i < size; i++) {
                        if (authorizedId == mCheckedAuthorizedIds.get(i)) {
                            pos = i;
                            break;
                        }
                    }
                    if (pos >= 0) {
                        mCheckedAuthorizedIds.remove(pos);
                    }
                }
                mCheckedAuthorizedIds.remove(doorUserData);
            }
        }
        mLockMemberSelectAdapter.setCheckedLockMembers(mCheckedAuthorizedIds);
        mLockMemberSelectAdapter.notifyDataSetChanged();
    }

    public abstract void confirmClicked(List<Integer> checkedAuthorizedIds);
    public abstract void cancelClicked();
}
