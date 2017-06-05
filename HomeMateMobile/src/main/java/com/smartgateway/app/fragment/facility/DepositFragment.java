package com.smartgateway.app.fragment.facility;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.smartgateway.app.Utils.Constants;
import com.smartgateway.app.R;
import com.smartgateway.app.Utils.DialogUtil;
import com.smartgateway.app.activity.ChildActivity;
import com.smartgateway.app.data.BookingDayData;
import com.smartgateway.app.data.FacilityData;
import com.smartgateway.app.data.FacilityVariantData;
import com.smartgateway.app.data.PaymentData;
import com.smartgateway.app.data.model.Booking.Booking;
import com.smartgateway.app.data.model.Detail;
import com.smartgateway.app.fragment.SgWalletFragment;
import com.smartgateway.app.fragment.booking.BookingListFragment;
import com.smartgateway.app.service.NetworkService.ErrorAction;
import com.smartgateway.app.data.model.User.ResponseError;
import com.smartgateway.app.service.BackgroundService;
import com.smartgateway.app.service.NetworkService.RetrofitManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import ru.johnlife.lifetools.activity.BaseMainActivity;
import ru.johnlife.lifetools.adapter.BaseAdapter;
import ru.johnlife.lifetools.fragment.BaseListFragment;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by yanyu on 5/14/2016.
 *
 */

public class DepositFragment extends BaseListFragment<PaymentData> {
    private FacilityData facility;
    private FacilityVariantData variant;
    private BookingDayData day;
    private HashSet<String> session_ids = new HashSet<>();

    private TextView time;
    private TextView date;
    private TextView variantName;
    private TextView deposit;
    private TextView fee;

    private int payment_type;
    private String token;
    private String session_id;

    public void setFacility(FacilityData facility) {
        this.facility = facility;
    }

    @Override
    protected BaseAdapter<PaymentData> instantiateAdapter(Context context) {
        return new BaseAdapter<PaymentData>(R.layout.item_payment) {
            @Override
            protected ViewHolder<PaymentData> createViewHolder(final View view) {
                return new ViewHolder<PaymentData>(view) {
                    private Button button = (Button) view.findViewById(R.id.button);
                    private ImageView image = (ImageView) view.findViewById(R.id.imgTick);
                    private ItemizedClickListener itemClickListener = new ItemizedClickListener() {
                        @Override
                        public void onClick(View v) {
                            payment_type = getPaymentType(getItem().getName());
                            notifyDataSetChanged();
                        }
                    };

                    {
                        view.setOnClickListener(itemClickListener);
                    }
                    @Override
                    protected void hold(PaymentData item) {
                        if (payment_type == getPaymentType(item.getName())) {
                            image.setVisibility(View.VISIBLE);
                            button.setTextColor(getResources().getColor(R.color.colorPrimary));
                        } else {
                            image.setVisibility(View.INVISIBLE);
                            button.setTextColor(getResources().getColor(R.color.black));
                        }
                        button.setCompoundDrawablesWithIntrinsicBounds(item.getIconRes(), 0, 0, 0);
                        button.setText(item.getName());
                        itemClickListener.setItem(item);
                    }
                };
            }
        };
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_deposit;
    }

    @Override
    protected int getListId() {
        return R.id.list;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.createView(inflater, container, savedInstanceState);
        initView(view);
        SharedPreferences preferences = getContext().getSharedPreferences(Constants.USER_DATA,
                Context.MODE_PRIVATE);
        token = preferences.getString(Constants.USER_TOKEN, "");

        ArrayList<String> list = new ArrayList<>(session_ids);
        Collections.sort(list);
        StringBuilder sb = new StringBuilder();
        String s = "";
        for (int i = 0; i < list.size(); i++) {
            sb.append(s);
            sb.append(list.get(i));
            s = ",";
        }
        session_id = sb.toString();
        getBooking(session_id);

        requestService(new BackgroundService.Requester<BackgroundService>() {
            @Override
            public void requestService(BackgroundService service) {
                BaseAdapter<PaymentData> adapter = getAdapter();
                for (PaymentData payment : service.getPayments()) {
                    adapter.add(payment);
                }
            }
        });

        payment_type = 1;
        return view;
    }

