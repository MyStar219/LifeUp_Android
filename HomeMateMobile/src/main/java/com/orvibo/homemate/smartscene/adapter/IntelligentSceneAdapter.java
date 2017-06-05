package com.orvibo.homemate.smartscene.adapter;

import android.content.Context;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Linkage;
import com.orvibo.homemate.bo.Scene;
import com.orvibo.homemate.bo.Security;
import com.orvibo.homemate.bo.SmartScene;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.LinkageActiveType;
import com.orvibo.homemate.util.IntelligentSceneTool;
import com.orvibo.homemate.util.LibIntelligentSceneTool;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.OfflineView;
import com.orvibo.homemate.view.custom.ProgressView;
import com.orvibo.homemate.view.custom.SecurityCountdownTextView;

import java.util.List;

/**
 * Created by Smagret on 2015/10/14.
 */
public class IntelligentSceneAdapter extends BaseAdapter {
    private static final String TAG = IntelligentSceneAdapter.class.getSimpleName();
    private static final String SAMPLE_ID = Constant.INVALID_NUM + "";
    private List<SmartScene> smartScenes;
    private Context mContext;
    private static final int SCENE_ITEM = 0;
    private static final int LINKAGE_ITEM = 1;
    private static final int SECURITY_ITEM = 2;
    private static final int MAX_SCENE_DEVICE_ICON_COUNT = 5;
    private static final int MAX_LINKAGE_DEVICE_ICON_COUNT = 3;
    private static final int MAX_SECUTIY_DEVICE_ICON_COUNT = 5;

    private View.OnClickListener clickListener;

    private LayoutInflater mInflater;
    private boolean isFinish = false;
    public int countdown;
    private String mMainUid;

    public IntelligentSceneAdapter(Context context, List<SmartScene> smartScenes, View.OnClickListener listener) {
        LogUtil.d(TAG, "IntelligentSceneAdapter()-smartScenes:" + smartScenes);
        mContext = context;
        this.smartScenes = smartScenes;
        this.clickListener = listener;
        mInflater = LayoutInflater.from(context);
    }

    public void refresh(List<SmartScene> smartScenes) {
        LogUtil.d(TAG, "refresh()-smartScenes:" + smartScenes);
        this.smartScenes = smartScenes;
        notifyDataSetChanged();
    }

