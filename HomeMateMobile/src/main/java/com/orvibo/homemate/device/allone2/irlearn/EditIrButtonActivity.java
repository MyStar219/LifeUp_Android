package com.orvibo.homemate.device.allone2.irlearn;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.smartgateway.app.R;
import com.orvibo.homemate.api.IrApi;
import com.orvibo.homemate.api.listener.BaseResultListener;
import com.orvibo.homemate.bo.KKIr;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.ErrorMessage;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.ToastType;
import com.orvibo.homemate.device.control.BaseControlActivity;
import com.orvibo.homemate.event.BaseEvent;
import com.orvibo.homemate.event.PartViewEvent;
import com.orvibo.homemate.util.ClickUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.EditTextWithCompound;
import com.orvibo.homemate.view.custom.NavigationCocoBar;

import de.greenrobot.event.EventBus;

/**
 * Created by yuwei on 2016/4/15.
 * 添加按键页面，也是编辑按键页面
 */
public class EditIrButtonActivity extends BaseControlActivity implements NavigationCocoBar.OnLeftClickListener, NavigationCocoBar.OnRightClickListener {


    public static final String OPERATION_TYPE = "operation_type";
    public static final int OPERATION_TYPE_ADD = 0;//添加按键状态
    public static final int OPETATION_TYPE_EDIT = 1;//编辑按键状态

    //操作类型（0：添加按键；1：编辑按键）
    private int operation_type;

    //需要传递的KKIr对象
    private KKIr kkIr;

    private NavigationCocoBar nbTitle;
    private EditTextWithCompound et_irButtonName;
    private Button nextButton;
    private int rid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_ir_btn);

        initView();
        initData();
        initListener();
    }

    private void initView() {
        nbTitle = (NavigationCocoBar) findViewById(R.id.navigationBar);
        et_irButtonName = (EditTextWithCompound) findViewById(R.id.userNicknameEditText);
        et_irButtonName.setRightful(getResources().getDrawable(R.drawable.bg_list_device_default));
        et_irButtonName.setNeedRestrict(false);
        et_irButtonName.setMaxLength(8);
        nextButton = (Button) findViewById(R.id.saveButton);
        et_irButtonName.setHint(R.string.ir_key_name_empty);
    }

    private void initData() {
        operation_type = getIntent().getIntExtra(OPERATION_TYPE, -1);
        kkIr = (KKIr) getIntent().getSerializableExtra(IntentKey.ALL_ONE_IR_KEY);
        rid = getIntent().getIntExtra(IntentKey.ALLONE_LEARN_RID, 0);
        switch (operation_type) {
            case OPERATION_TYPE_ADD:
                nbTitle.setCenterText(getString(R.string.add_irkeybutton));
                nextButton.setText(R.string.next);
                nextButton.setTextColor(getResources().getColor(R.color.green));
                break;
            case OPETATION_TYPE_EDIT:
                nbTitle.setCenterText(getString(R.string.edit_irkeybutton));
                nbTitle.setRightText(getString(R.string.save));
                nbTitle.setOnRightClickListener(this);
                nextButton.setText(R.string.delete);
                nextButton.setTextColor(getResources().getColor(R.color.red));
                et_irButtonName.setText(kkIr.getfName().toString());
                break;
        }
    }

    private void initListener() {
        nbTitle.setOnRightClickListener(this);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (operation_type == OPERATION_TYPE_ADD)
                    createIrButton();//添加按钮
                else
                    deleteIrButton();//删除按钮
            }
        });
    }

    /**
     * 添加按键操作
     */
    private void createIrButton() {
        if (ClickUtil.isFastDoubleClick(1000))
            return;
        String addButtonName = et_irButtonName.getText().toString().trim();
        if (!stateOperation(addButtonName))
            return;
        kkIr = new KKIr();
        kkIr.setFid((int) (System.currentTimeMillis() / 1000));
        kkIr.setfKey(addButtonName);
        kkIr.setfName(addButtonName);
        showDialog();
        IrApi.createIrKey(userName, uid, deviceId, kkIr.getfName(), 0, null, rid, kkIr.getFid(), new BaseResultListener.DataListListener() {

            @Override
            public void onResultReturn(BaseEvent baseEvent, Object[] datas) {
                dismissDialog();
                if (baseEvent.getResult() == ErrorCode.SUCCESS) {
                    EventBus.getDefault().post(new PartViewEvent());
                    Intent intent = new Intent(EditIrButtonActivity.this, IrLearnActivity.class);
                    intent.putExtra(IntentKey.DEVICE, device);
                    intent.putExtra(IntentKey.ALL_ONE_IR_KEY, kkIr);
                    startActivity(intent);
                    finish();
                } else {
                    ToastUtil.toastError(baseEvent.getResult());
                }
            }
        });

    }

    /**
     * 删除按键操作
     */
    private void deleteIrButton() {
        showDialog();
        IrApi.deleteIrKey(userName, uid, null, kkIr.getKkIrId(), 0, new BaseResultListener() {
            @Override
            public void onResultReturn(BaseEvent baseEvent) {
                dismissDialog();
                if (baseEvent.getResult() == ErrorCode.SUCCESS) {
                    EventBus.getDefault().post(new PartViewEvent());
                    finish();
                } else {
                    ToastUtil.showToast(ErrorMessage.getError(mContext, baseEvent.getResult()), ToastType.ERROR, ToastType.SHORT);
                }
            }
        });
    }

    /**
     * 编辑按键操作
     */
    private void editIrButton() {
        String addButtonName = et_irButtonName.getText().toString().trim();
        if (!stateOperation(addButtonName) || kkIr == null)
            return;
        showDialog();
        IrApi.modifyIrKey(userName, uid, null, kkIr.getKkIrId(), addButtonName, new BaseResultListener() {

            @Override
            public void onResultReturn(BaseEvent baseEvent) {
                dismissDialog();
                if (baseEvent.getResult() == ErrorCode.SUCCESS) {
                    EventBus.getDefault().post(new PartViewEvent());
                    finish();
                } else {
                    ToastUtil.showToast(ErrorMessage.getError(mContext, baseEvent.getResult()), ToastType.ERROR, ToastType.SHORT);
                }
            }
        });
/*        Intent intent = new Intent(EditIrButtonActivity.this, IrLearnActivity.class);
        intent.putExtra(IntentKey.DEVICE, device);
        intent.putExtra(IntentKey.ALL_ONE_IR_KEY, kkIr);
        intent.putExtra(IntentKey.IS_START_LEARN, irButtonIsLearn);
        startActivity(intent);
        finish();*/
    }

    private boolean stateOperation(String buttonName) {
        if (operation_type == -1) {
            ToastUtil.showToast(R.string.network_canot_work);
            return false;
        }

        if (TextUtils.isEmpty(buttonName)) {
            ToastUtil.showToast(R.string.ir_key_name_empty);
            return false;
        }
        return true;
    }

    @Override
    public void onLeftClick(View v) {

    }

    @Override
    public void onRightClick(View v) {
        editIrButton();
    }
}
