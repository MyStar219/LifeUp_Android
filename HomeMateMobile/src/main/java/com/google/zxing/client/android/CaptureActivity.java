/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.client.android;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.google.zxing.client.android.camera.CameraManager;
import com.orvibo.homemate.api.listener.BaseResultListener;
import com.orvibo.homemate.bo.QRCode;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.common.launch.EggsActivity;
import com.orvibo.homemate.dao.QRCodeDao;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.ModelID;
import com.orvibo.homemate.device.manage.add.DeviceAddActivity;
import com.orvibo.homemate.device.manage.add.DeviceAddListActivity;
import com.orvibo.homemate.device.manage.add.DeviceScannedResultActivity;
import com.orvibo.homemate.device.ys.YsAdd2Activity;
import com.orvibo.homemate.event.BaseEvent;
import com.orvibo.homemate.util.Constants;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.view.custom.DialogFragmentOneButton;
import com.orvibo.homemate.view.custom.NavigationCocoBar;
import com.orvibo.homemate.view.custom.ProgressDialogFragment;
import com.smartgateway.app.R;
import com.videogo.universalimageloader.core.DisplayImageOptions;
import com.videogo.universalimageloader.core.ImageLoader;
import com.videogo.universalimageloader.core.assist.ImageScaleType;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * This activity opens the camera and does the actual scanning on a background thread. It draws a
 * viewfinder to help the user place the barcode correctly, shows feedback as the image processing
 * is happening, and then overlays the results when a scan is successful.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 * @author Sean Owen
 */
public final class CaptureActivity extends BaseActivity implements SurfaceHolder.Callback,BaseResultListener {

    private static final String TAG = CaptureActivity.class.getSimpleName();
    private CameraManager cameraManager;
    private CaptureActivityHandler handler;
    private ViewfinderView viewfinderView;
    private Result lastResult;
    private boolean hasSurface;
    private IntentSource source;
    private Collection<BarcodeFormat> decodeFormats;
    private Map<DecodeHintType, ?> decodeHints;
    private String characterSet;
    private InactivityTimer inactivityTimer;
    private BeepManager beepManager;
    private AmbientLightManager ambientLightManager;
    private NavigationCocoBar navigationCocoBar;
    private TextView cameraOpenFailedTip;
    private QRCodeDao mQrCodeDao;
    private ImageLoader mImageLoader;
    private DisplayImageOptions mOptions;
    private QRCode mQrCode;
    //判断是否是大拿添加页面跳转的扫描
    private String danaleScan;