    public void refreshSampleLinkage(boolean isActive) {
        if (smartScenes != null && !smartScenes.isEmpty()) {
            for (SmartScene smartScene : smartScenes) {
                if (smartScene.linkage != null && SAMPLE_ID.equals(smartScene.linkage.getLinkageId())) {
                    smartScene.linkage.setIsPause(isActive ? LinkageActiveType.ACTIVE : LinkageActiveType.PAUSE);
                    notifyDataSetChanged();
                    break;
                }
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        int itemType = LINKAGE_ITEM;
        SmartScene smartScene = smartScenes.get(position);
        if (smartScene.scene != null) {
            itemType = SCENE_ITEM;
        } else if (smartScene.scene == null && smartScene.linkage == null && smartScene.security != null) {
            itemType = SECURITY_ITEM;
        }
        return itemType;
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public int getCount() {
        return smartScenes == null ? 0 : smartScenes.size();
    }

    @Override
    public Object getItem(int position) {
        return smartScenes == null ? null : smartScenes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int itemType = getItemViewType(position);
        SmartScene smartScene = smartScenes.get(position);
        LogUtil.d(TAG, "getView()-smartScene:" + smartScene);
        if (itemType == SCENE_ITEM) {
            //情景item
            SceneViewHolder sceneViewHolder;
            if (convertView == null) {
                sceneViewHolder = new SceneViewHolder();
                convertView = mInflater.inflate(R.layout.intelligent_scene_list_item_hd, parent, false);
                sceneViewHolder.name = (TextView) convertView.findViewById(R.id.tvIntelligentSceneName);
                sceneViewHolder.deviceTypeIconGridView = (GridView) convertView.findViewById(R.id.deviceTypeIconGridView);
                sceneViewHolder.startTextView = (TextView) convertView.findViewById(R.id.startTextView);
                sceneViewHolder.OfflineView = (OfflineView) convertView.findViewById(R.id.offline_view);
                convertView.setTag(sceneViewHolder);
            } else {
                sceneViewHolder = (SceneViewHolder) convertView.getTag();
            }
            Scene scene = smartScene.scene;
            if ((Constant.INVALID_NUM + "").equals(scene.getSceneNo())) {
                sceneViewHolder.OfflineView.setVisibility(View.VISIBLE);
            } else {
                sceneViewHolder.OfflineView.setVisibility(View.GONE);
            }
            sceneViewHolder.name.setText(scene.getSceneName());
            List<Integer> deviceTypeList = LibIntelligentSceneTool.getDeviceTypeList(scene);
           /* DeviceIconsAdapter deviceIconsAdapter = new DeviceIconsAdapter(mContext,
                    deviceTypeList.size() > MAX_SCENE_DEVICE_ICON_COUNT ? deviceTypeList.subList(0, MAX_SCENE_DEVICE_ICON_COUNT) : deviceTypeList);*/
            DeviceIconsAdapter deviceIconsAdapter = new DeviceIconsAdapter(mContext,
                    deviceTypeList.size() > MAX_SCENE_DEVICE_ICON_COUNT ? deviceTypeList.subList(0, MAX_SCENE_DEVICE_ICON_COUNT) : deviceTypeList, smartScene);
            sceneViewHolder.deviceTypeIconGridView.setAdapter(deviceIconsAdapter);
            sceneViewHolder.deviceTypeIconGridView.setClickable(false);
            sceneViewHolder.deviceTypeIconGridView.setPressed(false);
            sceneViewHolder.deviceTypeIconGridView.setEnabled(false);
            expandViewTouchDelegate(sceneViewHolder.startTextView, convertView);
            sceneViewHolder.startTextView.setTag(R.id.tag_scene, scene);
            sceneViewHolder.startTextView.setOnClickListener(clickListener);
            //  convertView.setLayoutParams(lp);
        } else if (itemType == LINKAGE_ITEM) {
            //联动item
            LinkageViewHolder linkageViewHolder = null;
            if (convertView == null) {
                //  linkageViewHolder = new LinkageViewHolder();
                //一般的联动模式
                convertView = mInflater.inflate(R.layout.intelligent_linkage_list_item_hd, parent, false);
                linkageViewHolder = new LinkageViewHolder();
                convertView.setTag(linkageViewHolder);
                linkageViewHolder.name = (TextView) convertView.findViewById(R.id.securityNameTextView);
                linkageViewHolder.deviceTypeIconGridView = (GridView) convertView.findViewById(R.id.deviceTypeIconGridView);
                linkageViewHolder.activateImageView = (ImageView) convertView.findViewById(R.id.activateImageView);
                linkageViewHolder.action_tv = (TextView) convertView.findViewById(R.id.action_tv);
                linkageViewHolder.securityDeviceIconImageView = (ImageView) convertView.findViewById(R.id.securityDeviceIconImageView);
                linkageViewHolder.OfflineView = (OfflineView) convertView.findViewById(R.id.offline_view);
                linkageViewHolder.picImage = (ImageView) convertView.findViewById(R.id.linkage_item_pic);
                linkageViewHolder.arrowImage = (ImageView) convertView.findViewById(R.id.linkage_item_arrowImage);
                //  linkageViewHolder.tips = (TextView) convertView.findViewById(R.id.linkage_item_stopTips);

            } else {
                linkageViewHolder = (LinkageViewHolder) convertView.getTag();
            }
            Linkage linkage = smartScene.linkage;
            if (linkage != null) {
                if ((Constant.INVALID_NUM + "").equals(linkage.getLinkageId())) {
                    linkageViewHolder.OfflineView.setVisibility(View.VISIBLE);
                    linkageViewHolder.OfflineView.setBackgroundResource(R.drawable.bg_superscript_y);
                } else {
                    linkageViewHolder.OfflineView.setVisibility(View.GONE);
                }
            }

            linkageViewHolder.name.setText(linkage.getLinkageName());
            List<Integer> deviceTypeList = LibIntelligentSceneTool.getDeviceTypeList(linkage);
            /*DeviceIconsAdapter deviceIconsAdapter = new DeviceIconsAdapter(mContext,
                    deviceTypeList.size() > MAX_LINKAGE_DEVICE_ICON_COUNT ? deviceTypeList.subList(0, MAX_LINKAGE_DEVICE_ICON_COUNT) : deviceTypeList);*/
            DeviceIconsAdapter deviceIconsAdapter = new DeviceIconsAdapter(mContext,
                    deviceTypeList.size() > MAX_LINKAGE_DEVICE_ICON_COUNT ? deviceTypeList.subList(0, MAX_LINKAGE_DEVICE_ICON_COUNT) : deviceTypeList, smartScene);
            linkageViewHolder.deviceTypeIconGridView.setAdapter(deviceIconsAdapter);
            linkageViewHolder.deviceTypeIconGridView.setClickable(false);
            linkageViewHolder.deviceTypeIconGridView.setPressed(false);
            linkageViewHolder.deviceTypeIconGridView.setEnabled(false);
            /*linkageViewHolder.securityDeviceIconImageView.
                    setBackgroundResource(IntelligentSceneTool.getConditionIconResIdInList(IntelligentSceneTool.getConditionType(linkage), true));*/
            linkageViewHolder.activateImageView.setTag(R.id.tag_linkage, linkage);
            linkageViewHolder.activateImageView.setOnClickListener(clickListener);
            linkageViewHolder.activateImageView.setContentDescription(linkage.getIsPause() + "");
            //  convertView.setLayoutParams(lp);

            if (linkage.getIsPause() == LinkageActiveType.ACTIVE) {
                linkageViewHolder.activateImageView.setImageLevel(0);
            } else {
                linkageViewHolder.activateImageView.setImageLevel(1);
            }
            expandViewTouchDelegate(linkageViewHolder.action_tv, convertView);
            linkageViewHolder.action_tv.setTag(R.id.tag_linkage, linkage);
            linkageViewHolder.action_tv.setOnClickListener(clickListener);
            linkageViewHolder.action_tv.setContentDescription(linkage.getIsPause() + "");
            /*if (linkage.getIsPause() == LinkageActiveType.ACTIVE) {
                linkageViewHolder.action_tv.setBackgroundResource(R.drawable.btn_start_pressed);
                linkageViewHolder.action_tv.setText(R.string.intelligent_scene_stop);
            } else {
                linkageViewHolder.action_tv.setText(R.string.intelligent_scene_active);
                linkageViewHolder.action_tv.setBackgroundResource(R.drawable.btn_start);
            }*/
            // linkageViewHolder.action_tv.setText(linkage.getIsPause()==LinkageActiveType.ACTIVE?R.string.intelligent_scene_stop:R.string.intelligent_scene_active);
            linkageViewHolder.picImage.setImageResource(IntelligentSceneTool.getConditionIconResIdInList(IntelligentSceneTool.getConditionType(linkage), true));

            if (linkage.getIsPause() == LinkageActiveType.ACTIVE) {

                //激活按钮的设置
                linkageViewHolder.action_tv.setText(R.string.intelligent_scene_stop);
                linkageViewHolder.action_tv.setBackgroundResource(R.drawable.bt_yellow);
                // linkageViewHolder.action_tv.setTextColor(mContext.getResources().getColor(R.color.scene_yellow));
                //情景名字的设置
                linkageViewHolder.name.setText(linkage.getLinkageName());
                linkageViewHolder.name.setTextColor(mContext.getResources().getColor(R.color.scene_yellow));
                //图标的背景设置
                linkageViewHolder.picImage.setBackgroundResource(R.drawable.bg_bigcircle_yellow_normal);
                //安防图标的设置
                linkageViewHolder.securityDeviceIconImageView.
                        setBackgroundResource(IntelligentSceneTool.getConditionIconResIdInList2start(IntelligentSceneTool.getConditionType(linkage), true));
                //箭头图片的设置
                linkageViewHolder.arrowImage.setImageResource(R.drawable.icon_linkage_arrow_y);
                //停用提示的设置
                //  linkageViewHolder.tips.setVisibility(View.GONE);
                //在线、离线提示的设置
                //    linkageViewHolder.OfflineView.setBackgroundResource(R.drawable.bg_superscript_y);

            } else {

                //激活按钮的设置
                linkageViewHolder.action_tv.setText(R.string.intelligent_scene_active);
                //  linkageViewHolder.action_tv.setBackgroundResource(R.drawable.bt_gray);
                // linkageViewHolder.action_tv.setTextColor(mContext.getResources().getColor(R.color.scene_gray));
                //情景名字的设置
                linkageViewHolder.name.setText(linkage.getLinkageName());
                linkageViewHolder.name.setTextColor(mContext.getResources().getColor(R.color.gray));
                //图标背景的设置
                // linkageViewHolder.picImage.setBackgroundResource(R.drawable.bg_bigcircle);
                //箭头图片的设置
                linkageViewHolder.arrowImage.setImageResource(R.drawable.icon_linkage_arrow_gray);
                //安防图标的设置
                linkageViewHolder.securityDeviceIconImageView.
                        setBackgroundResource(IntelligentSceneTool.getConditionIconResIdInList2stop(IntelligentSceneTool.getConditionType(linkage), true));
                //停用提示的设置
                //  linkageViewHolder.tips.setVisibility(View.VISIBLE);
                //在线、离线提示的设置
                //    linkageViewHolder.OfflineView.setBackgroundResource(R.drawable.bg_superscript_gray);

            }
        } else if (itemType == SECURITY_ITEM) {
            //安防模式item
            final SecurityViewHolder securityViewHolder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.intelligent_security_list_item_hd, parent, false);
                securityViewHolder = new SecurityViewHolder();
                convertView.setTag(securityViewHolder);
                securityViewHolder.securityNameTextView = (TextView) convertView.findViewById(R.id.securityNameTextView);
                securityViewHolder.deviceTypeIconGridView = (GridView) convertView.findViewById(R.id.deviceTypeIconGridView);
                securityViewHolder.securityImageview = (ImageView) convertView.findViewById(R.id.securityImageview);
                securityViewHolder.delayTextView = (SecurityCountdownTextView) convertView.findViewById(R.id.delayTips);
                securityViewHolder.stopTipsTextView = (TextView) convertView.findViewById(R.id.security_item_stop_tips);
                securityViewHolder.deviceTypeIconGridView = (GridView) convertView.findViewById(R.id.deviceTypeIconGridView);
                securityViewHolder.securityTextView = (ProgressView) convertView.findViewById(R.id.securityTextView);
            } else {
                securityViewHolder = (SecurityViewHolder) convertView.getTag();
            }
            Security security = smartScene.security;
            // List<Integer> deviceTypeList = LibIntelligentSceneTool.getDeviceTypeList(security);
            //  List<Integer> sensorDeviceTypeList = LibIntelligentSceneTool.getSensorDeviceTypeList(mMainUid);
            List<Integer> sensorDeviceTypeList = LibIntelligentSceneTool.getSensorDeviceTypeList(security);
            final DeviceIconsAdapter deviceIconsAdapter = new DeviceIconsAdapter(mContext,
                    sensorDeviceTypeList.size() > MAX_SECUTIY_DEVICE_ICON_COUNT ?
                            sensorDeviceTypeList.subList(0, MAX_SECUTIY_DEVICE_ICON_COUNT) : sensorDeviceTypeList, smartScene);
            securityViewHolder.deviceTypeIconGridView.setAdapter(deviceIconsAdapter);
            securityViewHolder.deviceTypeIconGridView.setClickable(false);
            securityViewHolder.deviceTypeIconGridView.setPressed(false);
            securityViewHolder.deviceTypeIconGridView.setEnabled(false);
            securityViewHolder.securityNameTextView.setText(security.getSecurityName());
            expandViewTouchDelegate(securityViewHolder.securityTextView, convertView);
            securityViewHolder.securityTextView.setTag(R.id.tag_security, security);

            securityViewHolder.securityTextView.setOnClickListener(clickListener);
            securityViewHolder.securityTextView.setContentDescription(security.getIsArm() + "");
            securityViewHolder.securityImageview.setImageResource(R.drawable.icon_security);
            securityViewHolder.stopTipsTextView.setVisibility(View.GONE);
            securityViewHolder.delayTextView.registerSecurityCountdownFinishedListener(new SecurityCountdownTextView.OnSecurityCountdownFinishedListener() {
                @Override
                public void onSecurityCountdownFinished() {
                    //倒计时流程完整的走完
                    isFinish = true;
                    notifyDataSetChanged();
                    ToastUtil.showToast(mContext.getString(R.string.intelligent_security_success));
                    securityViewHolder.securityNameTextView.setTextColor(mContext.getResources().getColor(R.color.scene_red));
                }
            });
            securityViewHolder.securityTextView.setOnProgressFinishListener(new ProgressView.OnProgressFinishListener() {
                @Override
                public void onProgressFinish() {
                    isFinish = true;
                    securityViewHolder.securityNameTextView.setTextColor(mContext.getResources().getColor(R.color.scene_red));
                    deviceIconsAdapter.setIsCountdownStarted(false);//红色
                    securityViewHolder.delayTextView.setText("");
                    securityViewHolder.delayTextView.setVisibility(View.GONE);


                }
            });

//            if (isLeaveHomeArming) {
//                if (security.getSecType() == Security.AT_HOME) {
//                    security.setIsArm(Security.UNSECURITY);
//                } else if(security.getSecType() == Security.LEAVE_HOME){
//                    security.setIsArm(Security.SECURITY);
//                }
//            }

            if (security.getIsArm() == Security.SECURITY) {
                //点击布防延时一分钟后布防(一分钟后记得刷新状态)，计时期间可以撤防
                //点击【布防】，延迟1分钟布防，下方倒计时，布防成功以后， toast提示：布防成功
                securityViewHolder.securityTextView.setText(R.string.intelligent_security_stop);
                securityViewHolder.securityImageview.setBackgroundResource(R.drawable.bg_bigcircle_red_normal);
                //安防图标的设置
                if (security.getSecType() == Security.AT_HOME) {
                    securityViewHolder.securityImageview.setImageResource(R.drawable.icon_security_athome);
                    securityViewHolder.delayTextView.setVisibility(View.GONE);
                    securityViewHolder.securityNameTextView.setTextColor(mContext.getResources().getColor(R.color.scene_red));
                } else {
                    securityViewHolder.securityImageview.setImageResource(R.drawable.icon_security_leave);
                    securityViewHolder.delayTextView.setVisibility(View.GONE);
                    deviceIconsAdapter.setIsCountdownStarted(false);//红色
                    securityViewHolder.securityNameTextView.setTextColor(mContext.getResources().getColor(R.color.scene_red));

//                    Security securityTag = (Security) (securityViewHolder.securityTextView.getTag(R.id.securityTextView));
//                    boolean isEqualsSercurity = false;
//                    if (securityTag != null) {
//                        isEqualsSercurity = security.getSecurityId().equals(((Security) securityViewHolder.securityTextView.getTag(R.id.securityTextView)).getSecurityId());
//                    }
                    //开始倒计时时安防名字和安防设备的图标都显示灰色(倒计时中途切换了界面,但是还是在安防倒计时中)
                    if (securityViewHolder.delayTextView.isCountdownStarted()) {
                        securityViewHolder.securityNameTextView.setTextColor(mContext.getResources().getColor(R.color.gray));
                        deviceIconsAdapter.setIsCountdownStarted(true);//灰色
                        securityViewHolder.delayTextView.setVisibility(View.VISIBLE);
                    } else if ((Security) (securityViewHolder.securityTextView.getTag(R.id.securityTextView)) != null && !isFinish && security.getSecurityId().equals(((Security) securityViewHolder.securityTextView.getTag(R.id.securityTextView)).getSecurityId())) {
                        //动画是通过notifDatasetChange启动的，保证点击一次只启动一次倒计时
                        //@1 手动撤防后会把tag制空，1会影响到3，先设置了tag条件@3才成立
                        //@2 isFinish 因为界面会不定时刷新 （是为了防止倒计时结束后刷新界面又自动动画，因为目前已经是arm了）
                        //@3 securityId 目前没什么用（当前只有一个外出安防），目的是为了区分如果有多个外出安防时点击的是哪一个
                        deviceIconsAdapter.setIsCountdownStarted(true);//灰色
                        securityViewHolder.delayTextView.startCountdown(60);
                        securityViewHolder.securityTextView.startProgress(60);
                        securityViewHolder.delayTextView.setVisibility(View.VISIBLE);
                        securityViewHolder.securityNameTextView.setTextColor(mContext.getResources().getColor(R.color.gray));
                    }

                }
            } else {
                //撤防状态
                securityViewHolder.securityTextView.setText(R.string.intelligent_security_start);
                securityViewHolder.securityNameTextView.setTextColor(mContext.getResources().getColor(R.color.gray));
                securityViewHolder.securityImageview.setBackgroundResource(R.drawable.bg_bigcircle_red_normal);
                securityViewHolder.delayTextView.setVisibility(View.GONE);
                if (security.getSecType() == Security.AT_HOME) {
                    deviceIconsAdapter.setIsCountdownStarted(true);
                    securityViewHolder.securityImageview.setImageResource(R.drawable.icon_security_athome);
                } else {
                    securityViewHolder.securityImageview.setImageResource(R.drawable.icon_security_leave);
                    if (securityViewHolder.delayTextView.isCountdownStarted()) {
                        securityViewHolder.delayTextView.setText("");
                        securityViewHolder.delayTextView.stopCountdown();
                        deviceIconsAdapter.setIsCountdownStarted(true);
                        // 停止动画
                        securityViewHolder.securityTextView.cancleProgress();
                    }
                    //倒计时中途中止了（倒计时没有完整的走完）
                    isFinish = false;
                    securityViewHolder.securityTextView.setTag(R.id.securityTextView,null);
                }
            }
        }
        //  convertView.setBackgroundResource(DeviceTool.getIntelligentSceneItemBackgroundResource(position));
        return convertView;
    }

    public void setMainUid(String mainUid) {
        mMainUid = mainUid;
    }

//    @Override
//    public void onSecurityCountdownFinished() {
//        notifyDataSetChanged();
//        ToastUtil.showToast(mContext.getString(R.string.intelligent_security_success));
//
//    }

    /**
     * 扩大View的触摸和点击响应范围,最大不超过其父View范围
     * 子view的触摸区域需要父View来委派触摸
     *
     * @param view     需要扩大范围的view
     * @param itemView
     */
    public static void expandViewTouchDelegate(final View view, final View itemView) {

        ((View) view.getParent()).post(new Runnable() {
            @Override
            public void run() {
                itemView.measure(0, 0);
                int itemHeight = itemView.getMeasuredHeight();
                view.measure(0, 0);
                int viewHeight = view.getMeasuredHeight();
                int detaHeight = (itemHeight - viewHeight) / 2;
                int detaWidth = 50;
                Rect bounds = new Rect();
                view.setEnabled(true);
                view.getHitRect(bounds);
                //设定view的点击范围
                bounds.top -= detaHeight;
                bounds.bottom += detaHeight;
                bounds.left -= detaWidth;
                bounds.right += detaWidth;

                TouchDelegate touchDelegate = new TouchDelegate(bounds, view);

                if (View.class.isInstance(view.getParent())) {
                    ((View) view.getParent()).setTouchDelegate(touchDelegate);
                }
            }
        });
    }

    /**
     * 手动执行的场景下方放设备品类图标（按执行时间），最多放5个
     * 品类不超过5个时，有几类显示几个图标。
     * 品类超过5个时，显示前5个品类的图标。
     */
    class SceneViewHolder {
        private TextView name;
        private GridView deviceTypeIconGridView;
        private TextView startTextView;
        private OfflineView OfflineView;
    }

    /**
     * 联动执行的场景下方放设备品类图标（按执行时间），最多放3个
     * 品类不超过3个时，有几类显示几个图标。
     * 品类超过3个时，显示前3个品类的图标。
     */
    class LinkageViewHolder {
        private TextView name;
        private GridView deviceTypeIconGridView;
        private ImageView activateImageView;
        private TextView action_tv;
        private ImageView securityDeviceIconImageView;
        private OfflineView OfflineView;
        private ImageView picImage;
        private ImageView arrowImage;
        private TextView tips;
    }

    /**
     * 安防模式
     */
    class SecurityViewHolder {
        private ImageView securityImageview;
        private TextView securityNameTextView;
        private GridView deviceTypeIconGridView;
        private SecurityCountdownTextView delayTextView;
        private TextView stopTipsTextView;
        // private TextView securityTextView;
        private ProgressView securityTextView;
    }

}
