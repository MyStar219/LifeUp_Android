package com.orvibo.homemate.device.smartlock;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.api.DeviceApi;
import com.orvibo.homemate.api.listener.BaseResultListener;
import com.orvibo.homemate.bo.AuthUnlockData;
import com.orvibo.homemate.bo.DoorUserData;
import com.orvibo.homemate.core.product.ProductManage;
import com.orvibo.homemate.dao.AuthUnlockDao;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.device.control.BaseControlActivity;
import com.orvibo.homemate.event.BaseEvent;
import com.orvibo.homemate.sharedPreferences.SmartLockCache;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.ActivityJumpUtil;
import com.orvibo.homemate.util.InputUtil;
import com.orvibo.homemate.util.MyLogger;
import com.orvibo.homemate.util.PhoneUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.TimeUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.DialogFragmentTwoButton;
import com.orvibo.homemate.view.custom.EditTextWithCompound;
import com.orvibo.homemate.view.popup.LockTimeSelectPopup;

import java.util.Map;

/**
 * 智能门锁设置界面
 * Created by snown on 2015/11/27.
 */
public class AuthLockActivity extends BaseControlActivity implements LockTimeSelectPopup.ITimeListener {

    private EditTextWithCompound phoneEditText;
    private LinearLayout btnSelectPhone;
    private Button btnAuth;
    private ListView recentUseList;
    private String selectPhoneNumber, selectPhoneName;
    private String uid;
    private View lockTipView;
    private TextView textTip;
    private boolean isSelectPhoneBtn = false;//是否去选择通讯录号码

