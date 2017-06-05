package com.orvibo.homemate.device.manage.add;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.orvibo.homemate.application.ViHomeApplication;
import com.orvibo.homemate.bo.DeviceDesc;
import com.orvibo.homemate.bo.DeviceLanguage;
import com.orvibo.homemate.bo.DeviceQueryUnbind;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.common.MainActivity;
import com.orvibo.homemate.core.product.ProductManage;
import com.orvibo.homemate.dao.DeviceDescDao;
import com.orvibo.homemate.dao.DeviceLanguageDao;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.ErrorMessage;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.LoginStatus;
import com.orvibo.homemate.model.DeviceBind;
import com.orvibo.homemate.model.DeviceUnbind;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.ClickUtil;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.NetUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.DialogFragmentOneButton;
import com.orvibo.homemate.view.custom.NavigationTextBar;
import com.smartgateway.app.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by allen on 2015/11/5.
 * updated by huangqiyao on 2015/11/5.
 * updated by zhoubaoqi on 2016/8/19
 * 支持添加主机。
 * 不需要先解绑再绑定
 */
public class AddUnbindDeviceActivity extends BaseActivity {
    private final static String TAG = AddUnbindDeviceActivity.class.getSimpleName();
    private Button nextButton;
    private DeviceBind deviceBind;
    //    private Load load;
    private List<String> bindUids;
    //    private List<String> loadUids;
    private boolean hasBindSuccess;
    private View onlyOneWifiDeviceView;
    private ListView mUnbindDeviceListView;
    private ImageView mProductImageView;
    private TextView mProductNameTextView;
    private TextView mCompanyNameTextView;
    private UnBindWifiDeviceAdapter mUnBindWifiDeviceAdapter;
    private Context context;
    private List<DeviceQueryUnbind> deviceQueryUnbinds = new ArrayList<DeviceQueryUnbind>();
    private NavigationTextBar mNavigationTextBar;

