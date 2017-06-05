package com.smartgateway.app.fragment.maintenance;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.smartgateway.app.R;
import com.smartgateway.app.Utils.Constants;
import com.smartgateway.app.Utils.DialogUtil;
import com.smartgateway.app.data.MaintenanceData;
import com.smartgateway.app.service.NetworkService.ErrorAction;
import com.smartgateway.app.data.model.Maintenance.Maintenance;
import com.smartgateway.app.data.model.User.ResponseError;
import com.smartgateway.app.fragment.state.MaintenanceStateHelper;
import com.smartgateway.app.fragment.state.StateHelper;
import com.smartgateway.app.service.NetworkService.RetrofitManager;
import com.squareup.picasso.Picasso;

import java.util.List;

import ru.johnlife.lifetools.activity.BaseMainActivity;
import ru.johnlife.lifetools.fragment.BaseAbstractFragment;
import ru.johnlife.lifetools.tools.Base64Bitmap;
import ru.johnlife.lifetools.util.BitmapUtil;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class MaintenanceDetailsFragment extends BaseAbstractFragment {

    private ImageView img;
    private TextView item;
    private TextView apartment;
    private TextView state;
    private Button rateMaintenance;

    private static MaintenanceStateHelper states;
    private MaintenanceData maintenance;

    private TextView description;
    private TextView type;
    private String token;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected String getTitle(Resources res) {
        return res.getString(R.string.fragment_maintenance);
    }

    @Override
    protected AppBarLayout getToolbar(LayoutInflater inflater, ViewGroup container) {
        return createToolbarFrom(R.layout.toolbar_small);
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_maintenance_details, container, false);

        Context context = view.getContext();
        if (null == states) {
            states = new MaintenanceStateHelper(context);
        }

        initView(view);
        getMaintenanceDetail();

        return view;
    }
    private void initView(View view){
        img = (ImageView) view.findViewById(R.id.imgMaintenance);
        item = (TextView) view.findViewById(R.id.item);
        apartment = (TextView) view.findViewById(R.id.apartment);
        description = (TextView) view.findViewById(R.id.description);
        state = (TextView) view.findViewById(R.id.state);
        type = (TextView) view.findViewById(R.id.type);
        rateMaintenance = (Button) view.findViewById(R.id.rate);

        rateMaintenance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SubmitMaintenanceFragment fragment = new SubmitMaintenanceFragment();
                fragment.setId(maintenance.getId());
                BaseMainActivity activity = (BaseMainActivity) getBaseActivity();
                activity.changeFragment(fragment, true);
            }
        });

        item.setText(maintenance.getItem());
        apartment.setText(maintenance.getWhere());

        StateHelper.StateData stateData = states.get(maintenance.getState());
        state.setBackgroundColor(stateData.getColor());
        state.setText(stateData.getName());
        state.setCompoundDrawablesWithIntrinsicBounds(stateData.getIcon(), 0 ,0 ,0);
        if (stateData.getName().equals("Completed")) {
            rateMaintenance.setVisibility(View.VISIBLE);
        } else {
            rateMaintenance.setVisibility(View.GONE);
        }
    }

    public void setItem(MaintenanceData item) {
        this.maintenance = item;
    }

    private void getMaintenanceDetail(){
        SharedPreferences preferences = getContext().getSharedPreferences(Constants.USER_DATA,
                Context.MODE_PRIVATE);
        token = preferences.getString(Constants.USER_TOKEN, "");
        final DialogUtil dialogUtil = new DialogUtil(getContext());
        dialogUtil.showProgressDialog();
        RetrofitManager.getMaintenanceApiService()
                .detail(token, String.valueOf(maintenance.getId()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Maintenance>() {
                    @Override
                    public void call(Maintenance maintenance) {
                        dialogUtil.dismissProgressDialog();

                        List<String> imageUrl = maintenance.getMaintenance().getImageUrl();
                        if (imageUrl != null && imageUrl.size() > 0) {
                            Log.d("MaintainceDetail", "loading from url " + imageUrl.get(0));
                            Picasso.with(getActivity()).load(imageUrl.get(0)).into(img);
                        }

                        description.setText(maintenance.getMaintenance().getDescription());
                        type.setText(maintenance.getMaintenance().getCategory());
                    }
                }, new ErrorAction() {
                    @Override
                    public void call(ResponseError error) {
                        dialogUtil.dismissProgressDialog();
                    }
                });
    }
}
