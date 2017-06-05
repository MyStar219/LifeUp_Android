package com.orvibo.homemate.room;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Floor;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.ErrorMessage;
import com.orvibo.homemate.data.ToastType;
import com.orvibo.homemate.model.ModifyFloor;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.EditTextWithCompound;
import com.orvibo.homemate.view.popup.ConfirmAndCancelPopup;


/**
 * @author Smagret
 * @data 2015/03/28
 */
public class ModifyFloorActivity extends BaseActivity {

    private static final String TAG = ModifyFloorActivity.class
            .getSimpleName();
    private EditTextWithCompound floorName_et;
    private ModifyFloorControl modifyFloorControl;
    private Floor floor;
    private String uid;
    private String floorName;
    private String floorId;
    private String newFloorName;
    private SavePopup savaPopup;
    private final int RESULT_CODE_MODIFY_FLOOR = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.modify_floor);
        init();
    }

    private void init() {
        Intent intent = getIntent();
        floor = (Floor) intent.getSerializableExtra("floor");

        if (floor == null) {
            finish();
        } else {
            floorName = floor.getFloorName();
            uid = floor.getUid();
            floorId = floor.getFloorId();

            modifyFloorControl = new ModifyFloorControl(mAppContext);
            initView();
            initListener();
        }
    }

    private void initView() {
        floorName_et = (EditTextWithCompound) findViewById(R.id.floorName_et);
        floorName_et.setMaxLength(12);
        floorName_et.setNeedRestrict(false);
        floorName_et.setText(floorName);
        floorName_et.setRightful(getResources().getDrawable(R.color.transparent));
        if (!StringUtil.isEmpty(floorName)) {
            floorName = floorName_et.getText().toString();
            floorName_et.setSelection(floorName.length());
        }
        savaPopup = new SavePopup();
    }

    private void initListener() {

    }

    public void save(View v) {
        modifyFloorName();
    }

    /**
     * 删除楼层
     * @param view
     */
    public void rightTitleClick(View view) {

    }

    private void modifyFloorName(){
        newFloorName = floorName_et.getText().toString();
        if (StringUtil.isEmpty(newFloorName)) {
            ToastUtil.showToast(R.string.floor_name_empty);
            return;
        }

        if (floorName.equals(newFloorName)) {
            finish();
        }
        modifyFloorControl.startModifyFloor(userName, uid, floorId, newFloorName);
        showDialogNow();
    }

    private class ModifyFloorControl extends ModifyFloor {

        public ModifyFloorControl(Context context) {
            super(context);
        }

        @Override
        public void onModifyFloorResult(String uid, int serial, int result) {
            dismissDialog();
            if (result == ErrorCode.SUCCESS) {
                Intent intent = new Intent();
                intent.putExtra("newFloorName", newFloorName);
                ModifyFloorActivity.this.setResult(RESULT_CODE_MODIFY_FLOOR, intent);
                ModifyFloorActivity.this.finish();
            } else {
                ToastUtil.showToast(
                        ErrorMessage.getError(mAppContext, result),
                        ToastType.ERROR, ToastType.SHORT);
            }
        }

    }

    private class SavePopup extends ConfirmAndCancelPopup {
        /**
         * 点击确定按钮
         */
        public void confirm() {
            modifyFloorName();
            dismiss();
        }

        public void cancel() {
            dismiss();
            finish();
        }
    }

    /**
     * 标题栏返回事件
     *
     * @param v
     */
    public void leftTitleClick(View v) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        newFloorName = floorName_et.getText().toString();
        if (!StringUtil.isEmpty(newFloorName)&&!floorName.equals(newFloorName)) {
            savaPopup.showPopup(ModifyFloorActivity.this, getResources()
                    .getString(R.string.save_modify_or_not), getResources()
                    .getString(R.string.save), getResources()
                    .getString(R.string.not_save));
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (savaPopup != null&&savaPopup.isShowing()) {
            savaPopup.dismiss();
        }
        super.onDestroy();
    }
}
