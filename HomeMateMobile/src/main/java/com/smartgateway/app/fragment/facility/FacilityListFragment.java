package com.smartgateway.app.fragment.facility;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.smartgateway.app.R;
import com.smartgateway.app.Utils.Constants;
import com.smartgateway.app.Utils.DialogUtil;
import com.smartgateway.app.data.FacilityData;
import com.smartgateway.app.data.FacilityVariantData;
import com.smartgateway.app.data.model.Facility.Facilities;
import com.smartgateway.app.data.model.Facility.Type;
import com.smartgateway.app.service.NetworkService.RetrofitManager;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import ru.johnlife.lifetools.activity.BaseMainActivity;
import ru.johnlife.lifetools.adapter.BaseAdapter;
import ru.johnlife.lifetools.fragment.BaseListFragment;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by yanyu on 5/13/2016.
 */
public class FacilityListFragment extends BaseListFragment<FacilityData> {
    private Observer<Type> observer;
    private Observer<Facilities> facilitiesObserver;
    private List<FacilityData> list = new ArrayList<>();
    private Realm realm;

    DialogUtil dialogUtil;

    private String token;
    @Override
    protected BaseAdapter<FacilityData> instantiateAdapter(Context context) {
        return new BaseAdapter<FacilityData>(R.layout.item_facility) {
            @Override
            protected ViewHolder<FacilityData> createViewHolder(final View view) {
                return new ViewHolder<FacilityData>(view) {
                    private Button button = (Button) view;
                    private ItemizedClickListener itemClickListener = new ItemizedClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialogUtil.showProgressDialog();
                            RetrofitManager.getFacilityApiService()
                                    .facilities(getItem().getId(), token)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Observer<Facilities>() {
                                        @Override
                                        public void onCompleted() {
                                            Log.i(Constants.TAG, "onCompleted");
                                            BaseMainActivity activity = (BaseMainActivity) getBaseActivity();
                                            FacilityBookingFragment fragment = new FacilityBookingFragment();
                                            fragment.setFacility(getItem());
                                            activity.changeFragment(fragment, true);
                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            dialogUtil.dismissProgressDialog();
                                            if (e == null) return;
                                            Log.i(Constants.TAG, e.getMessage());
                                        }

                                        @Override
                                        public void onNext(Facilities facilities) {
                                            dialogUtil.dismissProgressDialog();
                                            List<FacilityVariantData> variantDataListList = new ArrayList<>();
                                            List<String> dateList = new ArrayList<>();
                                            List<String> stateList = new ArrayList<>();
                                            for (int i = 0; i < facilities.getFacility().size(); i++) {
                                                Facilities.FacilityBean fb = facilities.getFacility().get(i);
                                                int fid = fb.getFid();
                                                int capacity = fb.getDates().size();
                                                String name = fb.getName();
                                                String image_url = fb.getImage_url();
                                                for (int j = 0; j < capacity; j++){
                                                    dateList.add(fb.getDates().get(j).getBookdate());
                                                    stateList.add(fb.getDates().get(j).getState());
                                                }
                                                FacilityVariantData data = new FacilityVariantData(fid, capacity,
                                                        name, image_url, dateList, stateList);
                                                variantDataListList.add(data);
                                            }
                                            getItem().setVariants(variantDataListList);
                                        }
                                    });

                        }
                    };

                    {
                        view.setOnClickListener(itemClickListener);
                    }
                    @Override
                    protected void hold(FacilityData item) {
                        button.setCompoundDrawablesWithIntrinsicBounds(item.getIconRes(), 0, 0, 0);
                        button.setText(item.getName());
                        itemClickListener.setItem(item);
                    }
                };
            }
        };
    }

    @Override
    protected String getTitle(Resources res) {
        return res.getString(R.string.fragment_facility_list);
    }

    @Override
    protected AppBarLayout getToolbar(LayoutInflater inflater, ViewGroup container) {
        return createToolbarFrom(R.layout.toolbar_small);
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.createView(inflater, container, savedInstanceState);
        dialogUtil = new DialogUtil(getContext());
        list.clear();
        initObserver();
        getFacilityType();
        return view;
    }

    private void initObserver(){
        observer = new Observer<Type>() {
            @Override
            public void onCompleted() {
                Log.i(Constants.TAG, "onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                dialogUtil.dismissProgressDialog();
            }

            @Override
            public void onNext(Type type) {
                dialogUtil.dismissProgressDialog();
                for (int i = 0; i < type.getType().size(); i++){
                    int res = getRes(type.getType().get(i).getType());
                    int id = type.getType().get(i).getId();
                    String name = type.getType().get(i).getType();
                    FacilityData data = new FacilityData(res, id, name);
                    list.add(data);
                }
                getAdapter().addAll(list);
            }
        };

        facilitiesObserver = new Observer<Facilities>() {
            @Override
            public void onCompleted() {
                Log.i(Constants.TAG, "onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                dialogUtil.dismissProgressDialog();
                if (e == null) return;
                Log.i(Constants.TAG, e.getMessage());
            }

            @Override
            public void onNext(Facilities facilities) {
                dialogUtil.dismissProgressDialog();
            }
        };
    }

    private void getFacilityType(){
        SharedPreferences preferences = getContext().getSharedPreferences(Constants.USER_DATA,
                Context.MODE_PRIVATE);
        token = preferences.getString(Constants.USER_TOKEN, "");
        dialogUtil.showProgressDialog();
        RetrofitManager.getFacilityApiService()
                .type(token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    private void getFacility(int id){
        SharedPreferences preferences = getContext().getSharedPreferences(Constants.USER_DATA,
                Context.MODE_PRIVATE);
        token = preferences.getString(Constants.USER_TOKEN, "");
        dialogUtil.showProgressDialog();
        RetrofitManager.getFacilityApiService()
                .facilities(id, token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }


    private int getRes(String type){
        switch (type) {
            case Constants.FACILITY_BBQ:
                return R.drawable.dummy_bbq;
            case Constants.FACILITY_FUNCTION:
                return R.drawable.dummy_conference;
            case Constants.FACILITY_KTV:
                return R.drawable.dummy_badminton;
            case Constants.FACILITY_TENNIS:
                return R.drawable.dummy_badminton;
            default:
                return R.drawable.dummy;
        }
    }
}
