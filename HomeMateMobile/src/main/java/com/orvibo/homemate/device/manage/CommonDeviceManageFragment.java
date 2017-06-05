package com.orvibo.homemate.device.manage;

import com.smartgateway.app.R;
import com.orvibo.homemate.common.BaseFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class CommonDeviceManageFragment extends BaseFragment {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View parentView = inflater.inflate(
				R.layout.fragment_common_device_manage, container, false);
		return parentView;
	}
}
