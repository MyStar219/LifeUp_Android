package com.smartgateway.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.evideo.weiju.WeijuManage;
import com.evideo.weiju.callback.CommandCallback;
import com.evideo.weiju.callback.InfoCallback;
import com.evideo.weiju.command.apartment.BindApartmentByInfoCommand;
import com.evideo.weiju.command.token.AccessTokenCommand;
import com.evideo.weiju.command.wave.CreateUnlockWaveCommand;
import com.evideo.weiju.command.wave.DeleteUnlockWaveCommand;
import com.evideo.weiju.command.wave.ObtainUnlockWaveDetailCommand;
import com.evideo.weiju.command.wave.ObtainUnlockWaveListByPageCommand;
import com.evideo.weiju.info.CommandError;
import com.evideo.weiju.info.Info;
import com.evideo.weiju.info.apartment.ApartmentInfo;
import com.evideo.weiju.info.unlock.CreateUnlockWaveInfo;
import com.evideo.weiju.info.unlock.UnlockWaveInfo;
import com.evideo.weiju.info.unlock.UnlockWaveInfoList;
import com.smartgateway.app.Utils.Constants;
import com.smartgateway.app.data.model.Apartment.Apartments;
import com.smartgateway.app.data.model.Credentials;
import com.smartgateway.app.data.model.Provider;
import com.smartgateway.app.data.model.User.UserUtil;
import com.smartgateway.app.service.NetworkService.RetrofitManager;
import com.smartgateway.app.weiju.VoipWeijuHelper;

