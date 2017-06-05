package com.orvibo.homemate.smartscene.manager;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.View;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Security;
import com.orvibo.homemate.bo.WarningMember;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.SecurityWarningType;
import com.orvibo.homemate.model.SetSecurityWarning;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.EditTextWithCompound;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by allen on 2016/6/27.
 */
public class SecurityWarningEditContactActivity extends BaseActivity {
    private EditTextWithCompound nameEditText, contactEditText;
    private View contactImageView;
    private View saveButton, deleteButton;
    private Security security;
    private SetSecurityWarning setSecurityWarning;
    private List<WarningMember> warningMembers;
    private WarningMember warningMember;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_warning_edit_contact);
        Intent intent = getIntent();
        Serializable serializableSecurity = intent.getSerializableExtra(IntentKey.SECURITY);
        Serializable serializableWarningMember = intent.getSerializableExtra(IntentKey.WARNING_MEMBER);
        Serializable serializableWarningMembers = intent.getSerializableExtra(IntentKey.WARNING_MEMBERS);
        if (serializableSecurity == null || serializableWarningMember == null || serializableWarningMembers == null) {
            finish();
            return;
        }
        security = (Security) serializableSecurity;
        warningMember = (WarningMember) serializableWarningMember;
        warningMembers = (List<WarningMember>) serializableWarningMembers;
        init();
    }


    private void init() {
        nameEditText = (EditTextWithCompound) findViewById(R.id.nameEditText);
        nameEditText.setRightfulBackgroundDrawable(getResources().getDrawable(R.color.white));
        nameEditText.setText(warningMember.getMemberName());
        nameEditText.setMaxLength(EditTextWithCompound.MAX_TEXT_LENGTH);
        contactEditText = (EditTextWithCompound) findViewById(R.id.contactEditText);
        contactEditText.setRightfulBackgroundDrawable(null);
        contactEditText.setType(EditTextWithCompound.TYPE_PHONE);
        contactEditText.setText(warningMember.getMemberPhone());
        contactImageView = findViewById(R.id.contactImageView);
        contactImageView.setOnClickListener(this);
        saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(this);
        deleteButton = findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(this);

        initSetSecurityWarning();
    }

    private void initSetSecurityWarning() {
        setSecurityWarning = new SetSecurityWarning(mAppContext){
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
        switch(v.getId()) {
            case R.id.contactImageView:
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(intent, 0);
                break;
            case R.id.saveButton:
                String phone = contactEditText.getText().toString();
                if (TextUtils.isEmpty(phone)) {
                    ToastUtil.showToast(getString(R.string.security_warning_contact_phone_empty));
                    return;
                }
                String contactName = nameEditText.getText().toString();
                if (TextUtils.isEmpty(contactName)) {
                    ToastUtil.showToast(getString(R.string.security_warning_contact_name_empty));
                    return;
                }
                if (phone.startsWith("+")&&phone.length()>3) {
                    phone = phone.substring(3, phone.length()).trim();
                }
                phone = phone.replaceAll(" ", "");
                if (StringUtil.isPhone(phone)) {//判断手机格式是否正确
                    showDialog();
                    warningMember.setMemberName(contactName);
                    warningMember.setMemberPhone(phone);
                    List<WarningMember> warningMembers = new ArrayList<>();
                    warningMembers.add(warningMember);
                    setSecurityWarning.startSetSecurityWarning(userId, security.getSecurityId(), SecurityWarningType.APP_AND_PHONE, null, warningMembers, null);
                } else {
                    ToastUtil.showToast(getString(R.string.user_phone_format_error));
                }
                break;
            case R.id.deleteButton:
                List<WarningMember> warningMembers = new ArrayList<>();
                warningMembers.add(warningMember);
                int index = warningMember.getMemberSortNum();
                boolean hasChange = false;
                for (WarningMember wm: this.warningMembers) {
                    if (wm.getWarningMemberId().equals(warningMember.getWarningMemberId())) {
                        this.warningMembers.remove(wm);
                        break;
                    }
                }
                for (WarningMember wm: this.warningMembers) {
                    int sortNum = wm.getMemberSortNum();
                    if (sortNum > index) {
                        wm.setMemberSortNum(sortNum - 1);
                        hasChange = true;
                    }
                }
                setSecurityWarning.startSetSecurityWarning(userId, security.getSecurityId(), SecurityWarningType.APP_AND_PHONE, null, hasChange?this.warningMembers:null, warningMembers);
                break;
        }
    }


    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
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
     * @param cursor
     */
    private void setContactPhone(Cursor cursor) {
        try{
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
                    if (TextUtils.isEmpty(nameEditText.getText().toString())) {//姓名为空时，点击通讯录联系人时，填写号码并自动将通讯录名称写入姓名中，姓名不为空时，点击通讯录联系人时，只填号码，不填姓名。
                        String contactName = phone.getString(phone.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        if (TextUtils.isEmpty(contactName)) {
                            nameEditText.setText(num);
                        } else {
                            nameEditText.setText(contactName);
                        }
                    }
                    phone.close();
                }
            }
        } catch (Exception e){
            ToastUtil.showToast(R.string.security_warning_contact_no_permission);
        }
    }
}
