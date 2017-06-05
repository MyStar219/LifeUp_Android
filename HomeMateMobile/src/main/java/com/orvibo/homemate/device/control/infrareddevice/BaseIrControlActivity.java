package com.orvibo.homemate.device.control.infrareddevice;

import com.orvibo.homemate.device.control.BaseControlActivity;
import com.orvibo.homemate.view.custom.IrButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangqiyao on 2015/12/3.
 */
public class BaseIrControlActivity extends BaseControlActivity {
    protected List<IrButton> irNoButtons = new ArrayList<IrButton>();

    @Override
    protected void onResume() {
        super.onResume();
        if (irNoButtons != null && !irNoButtons.isEmpty()) {
            for (final IrButton irButton : irNoButtons) {
                irButton.refresh();
            }
        }
    }
}
