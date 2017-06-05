package com.smartgateway.app.fragment.apartment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.smartgateway.app.HomeMateHelper;
import com.smartgateway.app.LifeUpProvidersHelper;
import com.smartgateway.app.R;
import com.smartgateway.app.Utils.Constants;

import com.smartgateway.app.Utils.DialogUtil;
import com.smartgateway.app.data.RealmDB.Building;
import com.smartgateway.app.data.RealmDB.Level;
import com.smartgateway.app.data.RealmDB.Unit;
import com.smartgateway.app.data.model.Apartment.Condo;

import com.smartgateway.app.data.model.Apartment.Condos;
import com.smartgateway.app.data.model.Credentials;
import com.smartgateway.app.data.model.CredentialsBean;
import com.smartgateway.app.data.model.Detail;
import com.smartgateway.app.data.model.User.UserUtil;
import com.smartgateway.app.service.NetworkService.ErrorAction;
import com.smartgateway.app.data.model.User.ResponseError;
import com.smartgateway.app.service.NetworkService.RetrofitManager;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import retrofit2.adapter.rxjava.HttpException;
import ru.johnlife.lifetools.activity.BaseMainActivity;
import ru.johnlife.lifetools.fragment.BaseAbstractFragment;
import ru.johnlife.lifetools.service.BaseBackgroundService;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by yanyu on 5/20/2016.
 */
public class AddApartmentFragment extends BaseAbstractFragment {

    private AutoCompleteTextView apartmentName;
    private MaterialBetterSpinner level, unit, block;
    private SwipeRefreshLayout swipeRefreshLayout;

    private Realm realm;

    private ArrayAdapter<String> nameAdapter;
    private ArrayAdapter<String> buildingAdapter;
    private ArrayAdapter<String> levelAdapter;
    private ArrayAdapter<String> unitAdapter;

    private List<Condos.CondosBean> condosBeanList;

    private int condo_id;
    private int levelPosition;
    private int unitPosition;
    private int unit_id;

    private Observer<Condos> observer;
    private Observer<Condo> condoObserver;
    private Condo mCondo;

    private String token;
    private String condoName;

    DialogUtil dialogUtil;

    @Override
    protected String getTitle(Resources res) {
        return res.getString(R.string.fragment_add_apartment);
    }

    @Override
    protected AppBarLayout getToolbar(LayoutInflater inflater, ViewGroup container) {
        return createToolbarFrom(R.layout.toolbar_small);
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_apartment, container, false);
        dialogUtil = new DialogUtil(getContext());
        RealmConfiguration configuration = new RealmConfiguration
                .Builder(getActivity())
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(configuration);
        realm = Realm.getDefaultInstance();

        init();
        initView(view);
        initObserver();

        view.findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addApartment();
            }
        });
        return view;
    }

    private void init(){
        mCondo = new Condo();
        condosBeanList = new ArrayList<>();
        nameAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_dropdown_item_1line/*, testStrArray*/);

        buildingAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_dropdown_item_1line);

        levelAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_dropdown_item_1line);

        unitAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_dropdown_item_1line);
    }

    private void initView(View view){
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeFresh);
        swipeRefreshLayout.setColorSchemeColors(R.color.colorPrimary, R.color.colorPrimaryDark);
        swipeRefreshLayout.setRefreshing(false);

        apartmentName = (AutoCompleteTextView) view.findViewById(R.id.apartmentName);
        apartmentName.setAdapter(nameAdapter);

        block = (MaterialBetterSpinner) view.findViewById(R.id.building);
        block.setAdapter(buildingAdapter);

        level = (MaterialBetterSpinner) view.findViewById(R.id.level);
        level.setAdapter(levelAdapter);

        unit = (MaterialBetterSpinner) view.findViewById(R.id.apartmentUnit);
        unit.setAdapter(unitAdapter);

        apartmentName.setThreshold(1);
        apartmentName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                condoName = s.toString();
                getCondos();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        apartmentName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                condo_id = condosBeanList.get(position).getCondo_id();
                getCondoDetail(condo_id);
            }
        });

        block.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                levelPosition = position;
                levelAdapter.clear();
                unitAdapter.clear();
                level.setText("");
                unit.setText("");
                for (int i = 0; i < mCondo.getCondo().getBuildings()
                        .get(position).getLevels().size(); i++){
                    levelAdapter.add(String.valueOf(mCondo.getCondo()
                            .getBuildings().get(position).getLevels()
                            .get(i).getLevel()));
                }
                levelAdapter.notifyDataSetChanged();
            }
        });

        level.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                unitPosition = position;
                unitAdapter.clear();
                unit.setText("");
                for (int i = 0; i < mCondo.getCondo().getBuildings()
                        .get(levelPosition).getLevels()
                        .get(position).getUnits().size(); i++){
                    unitAdapter.add(String.valueOf(mCondo.getCondo()
                            .getBuildings().get(levelPosition).getLevels()
                            .get(position).getUnits().get(i).getUnit_no()));
                }
                unitAdapter.notifyDataSetChanged();
            }
        });

        unit.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                unit_id = mCondo.getCondo().getBuildings()
                        .get(levelPosition).getLevels()
                        .get(unitPosition).getUnits()
                        .get(position).getUnit_id();
            }
        });
    }

    private void initObserver(){


        // Condo detail
            condoObserver = new Observer<Condo>() {
                @Override
                public void onCompleted() {
                    swipeRefreshLayout.setRefreshing(false);
                    Log.i(Constants.TAG, " onCompleted");
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            Realm.getDefaultInstance().deleteAll();
                            for (int i = 0; i < mCondo.getCondo().getBuildings().size(); i ++){
                                Building building = realm.createObject(Building.class);
                                building.building_id = mCondo.getCondo().getBuildings().get(i).getBuilding_id();

                                for (int j = 0; j < mCondo.getCondo().getBuildings().get(i).getLevels().size(); j++){
                                    Level levels = realm.createObject(Level.class);
                                    levels.level = mCondo.getCondo().getBuildings().get(i).getLevels().get(j).getLevel();
                                    building.levels.add(levels);

                                    for (int k = 0; k < mCondo.getCondo().getBuildings().get(i).getLevels().get(j).getUnits().size(); k++){
                                        Unit units = realm.createObject(Unit.class);
                                        units.unit_id = mCondo.getCondo().getBuildings().get(i).getLevels().get(j).getUnits().get(k).getUnit_id();
                                        units.unit_no = mCondo.getCondo().getBuildings().get(i).getLevels().get(j).getUnits().get(k).getUnit_no();
                                        levels.units.add(units);
                                    }
                                }
                            }
                        }
                    });