    private DeviceUnbind mDeviceUnbind;
    private ConcurrentHashMap<String, Integer> bindHashMap_type = new ConcurrentHashMap<String, Integer>();
    private ConcurrentHashMap<String, DeviceQueryUnbind> bindHashMap_device = new ConcurrentHashMap<String, DeviceQueryUnbind>();
    private final static int WHAT_BIND_LIST = 0;
    private final static int WHAT_BIND_ONE = 1;
    private final static String BIND_KEY = "bind";
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            Bundle data = msg.getData();
            DeviceQueryUnbind deviceQueryUnbind = (DeviceQueryUnbind) data.getSerializable(BIND_KEY);
            switch (what) {
                case WHAT_BIND_LIST:

                    if (deviceQueryUnbind != null && bindHashMap_device.get(deviceQueryUnbind.getUid()) == null) {
                        bindHashMap_device.put(deviceQueryUnbind.getUid(), deviceQueryUnbind);
                        LogUtil.d(TAG, "handleMessage():toBindDevice:uid=" + deviceQueryUnbind.getUid());
                        toBindDevice(deviceQueryUnbind);
                    }
                    break;

            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_unbind_device_list);
        context = this;
        init();
        initView();
        initDate();
        initEvent();

    }

    private void initEvent() {
        nextButton.setOnClickListener(this);
    }

    private void initDate() {

        Intent intent = getIntent();
        deviceQueryUnbinds = (List<DeviceQueryUnbind>) intent.getSerializableExtra(IntentKey.DEVICE);

        if (deviceQueryUnbinds != null && deviceQueryUnbinds.size() == 1) {
            ((ViewGroup) mUnbindDeviceListView.getParent()).removeView(onlyOneWifiDeviceView);
            ((ViewGroup) mUnbindDeviceListView.getParent()).addView(onlyOneWifiDeviceView);
            mUnbindDeviceListView.setEmptyView(onlyOneWifiDeviceView);
            // 显示设备的信息
            DeviceQueryUnbind deviceQueryUnbind = deviceQueryUnbinds.get(0);
            String model = deviceQueryUnbind.getModel();
            String language = getResources().getConfiguration().locale.getLanguage();
            DeviceDesc deviceDesc = new DeviceDescDao().selDeviceDesc(model);
            if (deviceDesc != null) {
                String picUrl = deviceDesc.getPicUrl();
                ImageLoader.getInstance().displayImage(picUrl, mProductImageView, ViHomeApplication.getImageOptions());
                DeviceLanguage deviceLanguage = new DeviceLanguageDao().selDeviceLanguage(deviceDesc.getDeviceDescId(), language);
                if (deviceLanguage != null) {
                    String manufacturer = deviceLanguage.getManufacturer();
                    String productName = deviceLanguage.getProductName();
                    mCompanyNameTextView.setText(manufacturer);
                    mProductNameTextView.setText(productName);
                }
            }

        } else {
            if (onlyOneWifiDeviceView != null) {
                ((ViewGroup) mUnbindDeviceListView.getParent()).removeView(onlyOneWifiDeviceView);
                refresh();
            }
        }

    }

    private void refresh() {
        if (mUnBindWifiDeviceAdapter == null) {
            mUnBindWifiDeviceAdapter = new UnBindWifiDeviceAdapter();
            mUnbindDeviceListView.setAdapter(mUnBindWifiDeviceAdapter);
        } else {
            mUnBindWifiDeviceAdapter.notifyDataSetChanged();
        }


    }

    private void initView() {
        mUnbindDeviceListView = (ListView) findViewById(R.id.unbindDeviceListView);
        onlyOneWifiDeviceView = LayoutInflater.from(mContext).inflate(
                R.layout.activity_add_unbind_device, mUnbindDeviceListView, false);
        onlyOneWifiDeviceView.findViewById(R.id.nbTitle).setVisibility(View.GONE);
        nextButton = (Button) onlyOneWifiDeviceView.findViewById(R.id.nextButton);
        mProductImageView = (ImageView) onlyOneWifiDeviceView.findViewById(R.id.productImageView);
        mProductNameTextView = (TextView) onlyOneWifiDeviceView.findViewById(R.id.productNameTextView);
        mCompanyNameTextView = (TextView) onlyOneWifiDeviceView.findViewById(R.id.companyNameTextView);

        mNavigationTextBar = (NavigationTextBar) findViewById(R.id.list_nbTitle);
        //  mNavigationTextBar.setRightTextVisibility(View.GONE);
        mNavigationTextBar.setRightTextViewVisiblity(View.INVISIBLE);
    }

    private void init() {

        bindUids = new ArrayList<String>();
//        loadUids = new ArrayList<String>();
        initDeviceBind();
//        initLoad();
    }

    private void initDeviceBind() {
        deviceBind = new DeviceBind() {
            @Override
            public void onBindResult(String uid, int serial, int result) {
                super.onBindResult(uid, serial, result);
                bindResult(uid, result);

            }
//                if (result == ErrorCode.SUCCESS) {
//                    hasBindSuccess = true;
//                }
//                bindUids.remove(uid);
//                if (bindUids.isEmpty()) {
//                    dismissDialog();
//                    if (hasBindSuccess) {
//                        //  toMainActivity();
//                        // 刷新界面，设备添加成功，展现成功标示。右上角出现完成按钮
//                        mNavigationGreenBar.setRightTextVisibility(View.INVISIBLE);
//                        refresh();
//                    } else if (result == ErrorCode.DEVICE_HAS_BIND) {
//                        DialogFragmentOneButton dialogFragmentOneButton = new DialogFragmentOneButton();
//                        dialogFragmentOneButton.setTitle(getString(R.string.add_device_fail_title));
//                        dialogFragmentOneButton.setContent(getString(R.string.DEVICE_HAS_BIND));
//                        dialogFragmentOneButton.setButtonText(getString(R.string.confirm));
//                        dialogFragmentOneButton.setOnButtonClickListener(new DialogFragmentOneButton.OnButtonClickListener() {
//                            @Override
//                            public void onButtonClick(View view) {
//                                finish();
//                            }
//                        });
//                        dialogFragmentOneButton.show(getFragmentManager(), "");
//                    } else {
//                        ToastUtil.showToast(ErrorMessage.getError(mAppContext, result));
//                    }
//                } else {
//                    deviceBind.bind(mAppContext, bindUids.get(0), DeviceType.COCO + "");
//                }
//            }
        };

//        /**
//         *   先解绑.在绑定
//         */
//        mDeviceUnbind = new DeviceUnbind() {
//            @Override
//            public void onUnbindResult(final String uid, int serial, int result) {
//
//                if (result == ErrorCode.SUCCESS) {
//                    Integer deviceType = bindHashMap_type.get(uid);
//                    deviceBind.bind(mAppContext, uid, deviceType + "");
//                    Log.i(TAG, uid + "解绑365成功");
//                } else {
//                    ToastUtil.showToast(getString(R.string.unbind_add_wifi_device_fail));
//                }
//            }
//
//        };
    }

    private void bindResult(String uid, int result) {
        if (result == ErrorCode.SUCCESS) {
            hasBindSuccess = true;
            LogUtil.d(TAG, "uid=" + uid + " 365绑定成功");
        } else {
            LogUtil.d(TAG, "uid=" + uid + " 365绑定失败");
          //  ToastUtil.showToast(getString(R.string.unbind_add_wifi_device_fail));
            bindHashMap_device.remove(uid);
            mHandler.removeMessages(WHAT_BIND_ONE);
        }
        stopProgress();
        //  dismissDialog();
        if (hasBindSuccess) {

            //  toMainActivity();
            // 刷新界面，设备添加成功，展现成功标示。右上角出现完成按钮
            mNavigationTextBar.setRightTextViewVisiblity(View.VISIBLE);
            mNavigationTextBar.setRightText(getString(R.string.unbind_add_wifi_device_finish));

            if (deviceQueryUnbinds != null && deviceQueryUnbinds.size() > 1) {
//                if (bindDeviceQueryUnbind != null) {
//                    bindDeviceQueryUnbind.setAdded(true);
//                }
                if (bindHashMap_device.get(uid) != null) {
                    bindHashMap_device.get(uid).setAdded(true);
                }
                refresh();
            } else if (deviceQueryUnbinds != null && deviceQueryUnbinds.size() == 1) {
                toMainActivity();
            }
        } else if (result == ErrorCode.DEVICE_HAS_BIND) {
            DialogFragmentOneButton dialogFragmentOneButton = new DialogFragmentOneButton();
            dialogFragmentOneButton.setTitle(getString(R.string.add_device_fail_title));
            dialogFragmentOneButton.setContent(getString(R.string.DEVICE_HAS_BIND));
            dialogFragmentOneButton.setButtonText(getString(R.string.confirm));
            dialogFragmentOneButton.setOnButtonClickListener(new DialogFragmentOneButton.OnButtonClickListener() {
                @Override
                public void onButtonClick(View view) {
                    if (deviceQueryUnbinds != null && deviceQueryUnbinds.size() > 1) {
                        dismissDialog();
                    } else {
                        finish();
                    }
                }
            });
            dialogFragmentOneButton.show(getFragmentManager(), "");
        } else {
            ToastUtil.showToast(ErrorMessage.getError(mAppContext, result));
        }
    }

    private void toMainActivity() {
        Intent intent = new Intent(mAppContext, MainActivity.class);
        startActivity(intent);
        finish();
    }