    private String result;
    private boolean isInclude;
    private String model;
    private String hopeQrCode;
    private boolean isSpecial;

    ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    CameraManager getCameraManager() {
        return cameraManager;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.activity_capture2);

        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);
        beepManager = new BeepManager(this);
        ambientLightManager = new AmbientLightManager(this);
        navigationCocoBar = (NavigationCocoBar) findViewById(R.id.navigationBar);
        navigationCocoBar.setRightTextViewVisibility(View.INVISIBLE);
        // initDisplayImageOptions();
        // initImage();
        initDao();
    }

    private void initDao() {
        mQrCodeDao = new QRCodeDao();
    }

    private void initImage() {
        mImageLoader = ImageLoader.getInstance();
    }

    private void initDisplayImageOptions() {
        mOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.launch_logo) // resource or drawable
                .showImageForEmptyUri(R.drawable.launch_logo) // resource or drawable
                .showImageOnFail(R.drawable.launch_logo) // resource or drawable
                .resetViewBeforeLoading(false)  // default
                .delayBeforeLoading(1000)
                .cacheInMemory(true) // default
                .cacheOnDisk(true) // default
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT) // default
                .bitmapConfig(Bitmap.Config.RGB_565) // default
                .build();

    }

    @Override
    protected void onResume() {
        super.onResume();

        // CameraManager must be initialized here, not in onCreate(). This is necessary because we don't
        // want to open the camera driver and measure the screen size if we're going to show the help on
        // first launch. That led to bugs where the scanning rectangle was the wrong size and partially
        // off screen.
        reScanQr();
    }

    private void reScanQr() {
        cameraManager = new CameraManager(getApplication());

        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
        viewfinderView.setCameraManager(cameraManager);
        cameraOpenFailedTip = (TextView) findViewById(R.id.cameraOpenFailedTip);

        handler = null;
        lastResult = null;

        resetStatusView();

        beepManager.updatePrefs();
        ambientLightManager.start(cameraManager);
        inactivityTimer.onResume();

        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            // The activity was paused but not stopped, so the surface still exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            initCamera(surfaceHolder);
        } else {
            // Install the callback and wait for surfaceCreated() to init the camera.
            surfaceHolder.addCallback(this);
        }
    }


    @Override
    protected void onPause() {
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        inactivityTimer.onPause();
        ambientLightManager.stop();
        //失去焦点时关闭摄像头
        cameraManager.closeDriver();
        if (!hasSurface) {
            SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            surfaceHolder.removeCallback(this);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (source == IntentSource.NATIVE_APP_INTENT) {
                    setResult(RESULT_CANCELED);
                    finish();
                    return true;
                }
                if ((source == IntentSource.NONE || source == IntentSource.ZXING_LINK) && lastResult != null) {
                    restartPreviewAfterDelay(0L);
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_FOCUS:
            case KeyEvent.KEYCODE_CAMERA:
                // Handle these events so they don't launch the Camera app
                return true;
            // Use volume up/down to turn on light
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                cameraManager.setTorch(false);
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
                cameraManager.setTorch(true);
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (holder == null) {
            Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
        }
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
//        holder.removeCallback(this);
        hasSurface = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    /**
     * 对扫描结果进行匹配处理
     * A valid barcode has been found, so give an indication of success and show the results.
     *
     * @param rawResult   The contents of the barcode.
     * @param scaleFactor amount by which thumbnail was scaled
     * @param barcode     A greyscale bitmap of the camera data which was decoded.
     */
    public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
        isSpecial = false;
        model = "";
        showDialogNow(null, getString(R.string.loading1));
        inactivityTimer.onActivity();
        lastResult = rawResult;
//        beepManager.playBeepSoundAndVibrate();
        result = rawResult.getText();
        LogUtil.d(TAG, "handleDecode()-result:" + result);
        //如果解析到的数据前缀是http://music.hopesmart.cn/MyDeviceAdd.aspx报错如上，防止误扫描到旁边的微信二维码时报错不明确。
        if (result.startsWith(Constants.QR_ADD_BACKGROUND_MUSIC_ERROR)) {
            dismissDialog();
            Intent intent = new Intent(CaptureActivity.this, DeviceAddActivity.class);
            intent.putExtra(IntentKey.BACKGROUND_MUSIC_SCAN_ERROR, true);
            startActivity(intent);
            finish();
            return;
        }
        if (result.contains("ys7")) {
            Intent intent = new Intent(this, YsAdd2Activity.class);
            intent.putExtra(IntentKey.DEVICE, result);
            startActivity(intent);
            finish();
        } else {
            //  int qrCodeId = 0;
            int qrCodeNo = 0;
            hopeQrCode = "";
            if (result.equals(Constants.QR_EGG_COMMON_URL)) {
                Intent intent = new Intent(this, EggsActivity.class);
                startActivity(intent);
                finish();
                return;
            } else if (result.equals(Constants.QR_CODE_COCO_URL) || result.equals(Constants.QR_CODE_COCO_URL_ALIYUN) || result.equals(Constants.QR_CODE_COCO_URL_1)) {
                // qrCodeId = 1;
                qrCodeNo = 1;
            } else if (result.contains(Constants.MATCH_RULE1) && result.contains(Constants.MATCH_RULE2)) {
                //解决诸多OEM版本APP，会有对应数量的不同地址二维码，经常忘记修改这一块的地址前缀，导致扫码失败
                // 确保字符串里面包含orvibo.com
                // 确保字符串里面包含html?id=
                // 符合以上两点即可确认这是一个智家365系列产品所支持的二维码格式
                //http://www.orvibo.com/software/365.html?id=20&uid=ea8c697055e545c2bc49709617655ca0,后期的扫秒结果可能是这个，所以这里需要对id(用来查询数据里是否有该设备)和uid（）的值做处理
               /*  else if (result.startsWith(Constants.QR_CODE_COMMON_URL)) {
                  String id = result.substring(result.lastIndexOf("=") + 1);*/
                String id = result.substring(result.indexOf(Constants.MATCH_RULE3) + Constants.MATCH_RULE3.length());//20&uid=ea8c697055e545c2bc49709617655ca0
                LogUtil.e(TAG, "id=" + id);
                if (StringUtil.isNumber(id)) {
                    // qrCodeId = Integer.parseInt(id);
                    qrCodeNo = Integer.parseInt(id);
                    LogUtil.e(TAG, "qrCodeNo:" + "****************************************" + qrCodeNo);
                } else {
                    //id=20&uid=ea8c697055e545c2bc49709617655ca0
                    String uid = id.substring(id.lastIndexOf("=") + 1);
                    String[] ids = id.split("&");
                    qrCodeNo = Integer.parseInt(ids[0]);
                }

            } else if (!StringUtil.isEmpty(result)) {
                String id = result.substring(result.lastIndexOf("=") + 1);
                if (id.contains("HOPE")) {  //向往背景音乐
//                if (id.contains("HOPE-A10") || id.contains("HOPE-A7")) {
                    hopeQrCode = id;
                    isSpecial = true;
                }
            }
            isInclude = false;
            //如果添加的是背景音乐则采用固定model的方式
            if (isSpecial) {
                isInclude = true;
                model = ModelID.HOPE_BACKGROUND_MUSIC;
            } else {

                //二维码返回信息对象
                mQrCode = mQrCodeDao.selQRCode(qrCodeNo);
            }

            if (mQrCode != null) {
                isInclude = true;
            }
           /* int[] ids = getResources().getIntArray(R.array.device_id);
            boolean isInclude = false;
            int postion = -1;
            for (int id : ids) {
                postion++;
                //
                if (qrCodeId == id) {
                    isInclude = true;
                    break;
                }
            }*/

/*            if (isInclude) {
                dismissDialog();
                Intent intent = new Intent(CaptureActivity.this, DeviceScannedResultActivity.class);
                // intent.putExtra("id", postion);
                //把设备id传递给结果页面
                intent.putExtra("QRCode", mQrCode);
                intent.putExtra("model", model);
                intent.putExtra("hopeQrCode", hopeQrCode);
                intent.putExtra("isSpecial", isSpecial);
                //intent.putExtra("bitmap", bitmap);
                // MyLogger.jLog().d("qId=" + qrCodeId + "  postion=" + postion);
                startActivity(intent);
            } else {
                Intent intent = new Intent(this, DeviceAddListActivity.class);
                intent.putExtra("isShowDialog", true);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }*/
            scanResultOperate(isInclude,model,hopeQrCode,isSpecial);
        }
//    restartPreviewAfterDelay(1000L);
    }

    private void scanResultOperate(boolean isInclude,String model,String hopeQrCode,boolean isSpecial){
        if (isInclude) {
            dismissDialog();
            Intent intent = new Intent(CaptureActivity.this, DeviceScannedResultActivity.class);
            // intent.putExtra("id", postion);
            //把设备id传递给结果页面
            intent.putExtra("QRCode", mQrCode);
            intent.putExtra("model", model);
            intent.putExtra("hopeQrCode", hopeQrCode);
            intent.putExtra("isSpecial", isSpecial);
            //intent.putExtra("bitmap", bitmap);
            // MyLogger.jLog().d("qId=" + qrCodeId + "  postion=" + postion);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, DeviceAddListActivity.class);
            intent.putExtra("isShowDialog", true);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        finish();
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (cameraManager.isOpen()) {
            Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
            return;
        }
        try {
            cameraManager.openDriver(surfaceHolder);
            // Creating the handler starts the preview, which can also throw a RuntimeException.
            if (handler == null) {
                handler = new CaptureActivityHandler(this, decodeFormats, decodeHints, characterSet, cameraManager);
            }
            if (cameraOpenFailedTip.isShown())
                cameraOpenFailedTip.setVisibility(View.GONE);
        } catch (IOException ioe) {
            Log.w(TAG, ioe);
        } catch (RuntimeException e) {
            cameraOpenFailedTip.setVisibility(View.VISIBLE);
            // Barcode Scanner has seen crashes in the wild of this variety:
            // java.?lang.?RuntimeException: Fail to connect to camera service
            Log.w(TAG, "Unexpected error initializing camera", e);
        }
    }

    public void restartPreviewAfterDelay(long delayMS) {
        if (handler != null) {
            handler.sendEmptyMessageDelayed(R.id.restart_preview, delayMS);
        }
        resetStatusView();
    }

    private void resetStatusView() {
        viewfinderView.setVisibility(View.VISIBLE);
        lastResult = null;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();
    }

    @Override
    public void showDialogNow(ProgressDialogFragment.OnCancelClickListener onCancelClickListener, String content) {
        super.showDialogNow(onCancelClickListener, content);
    }

    /**
     * 注册大拿账号的回调
     * @param baseEvent
     */
    @Override
    public void onResultReturn(BaseEvent baseEvent) {
        if (isFinishingOrDestroyed())
            return;
        if (baseEvent.isSuccess()){
        }else{
            //注册大拿账号失败，按照之前的扫码流程走下去
            scanResultOperate(isInclude,model,hopeQrCode,isSpecial);
        }
    }

    /**
     * 显示需要绑定邮箱或者手机的对话框
     */
    private void showUnBindPhoneOrEmailDialog(){
        dismissDialog();
        DialogFragmentOneButton dialogFragmentOneButton = new DialogFragmentOneButton();
        dialogFragmentOneButton.setTitle(getString(R.string.warm_tips));
        dialogFragmentOneButton.setContent(getString(R.string.need_bind_phone_or_email));
        dialogFragmentOneButton.setOnButtonClickListener(new DialogFragmentOneButton.OnButtonClickListener() {
            @Override
            public void onButtonClick(View view) {
                finish();
            }
        });
        dialogFragmentOneButton.show(getFragmentManager(),"");
    }
}