//                    exportDatabase();
                    buildingAdapter.notifyDataSetChanged();
                }

                @Override
                public void onError(Throwable e) {
                    dialogUtil.dismissProgressDialog();
                    Log.i(Constants.TAG, e.getMessage());
                    if (e == null) return;
                }

                @Override
                public void onNext(Condo condo) {
                    dialogUtil.dismissProgressDialog();
                    mCondo.setCondo(condo.getCondo());
                    buildingAdapter.clear();
                    for (int i = 0; i < mCondo.getCondo().getBuildings().size(); i++){
                        buildingAdapter.add(String.valueOf(mCondo.getCondo().getBuildings().get(i).getBlock()));
                    }
                }
            };

        // Condo names
            observer = new Observer<Condos>() {
                @Override
                public void onCompleted() {
                    Log.i(Constants.TAG, " onCompleted");
                    ((Activity)getContext()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            nameAdapter.notifyDataSetChanged();
                        }
                    });
                }

                @Override
                public void onError(Throwable e) {
                    dialogUtil.dismissProgressDialog();
                    Log.i(Constants.TAG, e.getMessage());
                    if (e == null) return;
                        DialogUtil.showErrorAlert(getContext(), "\"detail\": \"retrieving condo info fail\"", null);
                }

                @Override
                public void onNext(final Condos condos) {
                    nameAdapter.clear();
                    condosBeanList.clear();
                    for (Condos.CondosBean condo : condos.getCondos()){
                        nameAdapter.add(condo.getName());
                        condosBeanList.add(condo);
                    }
                }
            };
    }

    // Get condo names & id
    private void getCondos(){
        SharedPreferences preferences = getContext().getSharedPreferences(Constants.USER_DATA,
                Context.MODE_PRIVATE);
        token = preferences.getString(Constants.USER_TOKEN, "");
        RetrofitManager.getApartmentApiService()
                .condos(condoName,token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    // Get condo detail with condo id
    private void getCondoDetail(int id){
        swipeRefreshLayout.setRefreshing(true);
        dialogUtil.showProgressDialog();
        RetrofitManager.getApartmentApiService()
                .getCondo(id,token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(condoObserver);
    }

    private void addApartment(){
        dialogUtil.showProgressDialog();
        RetrofitManager.getApartmentApiService()
                .add(unit_id, condo_id, token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<CredentialsBean>() {
                    @Override
                    public void call(CredentialsBean credentialsBean) {
                        dialogUtil.dismissProgressDialog();

                        UserUtil.saveCredentials(getContext(), credentialsBean.getCredentials());

                        LifeUpProvidersHelper helper = new LifeUpProvidersHelper();
                        helper.signOut(getContext());
                        helper.login(getContext());

                        if (!TextUtils.isEmpty(credentialsBean.getDetail())) {
                            DialogUtil.showErrorAlert(getContext(), credentialsBean.getDetail(), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    BaseMainActivity activity = (BaseMainActivity) getBaseActivity();
                                    activity.changeFragment(new ApartmentListFragment());
                                }
                            });
                        }
                    }
                }, new ErrorAction() {
                    @Override
                    public void call(ResponseError error) {
                        dialogUtil.dismissProgressDialog();
                    }
                });
    }

    public void exportDatabase() {

        // init realm

        File exportRealmFile = null;
        try {
            // get or create an "export.realm" file
            exportRealmFile = new File(getActivity().getExternalCacheDir(), "export.realm");

            // if "export.realm" already exists, delete
            exportRealmFile.delete();

            // copy current realm to "export.realm"
            realm.writeCopyTo(exportRealmFile);

        } catch (IOException e) {
            e.printStackTrace();
        }
        realm.close();

        // init email intent and add export.realm as attachment
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("plain/text");
        intent.putExtra(Intent.EXTRA_EMAIL, "u0509421@gmail.com");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Realm Database");
        intent.putExtra(Intent.EXTRA_TEXT, "new file coming");
        Uri u = Uri.fromFile(exportRealmFile);
        intent.putExtra(Intent.EXTRA_STREAM, u);

        // start email intent
        startActivity(Intent.createChooser(intent, "YOUR CHOOSER TITLE"));
    }
}
