package com.orvibo.homemate.view.custom;

import android.animation.FloatEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.text.style.TtsSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;

import com.smartgateway.app.R;
import com.orvibo.homemate.common.apatch.FileUtils;
import com.orvibo.homemate.util.DisplayUtils;

/**
 * Created by wuliquan on 2016/7/14.
 * 该控件支持点击和滑动切换Item
 */
public class DragOrClickItemView extends SurfaceView implements
        SurfaceHolder.Callback, Runnable {
    private Context mContext;
    //绘制报警标识
    private boolean isArm;
    //用来标识无报警需求
    private boolean isNeedArm=true;
    //绘制进度标识
    private boolean isProgress;
    private boolean isStop;

    // 两个动画扇形旋转的角度
    private float angle=0;
    // 旋转角度
    private float roate_angle=330;
    //指针的角度
    private float cursor_roate_angle;
    //控件的宽
    private float width;
    //控件的高
    private float height;
    //控件的中心坐标的x轴
    private float cenetr_x;
    //控件的中心坐标的y轴
    private float center_y;
    //画笔
    private Paint paint;
    //外边廓宽度
    private float outline_width;
    //里边轮廓宽度
    private float inline_width;
    //
    private int circle_width;

    //控件中心显示图片
    private Bitmap center_bitmap;
    //控件中心箭头图片
    private Bitmap center_cursor_bitmap,arm_cursor_bitmap;
    //在家安防
    private Bitmap home_bitmap,roate_home_bitmap;
    private Bitmap home_press_bitmap,roate_home_press_bitmap;
    //离家安防
    private Bitmap leave_bitmap,roate_leave_bitmap;
    private Bitmap leave_press_bitmap,roate_leave_press_bitmap;
    //撤防图片
    private Bitmap disarm_bitmap,roate_disarm_bitmap;
    private Bitmap disarm_press_bitmap,roate_disarm_press_bitmap;
    //没有报警状态的图片
    private Bitmap no_arm_bitmap;

    private SurfaceHolder sfh;

    private float x=0;
    private float y=0;

    private float angleindex=1;
    /**
     * 默认是撤防
     */
    private int select_flag = 2;

    private long startReadTime = 0L;

    private int oldStatus = 2;

    private ValueAnimator animator;


    //点击切换状态回掉
    private SecurityStatusListener statusListener;

    private SecurityArmListener armListener;

    public DragOrClickItemView(Context context) {
        super(context);
        init(context);
    }

    public DragOrClickItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DragOrClickItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    /**
     * 初始化
     * @param context
     */
    private void init(Context context){

        mContext = context;

        sfh = this.getHolder();
        sfh.addCallback(this);

        outline_width = DisplayUtils.dipToPx(context,32);
        inline_width  = outline_width/2;

        circle_width = DisplayUtils.dipToPx(mContext,3);
        paint = new Paint();
        paint.setAntiAlias(true);

        home_bitmap=BitmapFactory.decodeResource(this.getResources(), R.drawable.icon_inhome_selected);
        leave_bitmap=BitmapFactory.decodeResource(this.getResources(), R.drawable.icon_out_selected);
        disarm_bitmap=BitmapFactory.decodeResource(this.getResources(), R.drawable.icon_disarm_selected);

        home_press_bitmap=BitmapFactory.decodeResource(this.getResources(), R.drawable.icon_inhome_unchecked);
        leave_press_bitmap=BitmapFactory.decodeResource(this.getResources(), R.drawable.icon_out_unchecked);
        disarm_press_bitmap=BitmapFactory.decodeResource(this.getResources(), R.drawable.icon_disarm_unchecked);

        center_cursor_bitmap =BitmapFactory.decodeResource(this.getResources(), R.drawable.icon_center_pointer);
        arm_cursor_bitmap = BitmapFactory.decodeResource(this.getResources(),R.drawable.icon_police);
        center_bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.icon_safe);

        no_arm_bitmap = BitmapFactory.decodeResource(this.getResources(),R.drawable.icon_no_arm);


    }

    public void setStatus(int status,boolean isProgress){
        //在家
        if(status ==0){
            roate_angle = 90;
            select_flag=0;
            oldStatus =0;
            this.isProgress  = isProgress;

        }//离家
        else if(status ==1){
            roate_angle = 210;
            select_flag=1;
            oldStatus =1;
            this.isProgress  = false;
        }//撤防
        else if(status ==2){
            roate_angle = 330;
            select_flag=2;
            oldStatus = 2;
            this.isProgress  = false;
        }
    }

    /**
     * 设置外边扇形动画旋转时间
     * @param time
     */
    public void setAnim(long time){
        stopAnimator();
        startBackgroundAnimator(time);
    }

    /**
     * 设置进度条的开始时间
     * @param time
     */
    public void setStartReadTime(long time){
        isProgress = true;
        startReadTime = time;
    }
    /**
     * 设置接口
     * @param listener
     */
    public void setStatusListener(SecurityStatusListener listener){
        this.statusListener = listener;
    }

    /**
     * 设置暴击监听
     * @param listener
     */
    public void setArmListener(SecurityArmListener listener){
        this.armListener = listener;
    }
    /**
     * 设置报警标识
     * @param isArm
     */
  public void setArm(boolean isArm){
      this.isArm = isArm;
      if(armListener!=null){
          armListener.securityArm(isArm);
      }
  }


    public boolean isNeedArm() {
        return isNeedArm;
    }
    /**
     * 设置是否需求报警
     * @return
     */
    public void setNeedArm(boolean needArm) {
        isNeedArm = needArm;
    }

    /**
     * 停止动画
     */
    public void setStop(){
        isStop =true;
    }

    public void reStart(){
        if(!isStop){
            return;
        }
        isProgress  = false;
        isStop =false;
        new Thread(this).start();
    }

    public void  recover(){
        setStatus(oldStatus,false);
    }
    /**
     * 设置绘制进度条
     * @param isProgress
     */
    public void setProgress(boolean isProgress){
        this.isProgress = isProgress;
    }


    /**
     * 开始背景动画（此处为属性动画）
     */
    private void startBackgroundAnimator(long time){
        float value = ((float)time/(60*1000f));
        float animTime =120*value;
        animator = ValueAnimator.ofFloat(1,animTime ,1);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                angleindex = (float)animation.getAnimatedValue();
            }
        });
        animator.setDuration(time);
        animator.start();
    }

    /**
     * 停止动画
     */
    public void stopAnimator(){
        if(animator!=null){
            animator.end();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                //如果没在点击范围内
                if(!isContain(event)){
                    return false;
                }

                x = event.getX();
                y = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:

                float event_x = event.getX();
                float event_y = event.getY();
                //判断是否是滑动
                if(Math.abs(event_x-x)>30||Math.abs(event_y-y)>30) {
                    roate_angle = getAngle(event_x, event_y)+60;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                float event_x1 = event.getX();
                float event_y1 = event.getY();
                float angle = getAngle(event_x1,event_y1);
                if(angle>330||angle<=90){
                    roate_angle = 90;
                    select_flag=0;
                    if(statusListener != null){
                        statusListener.securityStatus(0);
                    }
                }else if (angle>90&&angle<=210){
                    roate_angle = 210;
                    select_flag=1;
                    if(statusListener != null){
                        statusListener.securityStatus(1);
                    }
                }else if(angle>210&&angle<=330){
                    roate_angle = 330;
                    select_flag=2;
                    if(statusListener != null){
                        statusListener.securityStatus(2);
                    }
                }


                break;
        }
        return true;
    }

    private boolean isContain(MotionEvent event ){
        float x_distance = event.getX()-cenetr_x;
        float y_distance = event.getY()-center_y;
        double distsnce = Math.hypot(Math.abs(x_distance),
                Math.abs(y_distance));
        float radius = Math.min(width-getPaddingRight()-getPaddingLeft(),height-getPaddingTop()-getPaddingBottom())/2-outline_width;
        if(distsnce>radius){
            return false;
        }
        return true;
    }
    /**
     * 以中心为原点，获取旋转角度
     * @param event_x
     * @param event_y
     * @return
     */
    private float getAngle(float event_x, float event_y)
    {
        float x_distance = event_x-cenetr_x;
        float y_distance = event_y-center_y;
        float angle =  (float) (Math.asin(Math.abs(y_distance) / Math.hypot(Math.abs(x_distance),
                                         Math.abs(y_distance))) * 180 / Math.PI);
        if(x_distance>0&&y_distance>0){
            return angle;
        }else if(x_distance>0&&y_distance<0){
            return 270+(90-angle);
        }else if(x_distance<0&&y_distance>0){
            return 90+(90-angle);
        }else if(x_distance<0&&y_distance<0){
            return 180+angle;
        }else {
            return 0;
        }
    }


    /**
     * 绘制控件
     */
    protected void draw() throws Exception{
        if(width==0||height==0){
            //获取宽高
            width = getWidth();
            height = getHeight();
            //计算中心坐标
            cenetr_x = width/2;
            center_y = height/2+getPaddingTop()-getPaddingBottom();

        }

        boolean isOrder = false;

         Canvas canvas = sfh.lockCanvas();

        while(true){
            isOrder = isArm?true:false;
            canvas.drawColor(isArm?getResources().getColor(R.color.arm_red):getResources().getColor(R.color.green));      //覆盖之前的画布
            //绘制背景
            drawBackgroundColor(canvas);

            //绘制动画扇形
            drawAnimaFat(canvas);
            //绘制扇形
            if(isArm&&!isOrder){
                continue;
            }
            drawFan(canvas,roate_angle,isArm);
            //绘制圆环
            drawCircle(canvas);
            //绘制进度
            if(isArm&&!isOrder){
                continue;
            }
            drawProgressBar(canvas,isArm,isProgress);
            //绘制箭头
            drawCursor(canvas,roate_angle);
            //绘制中心图片
            if(isArm&&!isOrder){
                continue;
            }
            drawCenterBitmap(canvas,isArm,isNeedArm);
            //绘制Item图片
            drawItemBitmap(canvas,select_flag);

            break;
        }


        sfh.unlockCanvasAndPost(canvas);

    }

    /**
     * 绘制背景色
     * @param canvas
     */
    private void drawBackgroundColor(Canvas canvas){
          float radius = Math.min(width-getPaddingRight()-getPaddingLeft(),height-getPaddingTop()-getPaddingBottom())/2;
          paint.setColor(Color.parseColor("#19ffffff"));
          paint.setStyle(Paint.Style.FILL);
          paint.setStrokeWidth(20);
          canvas.drawCircle(cenetr_x,center_y,radius,paint);
    }

    /**
     * 绘制外边圆环
     * @param canvas
     */
    private void drawCircle(Canvas canvas){
        float radius = Math.min(width-getPaddingRight()-getPaddingLeft(),height-getPaddingTop()-getPaddingBottom())/2-outline_width;
        paint.setColor(Color.parseColor("#ffffff"));
        paint.setStyle(Paint.Style.STROKE);

        paint.setStrokeWidth(circle_width);
        canvas.drawCircle(cenetr_x,center_y,radius,paint);
    }

    /**
     * 绘制两片旋转的扇形
     *用作动画效果,反方向旋转
     */
    private void drawAnimaFat(final Canvas canvas){
        float radius = Math.min(width-getPaddingRight()-getPaddingLeft(),height-getPaddingTop()-getPaddingBottom())/2-inline_width;
        final RectF oval_stroke = new RectF(cenetr_x-radius, center_y-radius, cenetr_x+radius, center_y+radius);
        paint.setColor(Color.parseColor("#33ffffff"));
        paint.setStyle(Paint.Style.FILL);
        angle += angleindex;
        if(angle>360){
            angle=0;
        }
        canvas.drawArc(oval_stroke, 0 + angle, 150 , true, paint);
        canvas.drawArc(oval_stroke, 180 -angle, 150 , true, paint);


    }
    /**
     *绘制进度条,
     */
    private void drawProgressBar(Canvas canvas,boolean isArm,boolean isProgress){
        if(isArm || !isProgress)
            return;

        float time = System.currentTimeMillis() - startReadTime ;
        if(time > 60*1000){
            this.isProgress = false;
            startReadTime = 0L;
            return;
        }
        float progress = (float)((time / (60*1000))*100);

        float radius =  Math.min(width-getPaddingRight()-getPaddingLeft(),height-getPaddingTop()-getPaddingBottom())/2-outline_width;
        paint.setColor(Color.parseColor("#07fd85"));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(circle_width);

        canvas.save();
        canvas.rotate(-90,cenetr_x,center_y);
        RectF oval = new RectF(cenetr_x-radius, center_y-radius, cenetr_x+radius, center_y+radius);

        float angle = 0;
        if(progress>=0&&progress<=100){
            angle = ((float)progress/100f)*360;
        }
        canvas.drawArc(oval,0,angle,false,paint);
        paint.setStyle(Paint.Style.FILL);

        float []value =calcuteXY(radius,angle);
        if(angle != 0 ) {

            RadialGradient mRadialGradient = new RadialGradient(value[0], value[1], 20, new int[] {
                    Color.parseColor("#3FFC9F"), Color.parseColor("#a03FFC9F") }, null,
                    Shader.TileMode.REPEAT);
            paint.setShader(mRadialGradient);
            canvas.drawCircle(value[0], value[1], 20, paint);
            paint.setShader(null);
            paint.setColor(Color.parseColor("#08FB84"));
            canvas.drawCircle(value[0], value[1], 10, paint);
        }

        canvas.restore();
    }

    /**
     * 计算进度条小球的圆心的xy
     * @param radius
     * @param angle
     * @return
     */
    private float[] calcuteXY(float radius,float angle){

        float [] result = {0,0};
        result[0] = cenetr_x+radius*(float)Math.cos(angle/180*Math.PI);
        result[1] = center_y+radius*(float)Math.sin(angle/180*Math.PI);
        return result;
    }

    /**
     * 绘制扇形
     * @param canvas
     * @param angle
     */
    private void drawFan(Canvas canvas,float angle,boolean isArm){
        float radius =  Math.min(width-getPaddingRight()-getPaddingLeft(),height-getPaddingTop()-getPaddingBottom())/2-outline_width;
        RectF oval_stroke = new RectF(cenetr_x-radius, center_y-radius, cenetr_x+radius, center_y+radius);// 设置个新的长方形，扫描测量
        //保存画布
        canvas.save();
        //旋转画布
        canvas.rotate(angle,cenetr_x,center_y);

        paint.setStrokeWidth(20);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(isArm?Color.parseColor("#f1594b"):getResources().getColor(R.color.green));
        canvas.drawArc(oval_stroke, 0, 120, true, paint);
        canvas.drawArc(oval_stroke,120,120,true,paint);
        paint.setColor(isArm?Color.parseColor("#db5447"):Color.parseColor("#2daf70"));
        canvas.drawArc(oval_stroke,240,120,true,paint);

        paint.setStrokeWidth(1);
        paint.setColor(Color.parseColor("#4cffffff"));
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawArc(oval_stroke, 0, 120, true, paint);
        canvas.drawArc(oval_stroke,120,120,true,paint);
        canvas.drawArc(oval_stroke,240,120,true,paint);

        //恢复画布
        canvas.restore();


    }

    /**
     *  绘制箭头
     */
    private void drawCursor(Canvas canvas,float angle){
        if(center_cursor_bitmap!=null){
            float bitmap_width = center_cursor_bitmap.getWidth();
            float bitmap_height= center_cursor_bitmap.getHeight();
            paint.setColor(Color.BLACK);
            paint.setAntiAlias(true);
            canvas.save();
            canvas.rotate(angle-90,cenetr_x,center_y);
            canvas.drawBitmap(center_cursor_bitmap,(width-bitmap_width)/2,(height-bitmap_height)/2+getPaddingTop()-getPaddingBottom(),paint);
            canvas.restore();
        }
    }
    /**
     * 绘制中心图片
     * @param canvas
     */
    private void drawCenterBitmap(Canvas canvas,boolean isArm,boolean isNeedArm){
        Bitmap bitmap = center_bitmap;
        if(!isNeedArm){
             bitmap = no_arm_bitmap;
        }
        if(isArm){
            bitmap = arm_cursor_bitmap;
        }
        if(bitmap!=null){
            float bitmap_width = bitmap.getWidth();
            float bitmap_height= bitmap.getHeight();

            canvas.drawBitmap(bitmap,(width-bitmap_width)/2,(height-bitmap_height)/2+getPaddingTop()-getPaddingBottom(),paint);
        }
    }

    /**
     * 绘制圆盘上的item图片
     * @param canvas
     */
    private void drawItemBitmap(Canvas canvas,int flag){

        float distance = center_cursor_bitmap.getWidth()-50;
        canvas.drawBitmap(flag==2?disarm_bitmap:disarm_press_bitmap,(width-disarm_bitmap.getWidth())/2,
                (height-disarm_bitmap.getHeight())/2+getPaddingTop()-getPaddingBottom()-distance,paint);

        canvas.save();
        canvas.rotate(240,cenetr_x,center_y);

        if(roate_leave_bitmap==null){
            roate_leave_bitmap = adjustPhotoRotation(leave_bitmap,120);
        }
        if(roate_leave_press_bitmap==null){
            roate_leave_press_bitmap = adjustPhotoRotation(leave_press_bitmap,120);
        }
        canvas.drawBitmap(flag==1?roate_leave_bitmap:roate_leave_press_bitmap,(width-roate_leave_bitmap.getWidth())/2,
                (height-roate_leave_bitmap.getHeight())/2+getPaddingTop()-getPaddingBottom()-distance,paint);

        canvas.restore();

        canvas.save();
        canvas.rotate(120,cenetr_x,center_y);
        if(roate_home_bitmap==null){
            roate_home_bitmap = adjustPhotoRotation(home_bitmap,240);
        }
        if(roate_home_press_bitmap==null){
                roate_home_press_bitmap = adjustPhotoRotation(home_press_bitmap,240);
        }

        canvas.drawBitmap(flag==0?roate_home_bitmap:roate_home_press_bitmap,(width-roate_home_bitmap.getWidth())/2,
                (height-roate_home_bitmap.getHeight())/2+getPaddingTop()-getPaddingBottom()-distance,paint);

        canvas.restore();

    }

    /**
     * 旋转图片
     * @param bm
     * @param orientationDegree
     * @return
     */
    private Bitmap adjustPhotoRotation(Bitmap bm, final int orientationDegree) {

        Matrix m = new Matrix();
        m.setRotate(orientationDegree, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);

        try {
            Bitmap bm1 = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);

            return bm1;

        } catch (OutOfMemoryError ex) {
        }
        return null;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        new Thread(this).start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void run() {

        while(!isStop) {

            try {
                draw();
            }catch (Exception e){
                e.printStackTrace();
            }

                try {
                    Thread.sleep(50);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    /**
     * 跟踪布防状态接口
     */
    public interface SecurityStatusListener{
        /**
         * @param status
         */
        void securityStatus(int status);
    }

    /**
     * 用来跟踪背景颜色
     */
    public interface SecurityArmListener{
        void securityArm(boolean isArm);
    }

    }