import java.util.List;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class WeijuHelper {
    private String appId;
    private String secret;
    private String userId;

    // todo use shared pref to save this object, just for test integration
    private static ApartmentInfo apartmentInfo;

    public static ApartmentInfo getApartmentInfo() {
        return apartmentInfo;
    }

    public void login(final Context context) {
        Log.d("WeijuHelper", "weiju start login");

        Credentials credentials = UserUtil.getCredentials(context);
        if (credentials == null) { // No credentials available return
            return;
        }
        Provider weijuProvider = credentials.getProvider(Constants.PROVIDER_WEIJU);
        if (weijuProvider == null) {
            Log.e("WeijuHelder", "weiju provider is null");
            return;
        }

        appId = weijuProvider.getUsername();
        secret = weijuProvider.getPassword();
        SharedPreferences preferences = context.getSharedPreferences(Constants.USER_DATA,
                Context.MODE_PRIVATE);
        userId = preferences.getString(Constants.USER_MOBILE, userId);
        if (apartmentInfo != null) {
            return;
        }

        Log.d("WeijuHelper", "app id: " + appId);
        Log.d("WeijuHelper", "secret: " + secret);
        Log.d("WeijuHelper", "user id: " + userId);

        AccessTokenCommand command = new AccessTokenCommand(context, appId, secret, userId);
        command.setCallback(new CommandCallback() {

            @Override
            public void onSuccess() {
                Log.d("WeijuHelper","weiju success");
                fetchApartment(context);
            }

            @Override
            public void onFailure(CommandError error) {
                apartmentInfo = null;
                Log.e("WeijuHelper", "weiju error: " + error.getStatus() + " message: " + error.getMessage());
            }
        });
        Log.d("WeijuHelper", "weiju start executing login");
        WeijuManage.execute(command);
    }

    public void signOut() {
        apartmentInfo = null;
        new VoipWeijuHelper().disconnect();
    }

    public void fetchApartment(final Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Constants.USER_DATA,
                Context.MODE_PRIVATE);
        String token = preferences.getString(Constants.USER_TOKEN, "");
        RetrofitManager.getApartmentApiService()
                .list(token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Apartments>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(Apartments apartments) {
                        Log.d("WeijuHelper", "apartments fetched " + apartments.getApartments().size());

                        List<Apartments.ApartmentsBean> list = apartments.getApartments();
                        for (Apartments.ApartmentsBean apartmentsBean : list) {
                            if (apartmentsBean != null && apartmentsBean.isDefaultX()) {
                                String city = "Singapore";
                                String community = apartmentsBean.getCondo().getName();
                                String address = (apartmentsBean.getCondo().getBuilding().getLevel().getUnit().getUnit_no()).replace("-", "");

//                                community = "6 Derbyshire";
                                address = "1Level1Unit";

                                Log.d("WeijuHelper", "city :" + city);
                                Log.d("WeijuHelper", "community :" + community);
                                Log.d("WeijuHelper", "address :" + address);

                                BindApartmentByInfoCommand byInfoCommand = new BindApartmentByInfoCommand(context, city, community, address);
                                byInfoCommand.setCallback(new InfoCallback<ApartmentInfo>() {

                                    @Override
                                    public void onSuccess(ApartmentInfo result) {
                                        Log.d("WeijuHelper", "apartment info added");
                                        setApartmentInfo(result);
                                        VoipWeijuHelper voipWeijuHelper = new VoipWeijuHelper();
                                        voipWeijuHelper.connect(context);
                                    }

                                    @Override
                                    public void onFailure(CommandError error) {
                                        Log.e("WeijuHelper", "can't bind apartment: " + error.getStatus() + " message:" + error.getMessage());
                                    }
                                });
                                WeijuManage.execute(byInfoCommand);//执行绑定住宅
                            }
                        }
                    }
                });

    }

    public void fetchWaves(Context appContext, int type, InfoCallback<UnlockWaveInfoList> callback) {
        final String apartmentId = apartmentInfo.getId();
        final ObtainUnlockWaveListByPageCommand obtainUnlockWaveListByPageCommand
                = new ObtainUnlockWaveListByPageCommand(appContext, apartmentId, type);
        obtainUnlockWaveListByPageCommand.setPage(0);
        obtainUnlockWaveListByPageCommand.setSize(100);
        obtainUnlockWaveListByPageCommand.setCallback(callback);
        WeijuManage.execute(obtainUnlockWaveListByPageCommand);
    }

    public void fetchWaveDetail(Context appContext, String waveId, InfoCallback<UnlockWaveInfo> callback) {
        final String apartmentId = apartmentInfo.getId();
        final ObtainUnlockWaveDetailCommand obtainUnlockWaveDetailCommand = new ObtainUnlockWaveDetailCommand(appContext, apartmentId, waveId);
        obtainUnlockWaveDetailCommand.setCallback(callback);
        WeijuManage.execute(obtainUnlockWaveDetailCommand);
    }

    public void createUnlockWave(Context appContext, int type, InfoCallback<CreateUnlockWaveInfo> callback) {
        Log.v("WeiJuHelper", "Start of CreateWave");
        CreateUnlockWaveCommand createUnlockWaveCommand = new CreateUnlockWaveCommand(appContext, apartmentInfo.getId(), type);
        createUnlockWaveCommand.setEffect_time((int) ((System.currentTimeMillis()/1000) - (30)));
        if (type == 1) { // If Guest Wave
            createUnlockWaveCommand.setExpired_time((int) ((System.currentTimeMillis() / 1000) + (60*60)));
            createUnlockWaveCommand.setCount(3);
        }
        else { // Else if Owner
            createUnlockWaveCommand.setExpired_time((int) ((System.currentTimeMillis() / 1000) + (24*60*60)));
        }
        createUnlockWaveCommand.setCallback(callback);
        WeijuManage.execute(createUnlockWaveCommand);
    }

    public void fetchOwnerUnlockWaveList(Context appContext, InfoCallback<UnlockWaveInfoList> callback) {
        fetchWaves(appContext, 0, callback);
    }

    public void deleteWave(Context appContext, UnlockWaveInfo unlockWaveInfo, InfoCallback<Info> callback) {
        DeleteUnlockWaveCommand deleteUnlockWaveCommand = new DeleteUnlockWaveCommand(appContext, apartmentInfo.getId(), unlockWaveInfo.getId());
        deleteUnlockWaveCommand.setCallback(callback);
        WeijuManage.execute(deleteUnlockWaveCommand);
    }

    public void deleteWave(Context appContext, UnlockWaveInfo unlockWaveInfo) {
        deleteWave(appContext, unlockWaveInfo, new InfoCallback<Info>() {
            @Override
            public void onSuccess(Info info) {
                Log.d("WeijuHelper", "wave is deleted");
            }

            @Override
            public void onFailure(CommandError error) {
                Log.e("WeijuHelper", "wave is not deleted");
            }
        });
    }

    public static synchronized void setApartmentInfo(ApartmentInfo apartmentInfo) {
        WeijuHelper.apartmentInfo = apartmentInfo;
    }

    public static synchronized boolean isWeijuConnected() {
        return apartmentInfo != null;
    }
}
