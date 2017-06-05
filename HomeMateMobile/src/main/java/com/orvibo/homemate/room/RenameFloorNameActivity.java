package com.orvibo.homemate.room;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Floor;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.view.custom.EditTextWithCompound;
import com.orvibo.homemate.view.popup.ConfirmAndCancelPopup;


/**
 * @author Smagret
 * @data 2015/03/28
 */
public class RenameFloorNameActivity extends BaseActivity {

	private static final String TAG = RenameFloorNameActivity.class
			.getSimpleName();
	private EditTextWithCompound floorName_et;
	private Floor floor;
	private String uid;
	private String floorName;
	private int floorId;
    private String newFloorName;

    private SavePopup savaPopup;
    private ToastPopup toastPopup;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rename_floor_name);
		init();
	}

	private void init() {
		Intent intent = getIntent();
		floor = (Floor) intent.getSerializableExtra("floor");
        if (floor != null) {
            floorName = floor.getFloorName();
        } else {
            finish();
        }

		initView();
		initListener();
	}

	private void initView() {
		floorName_et = (EditTextWithCompound) findViewById(R.id.floorName_et);
		floorName_et.setText(floorName);
        floorName_et.setNeedRestrict(false);
        savaPopup = new SavePopup();
        toastPopup = new ToastPopup();
	}

	private void initListener() {

	}

	public void save(View v) {
        reName();
	}

    public void leftTitleClick(View v) {
        newFloorName = floorName_et.getText().toString();
        if (!StringUtil.isEmpty(newFloorName)&&!floorName.equals(newFloorName)) {
            savaPopup.showPopup(RenameFloorNameActivity.this, getResources()
                    .getString(R.string.save_modify_or_not), getResources()
                    .getString(R.string.save), getResources()
                    .getString(R.string.not_save));
        } else {
            this.finish();
        }
    }

    private void reName() {
        newFloorName = floorName_et.getText().toString();
        if (StringUtil.isEmpty(newFloorName)) {
            toastPopup.showPopup(RenameFloorNameActivity.this, getResources()
                    .getString(R.string.floor_name_empty), getResources()
                    .getString(R.string.know),null);
            return;
        }

        if (floorName.equals(newFloorName)) {
            toastPopup.showPopup(RenameFloorNameActivity.this, getResources()
                    .getString(R.string.message_not_modified), getResources()
                    .getString(R.string.know),null);
            return;
        }

        Intent intent = new Intent();
        intent.putExtra("newFloorName", newFloorName);
        RenameFloorNameActivity.this.setResult(RESULT_OK, intent);
        RenameFloorNameActivity.this.finish();
    }

    private class SavePopup extends ConfirmAndCancelPopup {
        /**
         * 点击确定按钮
         */
        public void confirm() {
            Intent intent = new Intent();
            intent.putExtra("newFloorName", newFloorName);
            RenameFloorNameActivity.this.setResult(RESULT_OK, intent);
            RenameFloorNameActivity.this.finish();
        }
        public void cancel() {
            dismiss();
            finish();
        }
    }

    private class ToastPopup extends ConfirmAndCancelPopup {
        /**
         * 点击确定按钮
         */
        public void confirm() {
            dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        newFloorName = floorName_et.getText().toString();
        if (!StringUtil.isEmpty(newFloorName)&&!floorName.equals(newFloorName)) {
            savaPopup.showPopup(RenameFloorNameActivity.this, getResources()
                    .getString(R.string.save_modify_or_not), getResources()
                    .getString(R.string.save), getResources()
                    .getString(R.string.not_save));
        } else {
            super.onBackPressed();
        }
    }

	@Override
	protected void onDestroy() {
        if (savaPopup != null&&savaPopup.isShowing()) {
            savaPopup.dismiss();
        }

        if (toastPopup != null&&toastPopup.isShowing()) {
            toastPopup.dismiss();
        }
		super.onDestroy();
	}

}
