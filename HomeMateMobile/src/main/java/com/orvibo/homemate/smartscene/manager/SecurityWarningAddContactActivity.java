package com.orvibo.homemate.smartscene.manager;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.View;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Account;
import com.orvibo.homemate.bo.Security;
import com.orvibo.homemate.bo.WarningMember;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.dao.AccountDao;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.SecurityWarningType;
import com.orvibo.homemate.model.SetSecurityWarning;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.EditTextWithCompound;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by allen on 2016/6/27.
 */
public class SecurityWarningAddContactActivity extends BaseActivity implements EditTextWithCompound.OnInputListener {
    private EditTextWithCompound contactEditText;
    private View addButton;
    private View contactImageView;
    private View tipsTextView;
    private Security security;
    private SetSecurityWarning setSecurityWarning;
    private List<WarningMember> warningMembers;
    private WarningMember warningMember;
    private Map<String, String> contactNames;
    private int maxSortNum;
    private String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_warning_add_contact);
        Intent intent = getIntent();
        Serializable serializable = intent.getSerializableExtra(IntentKey.SECURITY);
        Serializable serializableWarningMembers = intent.getSerializableExtra(IntentKey.WARNING_MEMBERS);
        maxSortNum = intent.getIntExtra(IntentKey.MAX_SORT_NUM, -1);
        if (serializable == null || maxSortNum == -1) {
            finish();
            return;
        }
        security = (Security) serializable;
        if (serializableWarningMembers != null) {
            warningMembers = (List<WarningMember>) serializableWarningMembers;
        }
        init();
    }


    private void init() {
        contactNames = new HashMap<>();
        warningMember = new WarningMember();
        warningMember.setMemberSortNum(maxSortNum + 1);
        contactEditText = (EditTextWithCompound) findViewById(R.id.contactEditText);
        contactEditText.setType(EditTextWithCompound.TYPE_PHONE);
        contactEditText.setRightfulBackgroundDrawable(null);
        contactEditText.setOnInputListener(this);
        contactImageView = findViewById(R.id.contactImageView);
        contactImageView.setOnClickListener(this);
        tipsTextView = findViewById(R.id.tipsTextView);
        Account account = new AccountDao().selAccountByUserId(UserCache.getCurrentUserId(mContext));
        if (account != null && !TextUtils.isEmpty(account.getPhone())) {
            phone = account.getPhone();
            boolean isExistPhone = false;
            if (warningMembers != null) {
                for (WarningMember warningMember : warningMembers) {
                    if (warningMember.getMemberPhone().equals(phone)) {
                        isExistPhone = true;
                        break;
                    }
                }
            }
            if (!isExistPhone) {
                contactEditText.setText(phone);
                contactEditText.setSelection(phone.length());
                tipsTextView.setVisibility(View.VISIBLE);
            } else {
                tipsTextView.setVisibility(View.GONE);
                contactEditText.setText("");
            }
        } else {
            tipsTextView.setVisibility(View.GONE);
            contactEditText.setText("");
        }
        addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(this);
        //联系人名字，跟app名字相同
        String appName = getString(R.string.app_name);
        if (!contactExist(appName)) {//联系人不存在
            String[] securityAddContact = getResources().getStringArray(R.array.security_add_contact);
            //插入系统预置联系人
            addContact(appName, securityAddContact);
        }

        initSetSecurityWarning();
    }

    private void initSetSecurityWarning() {
        setSecurityWarning = new SetSecurityWarning(mAppContext) {
            @Override
            public void onSetSecurityWarningResult(int serial, int result, String secWarningId) {
                super.onSetSecurityWarningResult(serial, result, secWarningId);
                dismissDialog();
                if (result != ErrorCode.SUCCESS) {
                    ToastUtil.toastError(result);
                } else {
                    finish();
                }
            }
        };
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.contactImageView:
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(intent, 0);
                break;
            case R.id.addButton:
                String phone = contactEditText.getText().toString();
                if (TextUtils.isEmpty(phone)) {
                    ToastUtil.showToast(getString(R.string.security_warning_contact_phone_empty));
                    return;
                }
                String contactName = contactNames.get(phone);
                if (phone.startsWith("+") && phone.length() > 3) {
                    phone = phone.substring(3, phone.length()).trim();
                }
                phone = phone.replaceAll(" ", "");
                if (StringUtil.isPhone(phone)) {//判断手机格式是否正确
                    showDialog();
                    if (TextUtils.isEmpty(contactName)) {//联系人名字为空，使用手机号作为名字
                        contactName = phone;
                    }
                    warningMember.setMemberName(contactName);
                    warningMember.setMemberPhone(phone);
                    List<WarningMember> warningMembers = new ArrayList<>();
                    warningMembers.add(warningMember);
                    setSecurityWarning.startSetSecurityWarning(userId, security.getSecurityId(), SecurityWarningType.APP_AND_PHONE, warningMembers, null, null);
                } else {
                    ToastUtil.showToast(getString(R.string.user_phone_format_error));
                }
                break;
        }
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Uri contactData = data.getData();
            Cursor cursor = managedQuery(contactData, null, null, null, null);
            cursor.moveToFirst();
            setContactPhone(cursor);
        }
    }

    /**
     * 获取联系人信息
     *
     * @param cursor
     */
    private void setContactPhone(Cursor cursor) {
//        PackageManager pm = getPackageManager();
//        boolean permission = (PackageManager.PERMISSION_GRANTED == pm.checkPermission("android.permission.READ_CONTACTS", mContext.getPackageName()));
//        if (permission) {
        try {
            int phoneColumn = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);
            int phoneNum = cursor.getInt(phoneColumn);
            if (phoneNum > 0) {
                // 获得联系人的ID号
                int idColumn = cursor.getColumnIndex(ContactsContract.Contacts._ID);
                String contactId = cursor.getString(idColumn);
                // 获得联系人电话的cursor
                Cursor phone = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + contactId, null, null);
                if (phone != null && phone.moveToFirst()) {
                    int index = phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    String num = phone.getString(index);
                    contactEditText.setText(num);
                    contactEditText.setSelection(num.length());
                    String contactName = phone.getString(phone.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    if (TextUtils.isEmpty(contactName)) {
                        contactNames.put(num, num);
                    } else {
                        contactNames.put(num, contactName);
                    }
                    phone.close();
                }
            }
        } catch (Exception e) {
            ToastUtil.showToast(R.string.security_warning_contact_no_permission);
        }
    }

    /**
     * 判断联系人是否存在
     */
    public boolean contactExist(String contactName) {
        boolean exist = false;
        try {
            Cursor cursor = this.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                int nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                while (cursor.moveToNext()) {
                    String name = cursor.getString(nameIndex);
                    if (contactName.equals(name)) {
                        exist = true;
                        break;
                    }
                }
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return exist;
    }

    /**
     * 往数据库中新增联系人
     */
    public void addContact(String name, String[] numbers) {
        try {
            //创建一个空的ContentValues
            ContentValues values = new ContentValues();
            //首先向RawContacts.CONTENT_URI执行一个空值插入，目的是获取系统返回的rawContactId
            Uri rawContactUri = getContentResolver().insert(ContactsContract.RawContacts.CONTENT_URI, values);
            long rawContactId = ContentUris.parseId(rawContactUri);
            //往data表插入姓名数据
            values.clear();
            values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
            values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);//内容类型
            values.put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, name);//设置联系人名字
            getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);//向联系人URI添加联系人名字
            for (String number : numbers) {
                //往data表插入电话数据
                values.clear();
                values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
                values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
                values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, number);
                values.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);//插入手机号码
                getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRightful() {
        setTips();
    }

    @Override
    public void onUnlawful() {
        setTips();
    }

    @Override
    public void onClearText() {
        setTips();
    }

    private void setTips() {
        if (!TextUtils.isEmpty(phone) && phone.equals(contactEditText.getText().toString())) {
            tipsTextView.setVisibility(View.VISIBLE);
        } else {
            tipsTextView.setVisibility(View.GONE);
        }
    }
}
