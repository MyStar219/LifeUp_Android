package com.smartgateway.app.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.creativityapps.badgedimageviews.BadgedSquareImageView;
import com.smartgateway.app.R;
import com.smartgateway.app.Utils.Constants;
import com.smartgateway.app.Utils.DialogUtil;
import com.smartgateway.app.activity.Data;
import com.smartgateway.app.activity.WebActivity;
import com.smartgateway.app.data.model.SGWalletDetail;
import com.smartgateway.app.data.model.User.User;
import com.smartgateway.app.data.model.User.UserUtil;
import com.smartgateway.app.service.NetworkService.RetrofitManager;

import ru.johnlife.lifetools.fragment.BaseAbstractFragment;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SgWalletFragment extends BaseAbstractFragment {

    private TextView balance;
    private View topUp;
    private View history;
    private View transfer;
    private View purchase;

    @Override
    protected String getTitle(Resources res) {
        return res.getString(R.string.my_sgwallet);
    }

    @Override
    protected AppBarLayout getToolbar(LayoutInflater inflater, ViewGroup container) {
        return createToolbarFrom(R.layout.toolbar_small);
    }

    @Override
    protected View createView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sgwallet, container, false);

        balance = (TextView) view.findViewById(R.id.balance);
        topUp = view.findViewById(R.id.top_up);
        history = view.findViewById(R.id.history);
        transfer = view.findViewById(R.id.transfer);
        purchase = view.findViewById(R.id.purchase);

        BadgedSquareImageView badgeHistory = (BadgedSquareImageView)view.findViewById(R.id.badgeHistory);
        BadgedSquareImageView badgeTransfer = (BadgedSquareImageView)view.findViewById(R.id.badgeTransfer);
        BadgedSquareImageView badgeFee = (BadgedSquareImageView)view.findViewById(R.id.badgeFee);

        final User.Notification notification = UserUtil.loadFeedback(getActivity());

        if (notification.getHistory()) {
            badgeHistory.showBadge(true);
            badgeHistory.setBadgeText("1");
        }
        if (notification.getTransfer()) {
            badgeTransfer.showBadge(true);
            badgeTransfer.setBadgeText("1");
        }
        if (notification.getMaintenance_fees()) {
            badgeFee.showBadge(true);
            badgeFee.setBadgeText("1");
        }
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    public void fetchSGWallet() {
        SharedPreferences preferences = getContext().getSharedPreferences(Constants.USER_DATA,
                Context.MODE_PRIVATE);
        String token = preferences.getString(Constants.USER_TOKEN, "");

        final DialogUtil dialogUtil = new DialogUtil(getActivity());
        dialogUtil.showProgressDialog();

        RetrofitManager.getUserApiService()
                .getSGWallet(token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<SGWalletDetail>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        dialogUtil.dismissProgressDialog();
                    }

                    @Override
                    public void onNext(SGWalletDetail sgWalletDetail) {
                        dialogUtil.dismissProgressDialog();

                        if (sgWalletDetail == null || sgWalletDetail.getWallet() == null) {
                            return;
                        }

                        updateBalance(sgWalletDetail);
                        updateClickListeners(sgWalletDetail.getWallet());
                    }
                });
    }

    private void updateBalance(SGWalletDetail sgWalletDetail) {
        SGWalletDetail.Wallet wallet = sgWalletDetail.getWallet();
        balance.setText(String.format("%s %s", wallet.getCurrency(),
                wallet.getBalance()));
    }

    private void updateClickListeners(SGWalletDetail.Wallet wallet) {
        setClickListener(topUp, wallet.getTopupUrl(), getString(R.string.top_up));
        setClickListener(history, wallet.getHistoryUrl(), getString(R.string.history));
        setClickListener(purchase, wallet.getMaintenancefeesUrl(), getString(R.string.maintenance_fees));
        setClickListener(transfer, wallet.getTransferUrl(), getString(R.string.transfer));
    }

    private void setClickListener(View view, final String url, final String title) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUrl(url, title);
            }
        });
    }

    private void openUrl(String url, String title) {
        Log.d("WALLET", "move to " + url);
        Intent intent = new Intent(getActivity().getApplicationContext(), WebActivity.class);
        Data data = new Data(title, url);
        intent.putExtra("data", data);
        startActivity(intent);
    }

    public void onResume() {
        super.onResume();
        fetchSGWallet();
    }
}

