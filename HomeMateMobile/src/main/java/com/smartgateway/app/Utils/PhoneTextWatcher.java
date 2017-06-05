package com.smartgateway.app.Utils;

import android.widget.EditText;

/**
 * Created by dpr on 19/10/16.
 */

public class PhoneTextWatcher extends HardcodedTextWatcher {

    private static final String prefix = "(+65)";

    public PhoneTextWatcher(EditText edit) {
        super("(+65)", edit);
    }

    public static boolean isPhoneNotValid(EditText editText) {
        if (editText == null) {
            return false;
        }
        return editText.getText().length() != (8 + prefix.length());
    }

    public static String getValue(EditText editText) {
        return editText.getText().toString().replace(prefix, "");
    }
}
