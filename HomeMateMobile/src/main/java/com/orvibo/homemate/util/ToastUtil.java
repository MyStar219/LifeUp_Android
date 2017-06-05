package com.orvibo.homemate.util;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.smartgateway.app.R;
import com.orvibo.homemate.common.ViHomeProApp;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.ErrorMessage;
import com.orvibo.homemate.data.ToastType;

/**
 * @author smagret
 */
public class ToastUtil {
    private static final String TAG = "ToastUtil";
    private static Toast toast;
    /**
     * 字体大小
     */
    private final static int FONT_SIZE = 16;

    private final static Context context = ViHomeProApp.getContext();

    public static void showToast(String text) {
        showToast(text, ToastType.NULL, ToastType.SHORT);
    }

    public static void showToast(int textId) {
        showToast(textId, ToastType.NULL, ToastType.SHORT);
    }

    public static void showToast(int textId, int LONG) {
        showToast(textId, ToastType.NULL, LONG);
    }

    /**
     * @param textId
     * @param imgType {@link com.orvibo.homemate.data.ToastType}
     * @param LONG    0默认2s，1持续时间3.5s
     */
    public static void showToast(int textId, int imgType,
                                 int LONG) {
        String text = context.getResources().getString(textId);
        showToast(text, imgType, LONG == 0 ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG);
    }

    /**
     * @param imgType {@link com.orvibo.homemate.data.ToastType}
     * @param LONG    0默认2s，1持续时间3.5s
     */
    public static void showToast(String text, int imgType,
                                 int LONG) {
        if (context == null) {
            return;
        }
        if (toast == null) {
            toast = new Toast(context);
        }
        show(text, imgType, LONG == 0 ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG);
    }

    private static void show(String text, int imgType, int LONG) {
        LogUtil.d(TAG, "show()-toast " + text);
        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.customtoast, null);
        ImageView image = (ImageView) layout.findViewById(R.id.warningToast_iv);
        if (imgType == ToastType.RIGHT) {
            image.setImageResource(R.drawable.icon_right_normal);
        } else if (imgType == ToastType.ERROR) {
//            image.setImageResource(R.drawable.icon_error_normal);
            image.setVisibility(View.GONE);
        } else if (imgType == ToastType.WAIT) {

        } else if (imgType == ToastType.NULL) {
            image.setVisibility(View.GONE);
        }
        TextView tView = (TextView) layout.findViewById(R.id.textToast_tv);
        tView.setText(text);
        tView.setTextSize(FONT_SIZE);
//        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(LONG == 0 ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }

    /**
     * @param textId
     * @param imgType {@link com.orvibo.homemate.data.ToastType}
     * @param LONG    0默认2s，1持续时间3.5s
     */
    public static void showToast(Handler mHandler,
                                 int textId, final int imgType, final int LONG) {
        if (context == null) {
            return;
        }
        if (toast == null) {
            toast = new Toast(context);
        }
        final String text = context.getResources().getString(textId);
        if (mHandler != null) {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    showToast(text, imgType, LONG == 0 ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG);
                }
            });
        }
    }

    /**
     * @param text
     * @param imgType {@link com.orvibo.homemate.data.ToastType}
     * @param LONG    0默认2s，1持续时间3.5s
     */
    public static void showToast(Handler mHandler,
                                 final String text, final int imgType, final int LONG) {
        if (context == null) {
            return;
        }
        if (toast == null) {
            toast = new Toast(context);
        }
        if (mHandler != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    showToast(text, imgType, LONG == 0 ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG);
                }
            });
        }
    }

    private static void showToast(TextView textView, int LONG) {
        if (toast == null) {
            toast = new Toast(context);
        }
        toast.setView(textView);
        toast.setDuration(LONG == 0 ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG);
        toast.show();
    }

//    private static TextView getTextView() {
//        TextView tView = new TextView(context);
//        tView.setTextColor(context.getResources().getColor(R.color.white));// 字体颜色
//        tView.setBackgroundResource(R.drawable.bg_toast_not_operate);
//        tView.setTextSize(FONT_SIZE);
//        tView.setGravity(Gravity.CENTER);
//        tView.setPadding(15, 8, 15, 8);
//        return tView;
//    }

    /**
     * 显示错误信息
     *
     * @param errorCode 错误码 See {@link com.orvibo.homemate.data.ErrorCode}
     */
    public static void toastError(int errorCode) {
        if (context == null) {
            return;
        }
        if (toast == null) {
            toast = new Toast(context);
        }
        showToast(ErrorMessage.getError(context, errorCode),
                ToastType.ERROR, Toast.LENGTH_SHORT);
    }

    /**
     * 常用的错误提示，比如网络错误，权限错误
     *
     * @param errorCode
     * @return true不需要再toast提示，此接口已经进行了toast提示
     */
    public static boolean toastCommonError(int errorCode) {
        if (context == null) {
            return false;
        }
        if (toast == null) {
            toast = new Toast(context);
        }
        if (ErrorCode.isCommonError(errorCode)) {
            showToast(ErrorMessage.getError(context, errorCode),
                    ToastType.ERROR, Toast.LENGTH_SHORT);
            return true;
        }
        return false;
    }

    /**
     * 返回的是完整的toast提示，比如提示是"请删除[content]设备"
     *
     * @param context
     * @param id
     * @param content 动态显示的内容
     * @return
     */
    public static String getToastStr(Context context, int id, String content) {
        String str = context.getResources().getString(id);
        return String.format(str, content);
    }

    /**
     * 消失提示
     */
    public static void dismissToast() {
        if (toast != null) {
            toast.cancel();
        }
    }

    /**
     * 在UI线程中消失
     *
     * @param mHandler
     */
    public static void dismiss(Handler mHandler) {
        if (mHandler != null) {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    if (toast != null) {
                        toast.cancel();
                    }
                }
            });
        }
    }
}