//    private void initLoad() {
//        load = new Load(mAppContext);
//        Load.OnLoadListener onLoadListener = new Load.OnLoadListener() {
//            @Override
//            public void onLoadFinish(final String uid, int result) {
//                if (result == ErrorCode.SUCCESS) {
//                    hasLoadSuccess = true;
//                }
//                loadUids.remove(uid);
//                if (loadUids.isEmpty()) {
//                    if (!hasLoadSuccess){
//                        dismissDialog();
//                        ToastUtil.showToast(ErrorMessage.getError(mAppContext, result));
//                    } else {
//                        Intent intent = new Intent(mAppContext, MainActivity.class);
//                        startActivity(intent);
//                        finish();
//                    }
//                } else {
//                    load.load(loadUids.get(0));
//                }
//            }
//
//        };
//        load.setOnLoadListener(onLoadListener);
//    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        // toBindDevice(deviceQueryUnbinds.get(0));
        if (!NetUtil.isNetworkEnable(this)) {
            ToastUtil.showToast(getString(R.string.NET_DISCONNECT));
            return;
        }
        Message message = mHandler.obtainMessage();
        message.what = WHAT_BIND_LIST;
        message.getData().putSerializable(BIND_KEY, deviceQueryUnbinds.get(0));
        mHandler.sendMessageDelayed(message, 0);
    }

    @Override
    public void rightTitleClick(View view) {
        toMainActivity();
    }

    public void leftTitleClick(View view) {
        finish();
    }

    /**
     * 绑定设备
     *
     * @param deviceQueryUnbind
     */
    private void toBindDevice(DeviceQueryUnbind deviceQueryUnbind) {
        int status = UserCache.getLoginStatus(mAppContext, UserCache.getCurrentUserName(mAppContext));
        if (status == LoginStatus.SUCCESS) {
            //  showDialog(null, getString(R.string.ap_config_add_device));
            showProgress();
//            for (DeviceQueryUnbind deviceQueryUnbind : deviceQueryUnbinds) {
//                bindUids.add(deviceQueryUnbind.getUid());
//            }
//            deviceBind.bind(mAppContext, bindUids.get(0), DeviceType.COCO + "");
            bindUids.add(0, deviceQueryUnbind.getUid());
            bindHashMap_type.put(deviceQueryUnbind.getUid(), getWifiDeviceType(deviceQueryUnbind));
            int deviceType = getWifiDeviceType(deviceQueryUnbind);
            if (deviceType != DeviceType.CAMERA) {
                //绑定
                //  mDeviceUnbind.unBind(mAppContext, deviceQueryUnbind.getUid());
                deviceBind.bind(mAppContext, deviceQueryUnbind.getUid());
            } else {

            }
        } else {
            showLoginDialog();
        }
    }

    /**
     * 局域网没有绑定设备的适配器
     */
    private class UnBindWifiDeviceAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (deviceQueryUnbinds != null) {
                return deviceQueryUnbinds.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            if (deviceQueryUnbinds != null) {
                return deviceQueryUnbinds.get(position);
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = View.inflate(AddUnbindDeviceActivity.this, R.layout.item_unbind_device, null);
                viewHolder.deviceImageView = (ImageView) convertView.findViewById(R.id.deviceImageView);
                viewHolder.productNameTextView = (TextView) convertView.findViewById(R.id.productNameTextView);
                viewHolder.productMacTextView = (TextView) convertView.findViewById(R.id.productModelTextView);
                viewHolder.addDeviceTextView = (TextView) convertView.findViewById(R.id.addDeviceTextView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            final DeviceQueryUnbind deviceQueryUnbind = (DeviceQueryUnbind) getItem(position);
            if (deviceQueryUnbind != null) {
                viewHolder.deviceImageView.setImageResource(getWifiDeviceImageViewResID(deviceQueryUnbind));
                viewHolder.productNameTextView.setText(getWifiDeviceName(deviceQueryUnbind));
                viewHolder.productMacTextView.setText(getWifiDeviceMacAdress(deviceQueryUnbind));
                if (deviceQueryUnbind.isAdded()) {
                    //  viewHolder.addDeviceTextView.setText(getString(R.string.unbind_add_wifi_device_add_successs));
                    viewHolder.addDeviceTextView.setText("");
                    viewHolder.addDeviceTextView.setBackgroundResource(R.drawable.icon_success);
                } else {
                    viewHolder.addDeviceTextView.setText(getString(R.string.unbind_add_wifi_device_add));
                    viewHolder.addDeviceTextView.setBackgroundResource(R.drawable.unbind_bt_select);
                    viewHolder.addDeviceTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if (NetUtil.isNetworkEnable(AddUnbindDeviceActivity.this)) {
                                if (!deviceQueryUnbind.isAdded()) {
                                    //如果绑定成功后又去绑定会返回ErrorCode=30，提示我去加载数据
                                    if (!ClickUtil.isFastDoubleClick()) {
                                        Message message = mHandler.obtainMessage();
                                        message.what = WHAT_BIND_LIST;
                                        message.getData().putSerializable(BIND_KEY, deviceQueryUnbind);
                                        mHandler.sendMessageDelayed(message, 0);
                                        // toBindDevice(deviceQueryUnbind);
                                    }
                                } else {
                                    return;
                                }
                            } else {
                                ToastUtil.showToast(getString(R.string.NET_DISCONNECT));
                            }
                        }
                    });
                }
            }
            return convertView;
        }

        private class ViewHolder {
            private ImageView deviceImageView;
            private TextView productNameTextView;
            private TextView productMacTextView;
            private TextView addDeviceTextView;
        }
    }


    /**
     * 根据未绑定的设备获取wifi设备名称
     *
     * @param deviceQueryUnbind
     * @return
     */
    public String getWifiDeviceName(DeviceQueryUnbind deviceQueryUnbind) {

        String deviceName = "";
        if (deviceQueryUnbind != null) {
            String model = deviceQueryUnbind.getModel();

            if (ProductManage.getInstance().isOrviboCOCO(model)) {
                deviceName = getString(R.string.device_type_COCO_43);
            } else if (ProductManage.getInstance().isS20orS25(model)) {
                deviceName = getString(R.string.device_add_s20c);
            } else if (ProductManage.getInstance().isAllone2(model)) {
                deviceName = getString(R.string.device_add_xiaofang_tv);
            } else if (ProductManage.getInstance().isLiangBa(model)) {
                deviceName = getString(R.string.device_add_liangba);
            } else if (ProductManage.getInstance().isAoKe(model)) {
                deviceName = getString(R.string.device_add_aoke_liangyi);
            } else if (ProductManage.getInstance().isOuJia(model)) {
                deviceName = getString(R.string.device_add_oujia);
            } else if (ProductManage.getInstance().isMaiRun(model)) {
                deviceName = getString(R.string.device_add_mairunclothes);
            } else if (ProductManage.getInstance().isMethanal(model)) {

            } else if (ProductManage.getInstance().isCo(model)) {

            } else if (ProductManage.getInstance().isXiaoOuCamera(model)) {

                deviceName = getString(R.string.xiao_ou_camera);
            }

            String language = getResources().getConfiguration().locale.getLanguage();
            DeviceDesc deviceDesc = new DeviceDescDao().selDeviceDesc(model);
            if (deviceDesc != null) {
                DeviceLanguage deviceLanguage = new DeviceLanguageDao().selDeviceLanguage(deviceDesc.getDeviceDescId(), language);
                if (deviceLanguage != null) {
                    deviceName = deviceLanguage.getProductName();
                    return deviceName;
                }
            }

        }
        return deviceName;

    }

    /**
     * 根据未绑定的设备获取wifi设备的图片id
     *
     * @param deviceQueryUnbind
     * @return
     */
    public int getWifiDeviceImageViewResID(DeviceQueryUnbind deviceQueryUnbind) {
        int picResID = 0;
        if (deviceQueryUnbind != null) {
            String model = deviceQueryUnbind.getModel();
            if (ProductManage.getInstance().isOrviboCOCO(model)) {
                picResID = R.drawable.icon_bg_coco;
            } else if (ProductManage.getInstance().isS20orS25(model)) {
                picResID = R.drawable.icon_bg_lianrong_s20;
            } else if (ProductManage.getInstance().isAllone2(model)) {
                picResID = R.drawable.device_500_allone2;
            } else if (ProductManage.getInstance().isLiangBa(model)) {
                picResID = R.drawable.icon_bg_liangba;
            } else if (ProductManage.getInstance().isAoKe(model)) {
                picResID = R.drawable.icon_bg_aoke_liangyi;
            } else if (ProductManage.getInstance().isOuJia(model)) {
                picResID = R.drawable.icon_bg_zicheng;
            } else if (ProductManage.getInstance().isMaiRun(model)) {
                picResID = R.drawable.device_500_mairun;
            } else if (ProductManage.getInstance().isMethanal(model)) {
                //  picResID = R.drawable.icon_bg_coco;
            } else if (ProductManage.getInstance().isCo(model)) {
                //   picResID = R.drawable.icon_bg_coco;
            } else if (ProductManage.getInstance().isXiaoOuCamera(model)) {
                picResID = R.drawable.device_120_xiaoou;
            } else if (ProductManage.getInstance().isXiaoESmartSocket(model)) {
                picResID = R.drawable.icon_bg_feidiao_xiaoe;
            } else if (ProductManage.getInstance().isYDSmartSocket(model)) {
                picResID = R.drawable.icon_bg_yidong_s;
            } else if (ProductManage.getInstance().isYDSmartSocket_S31(model)) {
                picResID = R.drawable.device_500_s31_black1;
            } else if (ProductManage.getInstance().isS31SmartSocket(model)) {
                picResID = R.drawable.device_500_s31_black1;
            }

        }
        return picResID;
    }

    /**
     * 根据未绑定的设备获取wifi设备的macadress
     *
     * @param deviceQueryUnbind
     * @return
     */
    public String getWifiDeviceMacAdress(DeviceQueryUnbind deviceQueryUnbind) {
        String uid = deviceQueryUnbind.getUid();
        String mac = toMac(uid.replaceAll("202020202020", ""));
        return mac;
    }

    /**
     * 把设备的uid转换成AA:BB:CC:DD格式
     *
     * @param mac
     * @return
     */
    private String toMac(String mac) {
        return mac.substring(0, 2).toUpperCase() + ":" + mac.substring(2, 4).toUpperCase() + ":" + mac.substring(4, 6).toUpperCase() + ":" + mac.substring(6, 8).toUpperCase() + ":" + mac.substring(8, 10).toUpperCase() + ":" + mac.substring(10, 12).toUpperCase();
    }

    /**
     * 根据未绑定的设备获取wifi设备的类型
     *
     * @param deviceQueryUnbind
     * @return
     */
    public int getWifiDeviceType(DeviceQueryUnbind deviceQueryUnbind) {
        int deviceType = 0;
        if (deviceQueryUnbind != null) {
            String model = deviceQueryUnbind.getModel();

            if (ProductManage.getInstance().isOrviboCOCO(model)) {
                deviceType = DeviceType.COCO;
            } else if (ProductManage.getInstance().isS20orS25(model) || ProductManage.getInstance().isYDSmartSocket(model) || ProductManage.getInstance().isYDSmartSocket_S31(model) || ProductManage.getInstance().isS31SmartSocket(model) || ProductManage.getInstance().isXiaoESmartSocket(model)) {
                deviceType = DeviceType.S20;
            } else if (ProductManage.getInstance().isAllone2(model)) {
                deviceType = DeviceType.ALLONE;
            } else if (ProductManage.getInstance().isLiangBa(model)) {
                deviceType = DeviceType.CLOTHE_SHORSE;
            } else if (ProductManage.getInstance().isAoKe(model)) {
                deviceType = DeviceType.CLOTHE_SHORSE;
            } else if (ProductManage.getInstance().isOuJia(model)) {
                deviceType = DeviceType.CLOTHE_SHORSE;
            } else if (ProductManage.getInstance().isMaiRun(model)) {
                deviceType = DeviceType.CLOTHE_SHORSE;
            } else if (ProductManage.getInstance().isMethanal(model)) {

            } else if (ProductManage.getInstance().isCo(model)) {

            } else if (ProductManage.getInstance().isXiaoOuCamera(model)) {
                deviceType = DeviceType.CAMERA;
            }

        }
        return deviceType;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
    }
}
