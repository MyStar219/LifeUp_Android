package com.orvibo.homemate.device.allone2.add;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hzy.tvmao.KookongSDK;
import com.hzy.tvmao.interf.IRequestResult;
import com.hzy.tvmao.ir.Device;
import com.kookong.app.data.BrandList;
import com.smartgateway.app.R;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.device.allone2.ActivityFinishEvent;
import com.orvibo.homemate.device.allone2.BaseAlloneControlActivity;
import com.orvibo.homemate.util.AlloneUtil;
import com.orvibo.homemate.util.AnimUtils;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.ClearEditText;
import com.orvibo.homemate.view.custom.NavigationGreenBar;
import com.orvibo.homemate.view.custom.sortlistview.PinyinComparator;
import com.orvibo.homemate.view.custom.sortlistview.SideBar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by yuwei on 2016/3/24.
 * 设备品牌列表界面
 */
public class DeviceBrandListActivity extends BaseAlloneControlActivity {

    private static String TAG = DeviceBrandListActivity.class.getSimpleName();

    /**
     * 设备品牌对象键值
     */
    public static final String BRAND_KEY = "brand_key";

    /**
     * 设备类型
     */
    private int device_type;
    private List<BrandList.Brand> deviceList;
    private List<BrandList.Brand> allDeviceList;
    private List<BrandList.Brand> commonDeviceList;//常见品牌列表

    private NavigationGreenBar nbTitle;
    private TextView cancelSearchTextView;
    private ClearEditText searchEditText;
    private RelativeLayout searchBoxCollapsedRelativeLayout;
    private TextView searchTextView;
    private TextView searchTextView2;

    private ListView device_lv;//设备列表控件
    private TextView toast_click_letter;//点钟字母显示TextView
    private SideBar sideBar;//右侧字母索引控件

    private PinyinComparator pingyinComparator;//根据拼音首字母来排序数据列表
    private DeviceBrandListAdapter deviceBrandListAdapter;//品牌列表适配器

    private TextView brandText;//常见品牌
    private int realType = Device.AC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_device_brand);
        initView();
        initData();
        initListener();
    }

    private void initView() {
        nbTitle = (NavigationGreenBar) findViewById(R.id.nbTitle);
        cancelSearchTextView = (TextView) findViewById(R.id.cancelSearchTextView);
        searchEditText = (ClearEditText) findViewById(R.id.searchEditText);
        searchBoxCollapsedRelativeLayout = (RelativeLayout) findViewById(R.id.searchBoxCollapsedRelativeLayout);
        searchTextView = (TextView) findViewById(R.id.searchTextView);
        searchTextView2 = (TextView) findViewById(R.id.searchTextView2);
        device_lv = (ListView) findViewById(R.id.device_lv);
        toast_click_letter = (TextView) findViewById(R.id.toast_click_letter);
        sideBar = (SideBar) findViewById(R.id.sideBar);
        sideBar.setTextView(toast_click_letter);
    }

    private void initData() {
        showDialog(null, getString(R.string.loading));
        device_type = getIntent().getIntExtra(IntentKey.DEVICE_ADD_TYPE, -1);
        realType = AlloneUtil.getKKDeviceType(device_type);
        String deviceName = AlloneUtil.getDeviceName(device_type);
        nbTitle.setText(getString(R.string.select_device_brand, deviceName));
        searchEditText.setHint(getString(R.string.search_device_brand, deviceName));
        searchTextView2.setText(getString(R.string.search_device_brand, deviceName));
        deviceList = new ArrayList<>();
        allDeviceList = new ArrayList<>();
        pingyinComparator = new PinyinComparator();
        deviceBrandListAdapter = new DeviceBrandListAdapter(DeviceBrandListActivity.this, deviceList, true);
        device_lv.addHeaderView(initHeadView(), null, false);
        device_lv.setAdapter(deviceBrandListAdapter);
        //获取设备的品牌列表
        KookongSDK.getBrandListFromNet(realType, new IRequestResult<BrandList>() {
            @Override
            public void onSuccess(String s, BrandList result) {
                device_lv.setVisibility(View.VISIBLE);
                //加载设备品牌成功
                deviceList = result.brandList;
                commonDeviceList = result.brandList.subList(0, 10);
                allDeviceList.addAll(commonDeviceList);
                Collections.sort(deviceList, pingyinComparator);
                allDeviceList.addAll(deviceList);
                deviceBrandListAdapter.updateListData(allDeviceList, true);
                dismissDialog();
            }

            @Override
            public void onFail(String msg) {
                //加载设备品牌失败
                ToastUtil.showToast(R.string.get_device_brand_fail);
                dismissDialog();
            }
        });
    }

    private void initListener() {
        searchTextView.setOnClickListener(this);
        cancelSearchTextView.setOnClickListener(this);

        //根据输入框输入值的改变来过滤搜索
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterData(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        //设置侧栏A-Z导航的触摸事件
        sideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                //定位到字母首次出现的位置
                int position = deviceBrandListAdapter.getPositionForSection(s.charAt(0));
                if (position != -1)
                    device_lv.setSelection(position + 1);
            }
        });

        searchEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    AnimUtils.showInputMethod(v);
                else
                    AnimUtils.hideInputMethod(v);
            }
        });

        device_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                deviceBrand = (BrandList.Brand) deviceBrandListAdapter.getItem(position - 1);
                loadIrData(realType, deviceBrand.brandId, null, 0, deviceBrand);
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.searchTextView:
                changeState();
                break;
        }
    }


    /**
     * 改变输入框状态
     */
    private void changeState() {
        if (searchEditText.isFocused()) {
            searchBoxCollapsedRelativeLayout.setVisibility(View.VISIBLE);
        } else {
            searchEditText.requestFocus();
            searchBoxCollapsedRelativeLayout.setVisibility(View.GONE);
        }
    }

    /**
     * 根据输入框中的值来过滤数据并更新ListView
     *
     * @param filterStr
     */
    private void filterData(String filterStr) {
        List<BrandList.Brand> filterDateList = new ArrayList<BrandList.Brand>();
        //搜索内容为空，还原数据列表；非空则进行筛选
        if (TextUtils.isEmpty(filterStr)) {
            filterDateList = allDeviceList;
            brandText.setText(getString(R.string.allone_common_brand));
        } else {
            filterDateList.clear();
            for (BrandList.Brand deviceBrand : deviceList) {
                if (deviceBrand.cname.indexOf(filterStr.toString()) != -1 || deviceBrand.ename.toLowerCase().indexOf(filterStr.toString().toLowerCase()) != -1) {
                    filterDateList.add(deviceBrand);
                }
            }
            brandText.setText(getString(R.string.allone_brand));
            // 根据a-z进行排序
            Collections.sort(filterDateList, pingyinComparator);
        }
        deviceBrandListAdapter.updateListData(filterDateList, TextUtils.isEmpty(filterStr));
    }


    private View initHeadView() {
        View view = getLayoutInflater().inflate(R.layout.item_allone_brand_headview, null);
        brandText = (TextView) view.findViewById(R.id.brandText);
        return view;
    }

    /**
     * 添加遥控器成功后finish掉添加的activity
     * @param event
     */
    public void onEventMainThread(ActivityFinishEvent event) {
        finish();
    }

}