    private View timeView;
    private TextView timeText;
    private boolean isBLLock;//是否是霸陵门锁
    private boolean isCn;//是否是中文
    private int time = 60;//授权有效时间，默认是1小时
    private long selectTime = 0;//选择框选择后时间记录

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isBLLock = ProductManage.isBLLock(device);
        isCn = PhoneUtil.isCN(mContext);
        if (isBLLock) {
            setContentView(R.layout.activity_auth_lock_bl);
            timeView = findViewById(R.id.timeView);
            timeText = (TextView) findViewById(R.id.time);
        } else if (isCn) {
            setContentView(R.layout.activity_auth_lock_cn);
        } else {
            setContentView(R.layout.activity_auth_lock);
        }
        initView();
    }

    private void initView() {
        uid = UserCache.getCurrentMainUid(getApplicationContext());
        recentUseList = (ListView) findViewById(R.id.recentUseList);
        textTip = (TextView) findViewById(R.id.textTip);
        lockTipView = findViewById(R.id.lockTipView);
        Map<String, String> phoneDatas = SmartLockCache.getInstance().getPhoneInfo(uid);
        if (!phoneDatas.isEmpty()) {
            lockTipView.setVisibility(View.VISIBLE);
        }
        final SmartLockPhoneAdapter adapter = new SmartLockPhoneAdapter(phoneDatas);
        recentUseList.setAdapter(adapter);

        btnAuth = (Button) findViewById(R.id.btnAuth);
        btnAuth.setOnClickListener(this);
        btnSelectPhone = (LinearLayout) findViewById(R.id.btnSelectPhone);
        btnSelectPhone.setOnClickListener(this);
        phoneEditText = (EditTextWithCompound) findViewById(R.id.phoneEditText);
        phoneEditText.setRightful(getResources().getDrawable(R.drawable.bg_list_device_default));
        if (isBLLock) {
            selectTime = System.currentTimeMillis() + 60 * 60 * 1000;
            timeText.setText(TimeUtil.getStringDate1(selectTime));
            timeView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    InputUtil.closeInput(mContext);
                    LockTimeSelectPopup lockTimeSelectPopup = new LockTimeSelectPopup(AuthLockActivity.this, selectTime);
                    lockTimeSelectPopup.setTimeListener(AuthLockActivity.this);
                    lockTimeSelectPopup.show();
                }
            });
            CharSequence str = Html.fromHtml(String.format("%s<font color=\"#fd5c48\">%s</font>", getString(R.string.lock_bl_tip), getString(R.string.lock_bl_tip3)));
            textTip.setText(str);
            initAuthView();
        } else if (isCn) {
            phoneEditText.setType(EditTextWithCompound.TYPE_PHONE);
            phoneEditText.setMaxLength(11);
            CharSequence str = Html.fromHtml(String.format("%s<font color=\"#fd5c48\">%s</font>", getString(R.string.smart_lock_authorize_tip), getString(R.string.smart_lock_authorize_tip1)));
            textTip.setText(str);
        } else {
            initAuthView();
        }
        recentUseList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectPhoneNumber = adapter.getItem(position);
                selectPhoneName = SmartLockCache.getInstance().getPhoneInfo(uid).get(selectPhoneNumber);
                phoneEditText.setText(selectPhoneNumber);
            }
        });
    }

    /**
     * 霸陵和英文环境授权界面
     */
    private void initAuthView() {
        lockTipView.setVisibility(View.GONE);
        phoneEditText.setMaxLength(16);
        phoneEditText.setMinLength(1);
        phoneEditText.setOnInputListener(new EditTextWithCompound.OnInputListener() {
            @Override
            public void onRightful() {
                btnAuth.setEnabled(true);
                btnAuth.setTextColor(getResources().getColor(R.color.green));
            }

            @Override
            public void onUnlawful() {
                btnAuth.setEnabled(false);
                btnAuth.setTextColor(getResources().getColor(R.color.gray));
            }

            @Override
            public void onClearText() {

            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnAuth:
                startAuth();
                break;
            case R.id.btnSelectPhone:
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(intent, 1);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isBLLock) {
            timeText.setText(TimeUtil.getStringDate1(selectTime));
        }
        //门锁授权时间超时和不是点击了选择通讯录号码后跳转到手势密码校验界面
        if (SmartLockCache.isTimeOut() && !isSelectPhoneBtn) {
            ActivityJumpUtil.jumpAct(AuthLockActivity.this, GestureActivity.class);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (1): {
                isSelectPhoneBtn = true;
                if (resultCode == Activity.RESULT_OK) {
                    Uri contactData = data.getData();
                    Cursor cursor = null;
                    try {
                        cursor = managedQuery(contactData, null, null, null, null);
                        if (cursor.moveToFirst()) {
                            getContactPhone(cursor);
                        }
                        if (Build.VERSION.SDK_INT < 14) {
                            cursor.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (StringUtil.isPhone(selectPhoneNumber))
                        phoneEditText.setText(selectPhoneNumber);
                    else {
                        if (!TextUtils.isEmpty(selectPhoneNumber)) {
                            selectPhoneNumber = "";
                            phoneEditText.setText("");
                            ToastUtil.showToast(R.string.user_phone_format_error1);
                        }
                    }
                }
                break;
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        isSelectPhoneBtn = false;
    }

    /**
     * 授权
     */
    private void startAuth() {
        check();
        final String phoneNumber = phoneEditText.getText().toString();
        if (TextUtils.isEmpty(phoneNumber)) {
            ToastUtil.showToast(getString(R.string.lock_input_tip));
        } else {
            //中文，英文，汇泰龙，霸陵分开验证
            if (!isCn || isBLLock) {
                if (StringUtil.isHasSpecialChar(phoneNumber)) {
                    ToastUtil.showToast(R.string.SPECIAL_CHAR_ERROR);
                    return;
                }
                if (isBLLock && selectTime < System.currentTimeMillis()) {
                    ToastUtil.showToast(getString(R.string.lock_bl_tip2));
                    return;
                }
            } else if (!StringUtil.isPhone(phoneNumber)) {
                ToastUtil.showToast(getString(R.string.user_phone_format_error1));
                return;
            }

            DialogFragmentTwoButton dialogFragmentTwoButton = new DialogFragmentTwoButton();
            dialogFragmentTwoButton.setTitle(getString(R.string.warm_tips));
            StringBuilder name = new StringBuilder();
            if (selectPhoneNumber != null && selectPhoneNumber.equalsIgnoreCase(phoneNumber)) {
                name.append(StringUtil.splitString(selectPhoneName));
            } else {
                name.append(phoneNumber);
            }
            dialogFragmentTwoButton.setContent(String.format(getString(R.string.dialog_lock_content), name.toString()));
            dialogFragmentTwoButton.setRightButtonText(getString(R.string.dialog_btn_ensure_auth));
            dialogFragmentTwoButton.setLeftButtonText(getString(R.string.cancel));
            dialogFragmentTwoButton.setOnTwoButtonClickListener(new DialogFragmentTwoButton.OnTwoButtonClickListener() {
                @Override
                public void onLeftButtonClick(View view) {

                }

                @Override
                public void onRightButtonClick(View view) {
                    showDialog();
                    DeviceApi.authUnlock(UserCache.getCurrentUserName(getApplicationContext()), UserCache.getCurrentMainUid(getApplicationContext()), deviceId, phoneNumber, time, 1, new BaseResultListener.DataListListener() {
                        @Override
                        public void onResultReturn(BaseEvent baseEvent, Object[] datas) {
                            MyLogger.jLog().d("result=" + baseEvent.getResult());
                            if (baseEvent.getResult() == ErrorCode.SUCCESS && datas != null) {
                                setName(datas, phoneNumber);
                            } else if (baseEvent.getResult() == ErrorCode.DOOR_HAS_AUTH) {
                                dismissDialog();
                                finish();
                            } else if (baseEvent.getResult() == ErrorCode.TIMEOUT_BIND) {//门锁授权超时，用的返回码是29，提示要特殊处理
                                dismissDialog();
                                String tip = getString(R.string.lock_add_tmp_pass) + getString(R.string.TIMEOUT);
                                ToastUtil.showToast(tip);
                            } else {
                                dismissDialog();
                                ToastUtil.toastError(baseEvent.getResult());
                            }
                        }
                    });
                }
            });
            dialogFragmentTwoButton.show(getFragmentManager(), "");
        }
    }

    private void setName(final Object[] datas, String phoneNumber) {
        final Intent intent = new Intent();
        final DoorUserData doorUserData = (DoorUserData) datas[1];
        intent.putExtra(IntentKey.AUTH_UNLOCK_DATA, (AuthUnlockData) datas[0]);
        intent.putExtra(IntentKey.DOOR_USER_DATA, doorUserData);
        intent.putExtra(IntentKey.DEVICE, device);
        //有通讯录选择项时才存储最近使用的手机号码
        if (btnSelectPhone.isShown()) {
            Map<String, String> phoneData = SmartLockCache.getInstance().getPhoneInfo(uid);
            phoneData = SmartLockCache.getInstance().limitData(phoneData, phoneNumber);
            if (selectPhoneNumber != null && selectPhoneNumber.equalsIgnoreCase(phoneNumber)) {
                phoneData.put(selectPhoneNumber, selectPhoneName);
            } else {
                phoneData.put(phoneNumber, phoneNumber);
            }
            SmartLockCache.getInstance().savePhoneInfo(uid, phoneData);
        }
        //通讯录中选择的用户，则把用户名直接设置为
        if (selectPhoneNumber != null && selectPhoneNumber.equalsIgnoreCase(phoneNumber)) {
            DeviceApi.authSetName(userName, currentMainUid, doorUserData.getDoorUserId(), selectPhoneName, new BaseResultListener() {
                @Override
                public void onResultReturn(BaseEvent baseEvent) {
                    dismissDialog();
                    if (baseEvent.getResult() == ErrorCode.SUCCESS) {
                        doorUserData.setName(selectPhoneName);
                    }
                    ActivityJumpUtil.jumpActFinish(AuthLockActivity.this, AuthLockResultActivity.class, intent);
                }
            });
        } else {
            dismissDialog();
            ActivityJumpUtil.jumpActFinish(AuthLockActivity.this, AuthLockResultActivity.class, intent);
        }


    }

    //获取联系人电话
    private void getContactPhone(Cursor cursor) {
        int phoneColumn = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);
        int phoneNum = cursor.getInt(phoneColumn);
        if (phoneNum > 0) {
            // 获得联系人的ID号
            int idColumn = cursor.getColumnIndex(ContactsContract.Contacts._ID);
            String contactId = cursor.getString(idColumn);
            // 获得联系人的电话号码的cursor;
            Cursor phones = getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId,
                    null, null);
            if (phones.moveToFirst()) {
                // 遍历所有的电话号码
                for (; !phones.isAfterLast(); phones.moveToNext()) {
                    String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    String phoneName = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    selectPhoneName = phoneName;
                    selectPhoneNumber = phoneNumber.replaceAll(" ", "");
                    selectPhoneNumber = selectPhoneNumber.replaceAll("-", "");
                    selectPhoneNumber = selectPhoneNumber.replaceAll("\\+86", "");
                }
                if (!phones.isClosed()) {
                    phones.close();
                }
            }
        }
    }

    private void check() {
        AuthUnlockData authUnlockData = AuthUnlockDao.getInstance().getAvailableAuth(deviceId);
        if (AuthUnlockDao.getInstance().isAvailableData(authUnlockData)) {
            finish();
        }
    }

    /**
     * 霸陵门锁选择有效期时间回调
     *
     * @param time
     */
    @Override
    public void onTimeReturn(long time) {
        if (time < System.currentTimeMillis()) {
            ToastUtil.showToast(getString(R.string.lock_bl_tip2));
        } else {
            selectTime = time;
            this.time = (int) Math.ceil((time - System.currentTimeMillis()) / (1000 * 60.0));
            timeText.setText(TimeUtil.getStringDate1(selectTime));
        }
    }

    @Override
    public void leftTitleClick(View v) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        //点击返回按钮时回到门锁记录界面
        ActivityJumpUtil.jumpActFinish(AuthLockActivity.this, LockRecordActivity.class);
        super.onBackPressed();
    }
}