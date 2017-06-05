package com.smartgateway.app.activity;

import android.content.Intent;
import android.support.annotation.NonNull;

import com.smartgateway.app.HomeMateHelper;
import com.smartgateway.app.LifeUpProvidersHelper;
import com.smartgateway.app.Utils.Constants;
import com.smartgateway.app.Utils.DialogUtil;
import com.smartgateway.app.fragment.user.LoginFragment;
import com.smartgateway.app.fragment.SplashFragment;

import ru.johnlife.lifetools.ClassConstantsProvider;
import ru.johnlife.lifetools.activity.BaseMainActivity;
import ru.johnlife.lifetools.fragment.BaseAbstractFragment;
import ru.johnlife.lifetools.service.BaseBackgroundService;

/**
 * LoginActivity
 * Created by yanyu on 5/13/2016.
 */
public class LoginActivity extends BaseMainActivity {

    @NonNull
    @Override
    protected BaseAbstractFragment createInitialFragment() {
        SplashFragment fragment = new SplashFragment();
        requestService(new BaseBackgroundService.Requester() {
            @Override
            public void requestService(BaseBackgroundService service) {
                Runnable action;
                if (service.isLoggedIn()) {
                    action = new Runnable() {
                        @Override
                        public void run() {
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        }
                    };
                } else {
                    action = new Runnable() {
                        @Override
                        public void run() {
                            changeFragment(new LoginFragment());
                        }
                    };
                }
                findViewById(android.R.id.content).postDelayed(action, 1000);
            }
        });
        return fragment;
    }

    @Override
    protected boolean shouldBeLoggedIn() {
        return false;
    }

    @Override
    protected ClassConstantsProvider getClassConstants() {
        return Constants.CLASS_CONSTANTS;
    }

    @Override
    protected void onDetailReturned(String detail, int responseCode) {
        DialogUtil.showDetailErrorAlert(this, detail, responseCode);
    }

    @Override
    protected void onLogoutEvent() {
        new LifeUpProvidersHelper().signOut(this);
    }
}
