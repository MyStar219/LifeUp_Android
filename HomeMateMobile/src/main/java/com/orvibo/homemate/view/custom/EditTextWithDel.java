//package com.orvibo.homemate.view.custom;
//
//import android.content.Context;
//import android.content.res.TypedArray;
//import android.graphics.drawable.Drawable;
//import android.text.Editable;
//import android.text.TextWatcher;
//import android.util.AttributeSet;
//import android.view.MotionEvent;
//import android.widget.EditText;
//
//import com.smartgateway.app.R;
//import com.orvibo.homemate.data.ErrorCode;
//import com.orvibo.homemate.util.EmojiFilterUtil;
//import com.orvibo.homemate.util.LogUtil;
//
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//import java.util.regex.PatternSyntaxException;
//
///**
// * @author smagret
// *         <p/>
// *         带删除按钮的编辑框
// */
//public class EditTextWithDel extends EditText {
//    private static final String TAG = EditTextWithDel.class.getSimpleName();
//    protected OnInputListener mOnPhoneNumListener;
//    private Drawable rightDrawable;
//    private Drawable leftDrawable;
//    private Context mContext;
//    private int times = 0;
//    //private EditTextWithDel editTextWithDel;
//    private boolean autochange = false;
//    private int mMaxLenth = 18;
//    private final int defaultMaxLength = 18;
//
//    /**
//     * 手机号11位
//     */
//    private final int PHONENUM = 11;
//
//    public EditTextWithDel(Context context) throws Exception{
//        super(context);
//        mContext = context;
//    }
//
//    public EditTextWithDel(Context context, AttributeSet attrs, int defStyle) throws Exception{
//        super(context, attrs, defStyle);
//        mContext = context;
//        init(attrs);
//    }
//
//    public EditTextWithDel(Context context, AttributeSet attrs) throws Exception {
//        super(context, attrs);
//        mContext = context;
//        init(attrs);
//    }
//
//    private void init(AttributeSet attrs) throws Exception{
//
//        //editTextWithDel = this;
//        //设置这个属性防止onTextchanged调用多次
//        //this.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
//
//        TypedArray a = mContext.obtainStyledAttributes(attrs,
//                R.styleable.EditTextWithDel);
//        this.leftDrawable = a.getDrawable(R.styleable.EditTextWithDel_left_img);
//        this.mMaxLenth = a.getInt(R.styleable.EditTextWithDel_maxLenth,defaultMaxLength);
//        a.recycle();
//
//        rightDrawable = mContext.getResources().getDrawable(R.drawable.bg_delete_selector);
//        addTextChangedListener(new TextWatcher() {
//            int byteLength = 0;
//            int currentLength = 0;
//            int startSelection = 0;
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before,
//                                      int count) {
//            }
//
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count,
//                                          int after) {
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//
//                if (autochange) { // skip execution if triggered by code
//                    autochange = false; // next change is not triggered by code
//                    return;
//                }
//
//                String str = s.toString().trim();
//                startSelection = getSelectionStart();
//
//                str = EmojiFilterUtil.filterEmoji(str);
//                int removeEmojiTextLen = str.length();//移除表情字符后str的长度
//
//                autochange = true;
//                str = str.trim();
//                //setText(str);
//
//                s.replace(0,removeEmojiTextLen,str);
//                setSelection(Math.min(removeEmojiTextLen,startSelection));
//                byteLength = str.getBytes().length;
//                while (byteLength > mMaxLenth) {
//                    currentLength = str.length();
//                    s.delete(currentLength - 1, currentLength);
//                    LogUtil.d(TAG, "afterTextChanged() - s = " + s.toString());
//                }
//                setDrawable();
//            }
//        });
//        setDrawable();
//    }
//
//    public static String stringFilter(String str) throws PatternSyntaxException {
//        // 只允许字母和数字
//        String regEx = "[^a-zA-Z0-9]";
//        Pattern p = Pattern.compile(regEx);
//        Matcher m = p.matcher(str);
//        return m.replaceAll("").trim();
//    }
//
//    // 设置删除图片
//    private void setDrawable() {
//        if (length() == 0) {
//            if (mOnPhoneNumListener != null) {
//                mOnPhoneNumListener.onUnlawful(ErrorCode.PHONE_ERROR);
//            }
//            setCompoundDrawablesWithIntrinsicBounds(leftDrawable, null, null,
//                    null);
//        } else if (length() == PHONENUM) {
//            if (mOnPhoneNumListener != null) {
//                mOnPhoneNumListener.onRightful();
//            }
//            setCompoundDrawablesWithIntrinsicBounds(leftDrawable, null,
//                    rightDrawable, null);
//        } else {
//            if (mOnPhoneNumListener != null) {
//                mOnPhoneNumListener.onUnlawful(ErrorCode.PHONE_ERROR);
//            }
//            setCompoundDrawablesWithIntrinsicBounds(leftDrawable, null,
//                    rightDrawable, null);
//        }
//    }
//
//    public void hideDeleteDrawable() {
//        rightDrawable = null;
//        setCompoundDrawablesWithIntrinsicBounds(leftDrawable, null,
//                rightDrawable, null);
//    }
//
//    public void showDeleteDrawable() {
//        rightDrawable = mContext.getResources().getDrawable(
//                R.drawable.bg_delete_selector);
//        setCompoundDrawablesWithIntrinsicBounds(leftDrawable, null,
//                rightDrawable, null);
//    }
//
//
//    // 处理删除事件
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//
//        if (getCompoundDrawables()[2] != null) {
//            if (event.getAction() == MotionEvent.ACTION_UP) {
//                boolean touchable = event.getX() > (getWidth() - getPaddingRight() - rightDrawable.getIntrinsicWidth())
//                        && (event.getX() < ((getWidth() - getPaddingRight())));
//                if (touchable) {
//                    this.setText("");
//
//                    callbackClearText();
//                }
//            }
//        }
//        return super.onTouchEvent(event);
//    }
//
//    @Override
//    protected void finalize() throws Throwable {
//        super.finalize();
//    }
//
//    public Drawable getLeftDrawable() {
//        return leftDrawable;
//    }
//
//    public void setLeftDrawable(Drawable leftDrawable) {
//        this.leftDrawable = leftDrawable;
//    }
//
//    public void setOnPhoneNumListener(OnInputListener l) {
//        mOnPhoneNumListener = l;
//    }
//
//    private void callbackClearText() {
//        if (mOnPhoneNumListener != null) {
//            mOnPhoneNumListener.onClearText();
//        }
//    }
//
//    /**
//     * Interface definition for a callback to be invoked when a phone num is 11.
//     */
//    public interface OnInputListener {
//        /**
//         * Called when a view has been clicked.
//         * <p/>
//         * The view that was clicked.
//         */
//        void onRightful();
//
//        void onUnlawful(int error);
//
//        void onClearText();
//    }
//
//}