    @Override
    protected String getTitle(Resources res) {
        return res.getString(R.string.fragment_deposit);
    }

    @Override
    protected AppBarLayout getToolbar(LayoutInflater inflater, ViewGroup container) {
        return createToolbarFrom(R.layout.toolbar_small);
    }

    private void initView(View view) {
        variantName = (TextView) view.findViewById(R.id.variant);
        date = (TextView) view.findViewById(R.id.date);
        time = (TextView) view.findViewById(R.id.time);
        deposit = (TextView) view.findViewById(R.id.deposit);
        fee = (TextView) view.findViewById(R.id.fee);

        view.findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmBooking(session_id, payment_type);
            }
        });
    }

    public DepositFragment setSessionIds(HashSet<String> session_ids) {
        this.session_ids = session_ids;
        return this;
    }

    private void getBooking(String sessionId){
        final DialogUtil dialogUtil = new DialogUtil(getContext());
        dialogUtil.showProgressDialog();
        RetrofitManager.getBookingApiService()
                .book(sessionId, token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Booking>() {
                    @Override
                    public void call(final Booking booking) {
                        dialogUtil.dismissProgressDialog();
                        variantName.setText("Facility: " + booking.getBooking().getFacility());
                        date.setText("Date: " +booking.getBooking().getDate());
                        time.setText("Time: " +booking.getBooking().getTime());
                        deposit.setText("Deposit: " +booking.getBooking().getDeposit());
                        fee.setText("Booking Fee: " +booking.getBooking().getFee());
                    }
                }, new ErrorAction() {
                    @Override
                    public void call(ResponseError error) {
                        dialogUtil.dismissProgressDialog();
                        if(error != null && error.getResponseCode() == 400) {
                            if (DialogUtil.isActivityAlive(getActivity())) {
                                getActivity().onBackPressed();
                            }
                        }
                    }
                });
    }

    private void confirmBooking(String sessionID, int payment){
        final DialogUtil dialogUtil = new DialogUtil(getContext());
        dialogUtil.showProgressDialog();
        RetrofitManager.getBookingApiService()
                .confirm(sessionID, payment, token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Detail>() {
                    @Override
                    public void call(Detail detail) {
                        dialogUtil.dismissProgressDialog();
                        if (detail != null) {
                            DialogUtil.showErrorAlert(getContext(), detail.getDetail(), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    BaseMainActivity activity = (BaseMainActivity) getBaseActivity();
                                    BookingListFragment fragment = new BookingListFragment();
                                    activity.changeFragment(fragment, false);
                                }
                            });

                        }
                    }
                }, new ErrorAction() {
                    @Override
                    public void call(ResponseError error) {
                        dialogUtil.dismissProgressDialog();
                        if (error.getResponseCode() == 428) {
                            showSgWalletAlertDialog(error);
                        }
                    }
                });
    }

    private void showSgWalletAlertDialog(ResponseError error) {
        DialogUtil.showErrorAlert(getContext(),
                error.getDetail(),
                getString(R.string.sg_wallet_button_title),
                getString(R.string.later), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BaseMainActivity activity = (BaseMainActivity) getBaseActivity();
                        activity.changeFragment(new BookingListFragment(), false);
                        activity.changeFragment(new SgWalletFragment(), true);
                    }
                },
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BaseMainActivity activity = (BaseMainActivity) getBaseActivity();
                        activity.changeFragment(new BookingListFragment(), false);
                    }
                });
    }

    private int getPaymentType(String type){
        switch (type){
            case "Cash":
                return 1;
            case "Cheque":
                return 2;
            case "Online payment":
                return 3;
            default:
                return 1;
        }
    }
}
