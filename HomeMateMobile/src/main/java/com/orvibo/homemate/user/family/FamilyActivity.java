package com.orvibo.homemate.user.family;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Account;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.dao.AccountDao;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.ErrorMessage;
import com.orvibo.homemate.model.DeleteFamilyMember;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.NetUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.DialogFragmentTwoButton;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Allen on 2015/9/8.
 */
public class FamilyActivity extends BaseActivity implements DialogFragmentTwoButton.OnTwoButtonClickListener{
    private static final String TAG = FamilyActivity.class.getSimpleName();
    private ListView memberListView;
    private TextView addTextView;
    private FamilyAdapter familyAdapter;
    private List<Account> accounts;
    private DeleteFamilyMember deleteFamilyMember;
    private String userName;
    private AccountDao accountDao;
    private boolean hasNewIntent = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family);
        findViews();
        init();
    }

    private void findViews() {
        memberListView = (ListView) findViewById(R.id.memberListView);
        addTextView = (TextView) findViewById(R.id.addTextView);
    }

    private void init() {
        addTextView.setOnClickListener(this);
        accountDao = new AccountDao();

        initDeleteFamilyMember();
    }

    private void initDeleteFamilyMember(){
        deleteFamilyMember = new DeleteFamilyMember(mAppContext) {
            @Override
            public void onDeleteFamilyMemberResult(int serial, int result) {
                dismissDialog();
                if (result == ErrorCode.SUCCESS) {
                    accounts = accountDao.selFamily(UserCache.getCurrentUserId(mAppContext));
                    familyAdapter.notifyDataSetChanged();
                    if (accounts.isEmpty()) {
                        finish();
                    }
                } else {
                    ToastUtil.showToast(ErrorMessage.getError(mAppContext, result));
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (!hasNewIntent) {
        accounts = accountDao.selFamily(UserCache.getCurrentUserId(mAppContext));
        if (familyAdapter == null) {
            familyAdapter = new FamilyAdapter();
            memberListView.setAdapter(familyAdapter);
        } else {
            familyAdapter.notifyDataSetChanged();
        }
//            if (accounts.isEmpty()) {
//                finish();
//            }
//        }
//        hasNewIntent = false;
        newIntent(getIntent());
        LogUtil.d(TAG, "onResume accounts:" + accounts);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        hasNewIntent = true;
        newIntent(intent);
        LogUtil.d(TAG, "onNewIntent accounts:" + accounts);
    }

    private void newIntent(Intent intent) {
        Serializable serializable = intent.getSerializableExtra(Constant.ACCOUNT);
        if (serializable != null) {
            Account account = (Account) serializable;
            String phone = account.getPhone();
            boolean contains = false;
            for (Account account1: accounts) {
                if (phone.equals(account1.getPhone()) || phone.equals(account1.getEmail())){
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                accounts.add(account);
                familyAdapter.notifyDataSetChanged();
            }
            if (accounts.isEmpty()) {
                finish();
            }
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.deleteImageView:{
                // 判断网络是否连接
                if (!NetUtil.isNetworkEnable(mAppContext)) {
                    ToastUtil.showToast( R.string.network_canot_work, Toast.LENGTH_SHORT);
                    return;
                }
                userName = (String) v.getTag();
                DialogFragmentTwoButton dialogFragmentTwoButton = new DialogFragmentTwoButton();
                dialogFragmentTwoButton.setTitle(getString(R.string.family_delete_title));
//                dialogFragmentTwoButton.setContent(getString(R.string.family_delete_content));
                dialogFragmentTwoButton.setLeftButtonText(getString(R.string.delete));
                dialogFragmentTwoButton.setLeftTextColor(getResources().getColor(R.color.red));
                dialogFragmentTwoButton.setRightButtonText(getString(R.string.cancel));
                dialogFragmentTwoButton.setOnTwoButtonClickListener(this);
                dialogFragmentTwoButton.show(getFragmentManager(),"");
                break;
            }
            case R.id.addTextView:{
                Intent intent = new Intent(this, FamilyInviteActivity.class);
                startActivity(intent);
                break;
            }
        }
    }

    @Override
    public void onLeftButtonClick(View view) {
//        StatService.trackCustomKVEvent(mContext, getString(R.string.MTAClick_SettingsCOCO_ConfirmDelete), null);
        showDialog();
        deleteFamilyMember.startDelete(userName);
    }

    @Override
    public void onRightButtonClick(View view) {
//        StatService.trackCustomKVEvent(mContext, getString(R.string.MTAClick_SettingsCOCO_CancelDelete), null);
    }

    private class FamilyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return accounts.size();
        }

        @Override
        public Object getItem(int position) {
            return accounts.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            FamilyHolder familyHolder;
            if (convertView == null) {
                convertView = View.inflate(parent.getContext(), R.layout.family_item, null);
                familyHolder = new FamilyHolder();
                familyHolder.nameTextView = (TextView) convertView.findViewById(R.id.nameTextView);
                familyHolder.deleteImageView = (ImageView) convertView.findViewById(R.id.deleteImageView);
                convertView.setTag(familyHolder);
            } else {
                familyHolder = (FamilyHolder) convertView.getTag();
            }
            Account account = accounts.get(position);
            String phone = account.getPhone();
            String email = account.getEmail();
            if (!TextUtils.isEmpty(phone)){
                familyHolder.nameTextView.setText(phone);
                familyHolder.deleteImageView.setTag(phone);
            } else if (!TextUtils.isEmpty(email)){
                familyHolder.nameTextView.setText(email);
                familyHolder.deleteImageView.setTag(email);
            }
            familyHolder.deleteImageView.setOnClickListener(FamilyActivity.this);
            return convertView;
        }
    }

    private class FamilyHolder {
        TextView nameTextView;
        ImageView deleteImageView;
    }
}
