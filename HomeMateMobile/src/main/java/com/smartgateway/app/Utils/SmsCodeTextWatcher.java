package com.smartgateway.app.Utils;

import android.widget.EditText;

public class SmsCodeTextWatcher extends HardcodedTextWatcher {

    private static final String prefix = "S-";

    public SmsCodeTextWatcher(EditText edit) {
        super(prefix, edit);
    }

    public static String getValue(EditText editText) {
        return editText.getText().toString().replace(prefix, "");
    }
}
