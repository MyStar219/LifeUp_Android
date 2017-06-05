package com.google.zxing.client.android;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.smartgateway.app.R;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.common.MainActivity;
import com.orvibo.homemate.util.DisplayUtils;
import com.orvibo.homemate.view.custom.NavigationCocoBar;

import java.util.Hashtable;

/**
 * Created by snown on 2016/6/30.
 *
 * @描述: 机器人二维码生成界面
 */
public class RobotQrActivity extends BaseActivity {
    private int qrWidth;
    private NavigationCocoBar navigationBar;
    private ImageView imageQr;
    private Button finish;
    private static final int BLACK = 0xff000000;
    private static final int PADDING_SIZE_MIN = 20; // 最小留白长度, 单位: px

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_robot_qr);
        this.finish = (Button) findViewById(R.id.finish);
        this.imageQr = (ImageView) findViewById(R.id.imageQr);
        this.navigationBar = (NavigationCocoBar) findViewById(R.id.navigationBar);

        navigationBar.setCenterText(getString(R.string.add) + getString(R.string.device_add_robot));
        qrWidth = DisplayUtils.getScreenWidth(mContext) * 2 / 3;
        String qrText = getIntent().getStringExtra("qrText");

        createQRImage(qrText);

        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RobotQrActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        try {
            imageQr.setImageBitmap(createQRCode(qrText, qrWidth));
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }


    /**
     * 生成二维码图片
     *
     * @param url
     */
    public void createQRImage(String url) {
        try {
            //判断URL合法性
            if (url == null || "".equals(url) || url.length() < 1) {
                return;
            }
            Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            //图像数据转换，使用了矩阵转换
            BitMatrix bitMatrix = new QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, qrWidth, qrWidth, hints);
            int[] pixels = new int[qrWidth * qrWidth];
            //下面这里按照二维码的算法，逐个生成二维码的图片，
            //两个for循环是图片横列扫描的结果
            for (int y = 0; y < qrWidth; y++) {
                for (int x = 0; x < qrWidth; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * qrWidth + x] = 0xff000000;
                    } else {
                        pixels[y * qrWidth + x] = 0xffffffff;
                    }
                }
            }
            //生成二维码图片的格式，使用ARGB_8888
            Bitmap bitmap = Bitmap.createBitmap(qrWidth, qrWidth, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, qrWidth, 0, 0, qrWidth, qrWidth);
            //显示到一个ImageView上面
            imageQr.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }


    public static Bitmap createQRCode(String str, int widthAndHeight) throws WriterException {
        Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        BitMatrix matrix = new MultiFormatWriter().encode(str,
                BarcodeFormat.QR_CODE, widthAndHeight, widthAndHeight, hints);

        int width = matrix.getWidth();
        int height = matrix.getHeight();
        int[] pixels = new int[width * height];

        boolean isFirstBlackPoint = false;
        int startX = 0;
        int startY = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (matrix.get(x, y)) {
                    if (isFirstBlackPoint == false) {
                        isFirstBlackPoint = true;
                        startX = x;
                        startY = y;
                    }
                    pixels[y * width + x] = BLACK;
                }
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

        // 剪切中间的二维码区域，减少padding区域
        if (startX <= PADDING_SIZE_MIN) return bitmap;

        int x1 = startX - PADDING_SIZE_MIN;
        int y1 = startY - PADDING_SIZE_MIN;
        if (x1 < 0 || y1 < 0) return bitmap;

        int w1 = width - x1 * 2;
        int h1 = height - y1 * 2;

        Bitmap bitmapQR = Bitmap.createBitmap(bitmap, x1, y1, w1, h1);

        return bitmapQR;
    }

    @Override
    protected void onResume() {
        super.onResume();
        lightScreen();
    }

    /**
     * 屏幕调到最亮
     */
    private void lightScreen() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = 1.0f;
        getWindow().setAttributes(lp);
    }
}
