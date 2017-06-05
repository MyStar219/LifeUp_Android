package com.orvibo.homemate.smartscene.manager;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobeta.android.dslv.DragSortListView;
import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Security;
import com.orvibo.homemate.bo.SecurityWarning;
import com.orvibo.homemate.bo.WarningMember;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.dao.WarningMemberDao;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.model.SetSecurityWarning;
import com.orvibo.homemate.util.ToastUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by allen on 2016/6/27.
 */
public class SecurityWarningContactActivity extends BaseActivity implements DragSortListView.DropListener, AdapterView.OnItemClickListener{
    private View emptyView, contactView;
    private View addContact, addMore;
    private DragSortListView contactListView;
    private Security security;
    private SecurityWarning securityWarning;
    private WarningMemberDao warningMemberDao;
    private List<WarningMember> warningMembers;
    private WarningContactAdapter adapter;
    private SetSecurityWarning setSecurityWarning;
    private int maxSortNum;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Serializable serializableSecurityWarning = intent.getSerializableExtra(IntentKey.SECURITY_WARNING);
        Serializable serializableSecurity = intent.getSerializableExtra(IntentKey.SECURITY);
        if (serializableSecurityWarning == null || serializableSecurity == null) {
            finish();
            return;
        }
        securityWarning = (SecurityWarning) serializableSecurityWarning;
        security = (Security) serializableSecurity;
        setContentView(R.layout.activity_security_warning_contact);
        init();
        initSetSecurityWarning();
    }

    private void init() {
        emptyView = findViewById(R.id.emptyView);
        contactView = findViewById(R.id.contactView);
        addContact = findViewById(R.id.addContact);
        addContact.setOnClickListener(this);
        addMore = findViewById(R.id.addMore);
        addMore.setOnClickListener(this);
        contactListView = (DragSortListView) findViewById(R.id.contactListView);
        contactListView.setDropListener(this);
        contactListView.setOnItemClickListener(this);
        warningMemberDao = new WarningMemberDao();
    }

    @Override
    protected void onResume() {
        super.onResume();
        maxSortNum = 0;
        warningMembers = warningMemberDao.selWarningMembers(securityWarning.getSecWarningId());
        if (warningMembers.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            contactView.setVisibility(View.GONE);
            notifyChanged();
        } else {
            sortMembers();
            emptyView.setVisibility(View.GONE);
            contactView.setVisibility(View.VISIBLE);
            if (warningMembers.size()>=5) {
                addMore.setVisibility(View.GONE);
            } else {
                addMore.setVisibility(View.VISIBLE);
            }
        }
    }

    private void notifyChanged() {
        if (adapter == null) {
            adapter = new WarningContactAdapter();
            contactListView.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    private void initSetSecurityWarning() {
        setSecurityWarning = new SetSecurityWarning(mAppContext){
            @Override
            public void onSetSecurityWarningResult(int serial, int result, String secWarningId) {
                super.onSetSecurityWarningResult(serial, result, secWarningId);
                if (result != ErrorCode.SUCCESS) {
                    ToastUtil.toastError(result);
                    onResume();
                }
            }
        };
    }

    /**
     * 插入排序，按照memberSortNum大小顺序排序
     */
    private void sortMembers() {
        List<WarningMember> temps = new ArrayList<>();
        temps.add(warningMembers.get(0));
        maxSortNum = Math.max(maxSortNum, warningMembers.get(0).getMemberSortNum());
        for (int i = 1; i < warningMembers.size(); i++) {
            WarningMember warningMember = warningMembers.get(i);
            int sortNum = warningMember.getMemberSortNum();
            maxSortNum = Math.max(maxSortNum, sortNum);
            for (int j = 0; j < temps.size(); j++) {
                WarningMember temp = temps.get(j);
                if (temp.getMemberSortNum() > sortNum) {
                    temps.add(j, warningMember);
                    break;
                } else if(j == temps.size() -1) {
                    temps.add(warningMember);
                    break;
                }
            }
        }
        warningMembers.clear();
        warningMembers.addAll(temps);
        notifyChanged();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.addContact:
            case R.id.addMore:
                Intent intent = new Intent(this, SecurityWarningAddContactActivity.class);
                intent.putExtra(IntentKey.MAX_SORT_NUM, maxSortNum);
                intent.putExtra(IntentKey.WARNING_MEMBERS, (Serializable) warningMembers);
                intent.putExtra(IntentKey.SECURITY, security);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(this, SecurityWarningEditContactActivity.class);
        intent.putExtra(IntentKey.SECURITY, security);
        intent.putExtra(IntentKey.WARNING_MEMBER, warningMembers.get(position));
        intent.putExtra(IntentKey.WARNING_MEMBERS, (Serializable) warningMembers);
        startActivity(intent);
    }

    @Override
    public void drop(int from, int to) {
        if (from == to) {
            return;
        }
        WarningMember fromWarningMember = warningMembers.get(from);
        WarningMember toWarningMember = warningMembers.get(to);
        int toSortNum = toWarningMember.getMemberSortNum();
        fromWarningMember.setMemberSortNum(toSortNum);
        if (from > to) {
            for (int i = to; i < from; i++) {
                WarningMember warningMember = warningMembers.get(i);
                int sortNum = warningMember.getMemberSortNum();
                warningMember.setMemberSortNum(sortNum+1);
            }
        } else {
            for (int i = from+1; i < to+1; i++) {
                WarningMember warningMember = warningMembers.get(i);
                int sortNum = warningMember.getMemberSortNum();
                warningMember.setMemberSortNum(sortNum-1);
            }
        }
        sortMembers();
        setSecurityWarning.startSetSecurityWarning(userId, security.getSecurityId(), securityWarning.getWarningType(), null, warningMembers, null);
    }

    private class WarningContactAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return warningMembers.size();
        }

        @Override
        public Object getItem(int position) {
            return warningMembers.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            WarningContactHolder familyHolder;
            if (convertView == null) {
                convertView = View.inflate(parent.getContext(), R.layout.security_warning_contact_item, null);
                familyHolder = new WarningContactHolder();
                familyHolder.nameTextView = (TextView) convertView.findViewById(R.id.nameTextView);
                familyHolder.menuImageView = (ImageView) convertView.findViewById(R.id.drag_handle);
                convertView.setTag(familyHolder);
            } else {
                familyHolder = (WarningContactHolder) convertView.getTag();
            }
            WarningMember warningMember = warningMembers.get(position);
            String name = warningMember.getMemberName();
            String phone = warningMember.getMemberPhone();
            if (!TextUtils.isEmpty(name)){
                familyHolder.nameTextView.setText(name);
            } else if (!TextUtils.isEmpty(phone)){
                familyHolder.nameTextView.setText(phone);
            }
            return convertView;
        }
    }

    private class WarningContactHolder {
        TextView nameTextView;
        ImageView menuImageView;
    }
}
