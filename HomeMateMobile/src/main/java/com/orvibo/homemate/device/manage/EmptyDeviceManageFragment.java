package com.orvibo.homemate.device.manage;

import com.smartgateway.app.R;
import com.orvibo.homemate.common.BaseFragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class EmptyDeviceManageFragment extends BaseFragment {
	OnAddDeviceListener mOnAddDeviceListener;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mOnAddDeviceListener = (OnAddDeviceListener) activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View parentView = inflater.inflate(
				R.layout.fragment_empty_device_manage, container, false);
		parentView.findViewById(R.id.addDevice_tv).setOnClickListener(this);
		return parentView;
	}

	@Override
	public void onClick(View v) {
		if (mOnAddDeviceListener != null) {
			mOnAddDeviceListener.onAddDevice();
		}
	}

	public interface OnAddDeviceListener {
		void onAddDevice();
	}

}
