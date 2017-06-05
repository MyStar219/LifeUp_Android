package com.orvibo.homemate.common;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.evideo.weiju.WeijuManage;
import com.evideo.weiju.callback.CommandCallback;
import com.evideo.weiju.command.unlock.CodeUnlockCommand;
import com.evideo.weiju.info.CommandError;
import com.smartgateway.app.R;
import com.smartgateway.app.WeijuHelper;
import com.smartgateway.app.weiju.ScanCaptureActivity;

import ru.johnlife.lifetools.fragment.BaseAbstractFragment;
import ru.johnlife.lifetools.fragment.PagerFragment;

/**
 * SmartCommunityFragment
 * Created by MDev on 12/14/16.
 */

public class SmartCommunityFragment extends PagerFragment implements View.OnClickListener {
    protected static class SmartCommunityListFactory implements FragmentFactory {
        int mode;

        public SmartCommunityListFactory(int mode) {
            this.mode = mode;
        }

        @Override
        public BaseAbstractFragment createFragment() {
            switch (mode) {
                case 0:
                    return new ServicesFragment();
                default:
                    return new HistoryFragment();
            }
        }
    }

    @Override
    protected TabDescriptor[] getTabDescriptors() {
        return new TabDescriptor[]{
                new TabDescriptor(R.string.services, new SmartCommunityListFactory(0)),
                new TabDescriptor(R.string.history, new SmartCommunityListFactory(1)),
        };
    }

    @Override
    protected String getTitle(Resources res) {
        return "Smart Community";
    }

//    BottomTabView btnDashboard, btnHome, btnPersonal;

    private void initView(View view) {
//        btnDashboard = (BottomTabView) view.findViewById(R.id.btnDashboard);
//        btnDashboard.setOnClickListener(this);
//        btnDashboard.setActive(false);
//        btnDashboard.setText(R.string.ic_dashboard, R.string.txt_dashboard);
//
//        btnHome = (BottomTabView) view.findViewById(R.id.btnHome);
//        btnHome.setOnClickListener(this);
//        btnHome.setActive(false);
//        btnHome.setText(R.string.ic_home, R.string.txt_home);
//
//        btnPersonal = (BottomTabView) view.findViewById(R.id.btnPersonal);
//        btnPersonal.setOnClickListener(this);
//        btnPersonal.setActive(true);
//        btnPersonal.setText(R.string.ic_personal, R.string.txt_personal);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_barcode, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_qrCode:
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    Log.v("OS6-Permission-Camera", "Not Granted");

                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, 1);
                } else {
                    gotoFacialRecognition();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null){
            if (requestCode == ScanCaptureActivity.UNLOCK_BY_QCODE){
                Bundle bundle = data.getExtras();
                if (bundle != null) {
                    String result = bundle.getString("result");
                    if (result != null && !TextUtils.isEmpty(result)){
                        String type = result.substring(result.length()-1);
                        if (type.equals("2")){
                            int strBegin = result.indexOf('=');
                            int strEnd = result.lastIndexOf('&');
                            String str = result.substring(strBegin+1,strEnd);
                            CodeUnlockCommand codeUnlockCommand = new CodeUnlockCommand(getContext(),str, WeijuHelper.getApartmentInfo().getId());
                            codeUnlockCommand.setCallback(new CommandCallback() {
                                @Override
                                public void onSuccess() {
//                                    Toast.makeText(getContext(), "扫码开锁成功", Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void onFailure(CommandError error) {
//                                    Toast.makeText(getContext(), "扫码开锁失败 错误码 " + error.getStatus() + " message:" + error.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                            WeijuManage.execute(codeUnlockCommand);
                        }
                    }
                }
            }
        }
    }
    private void gotoFacialRecognition() {
        Intent intent = new Intent(getContext(), ScanCaptureActivity.class);
        startActivityForResult(intent,ScanCaptureActivity.UNLOCK_BY_QCODE);

//        ApartmentInfo item = WeijuHelper.;
//        Intent intent = new Intent(getApplicationContext(), ApartmentDetailActivity.class);
//        intent.putExtra("apartment", item);
//        startActivity(intent);

    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.createView(inflater, container, savedInstanceState);
        initView(view);
        return view;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_smart_community;
    }

    @Override
    protected AppBarLayout getToolbar(LayoutInflater inflater, ViewGroup container) {
        AppBarLayout value = createToolbarFrom(R.layout.toolbar_main_white);
        setTabLayout((TabLayout) value.findViewById(ru.johnlife.lifetools.R.id.tabs));
        return value;
    }

    @Override
    public void onClick(View view) {
//        btnDashboard.setActive(false);
//        btnHome.setActive(false);
//        btnPersonal.setActive(false);
//        switch (view.getId()) {
//            case R.id.btnDashboard:
//                btnDashboard.setActive(true);
//                break;
//            case R.id.btnHome:
//                btnHome.setActive(true);
//                break;
//            case R.id.btnPersonal:
//                btnPersonal.setActive(true);
//                break;
//        }
    }
}
