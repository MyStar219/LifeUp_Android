package com.smartgateway.app.Utils;

import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.widget.EditText;

import java.lang.ref.WeakReference;

public class HardcodedTextWatcher implements TextWatcher {

    private String prefix;
    private WeakReference<EditText> edit;

    public HardcodedTextWatcher(String prefix, EditText edit) {
        this.prefix = prefix;
        this.edit = new WeakReference<>(edit);
        edit.setText(prefix);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        EditText editText = edit.get();
        if (editText == null) {
            return;
        }

        if (!s.toString().startsWith(prefix)) {
            editText.setText(prefix);
            Selection.setSelection(editText.getText(), editText
                    .getText().length());
        }
    }
}
