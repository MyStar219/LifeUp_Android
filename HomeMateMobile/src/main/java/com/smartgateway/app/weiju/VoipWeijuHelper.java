package com.smartgateway.app.weiju;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.evideo.voip.sdk.EVVoipAccount;
import com.evideo.voip.sdk.EVVoipException;
import com.evideo.voip.sdk.EVVoipManager;
import com.evideo.weiju.WeijuManage;
import com.evideo.weiju.callback.InfoCallback;
import com.evideo.weiju.command.voip.ObtainVoipInfoCommand;
import com.evideo.weiju.info.CommandError;
import com.evideo.weiju.info.voip.VoipInfo;
import com.orvibo.homemate.common.ViHomeProApp;

public class VoipWeijuHelper {

    private static VoipInfo voipInfo;
    private Context appContext;

    public synchronized void connect(final Context context) {
        if (isConnected()) {
            return;
        }

        appContext = context.getApplicationContext();

        if(voipInfo != null) {
            initVoipAccount(ViHomeProApp.evVoipAccount.getUsername(),
                    ViHomeProApp.evVoipAccount.getPassword(),
                    ViHomeProApp.evVoipAccount.getDisplayName(),
                    ViHomeProApp.evVoipAccount.getDomain(),
                    ViHomeProApp.evVoipAccount.getPort());
            setVoipAccountStateCallback();
        } else {
            Log.d("VoipHelper", "start obtain voip info command");
            ObtainVoipInfoCommand obtainVoipInfoCommand = new ObtainVoipInfoCommand(context);
            obtainVoipInfoCommand.setCallback(new InfoCallback<VoipInfo>() {

                @Override
                public void onSuccess(VoipInfo result) {
                    initVoipAccount(result.getUsername(), result.getPassword(), "", result.getDomain(), result.getPort());
                    setVoipAccountStateCallback();
                    voipInfo = result;
                }

                @Override
                public void onFailure(CommandError error) {
                    Log.e("VoipHelper", "can't opntain voip info: " + error.getStatus() + error.getMessage());
                    Toast.makeText(context, "Void error " + error.getStatus() + error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
            WeijuManage.execute(obtainVoipInfoCommand);
        }
    }

    private boolean isConnected() {
        if(ViHomeProApp.evVoipAccount != null &&
           ViHomeProApp.evVoipAccount.getState().equals(EVVoipAccount.AccountState.ONLINE)) {
            Log.d("VoipHelper", "online - connected");
            return true;
        }
        return false;
    }

    public synchronized void disconnect() {
        voipInfo = null;
        if(ViHomeProApp.evVoipAccount != null){
            ViHomeProApp.evVoipAccount.logout();
        }
        InCallActivity.evVoipCall = null;
    }

    private void initVoipAccount(String username, String password, String displayName, String domain, int port){
        try {
            Log.d("VoipHelper", "start init voip account, is ready: " + EVVoipManager.isReadly());
            if(EVVoipManager.isReadly()){
                Log.d("VoipHelper", "new state - in process");
                ViHomeProApp.evVoipAccount = EVVoipManager.login(username, password, displayName, domain, port);
            }
        } catch (EVVoipException e) {
            e.printStackTrace();
            Log.e("VoipHelper", "can't connect to voip, EVVoipException exception");
            Toast.makeText(appContext, "Voip error " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setVoipAccountStateCallback(){
        if (ViHomeProApp.evVoipAccount == null) {
            Log.e("VoipHelper", "evVoipAccount is null, can't connect");
            return;
        }

        ViHomeProApp.evVoipAccount.setAccountStateCallback(new EVVoipAccount.AccountStateCallback() {

            @Override
            public void onState(EVVoipAccount.AccountState state) {
                if(EVVoipAccount.AccountState.ONLINE == state) {
                    Log.d("VoipHelper", "new state - online");
                    //voipStateView.setText("在线");
                }else if(EVVoipAccount.AccountState.OFFLINE == state) {
                    Log.d("VoipHelper", "new state - offline");
                    //voipStateView.setText("离线");
                }else if(EVVoipAccount.AccountState.LOGINPROCESS == state) {
                    Log.d("VoipHelper", "new state - in process");
                    //voipStateView.setText("登录中。。。");
                }else if(EVVoipAccount.AccountState.NONE == state) {
                    Log.d("VoipHelper", "new state - not connected(none)");
                    //voipStateView.setText("未登录");
                }
            }
        });
    }
}
