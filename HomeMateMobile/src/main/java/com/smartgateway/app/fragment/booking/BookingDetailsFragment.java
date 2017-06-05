package com.smartgateway.app.fragment.booking;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.smartgateway.app.Utils.Constants;
import com.smartgateway.app.Utils.DialogUtil;
import com.smartgateway.app.Utils.GFunctions;
import com.smartgateway.app.data.model.Booking.BookingDetail;
import com.smartgateway.app.data.model.Booking.PaymentDetail;
import com.smartgateway.app.data.model.User.ResponseError;
import com.smartgateway.app.fragment.SgWalletFragment;
import com.smartgateway.app.fragment.state.BookingStateHelper;
import com.smartgateway.app.fragment.state.StateHelper;
import com.smartgateway.app.service.NetworkService.ErrorAction;
import com.smartgateway.app.service.NetworkService.RetrofitManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ru.johnlife.lifetools.activity.BaseMainActivity;
import ru.johnlife.lifetools.fragment.BaseAbstractFragment;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * BookingDetailsFragment
 * Created by yanyu on 5/15/2016.
 */
public class BookingDetailsFragment extends BaseAbstractFragment {
    private static BookingStateHelper states;
    private Observer<BookingDetail> observer;

    private TextView date, where, state, deposit,
            reason, bookingTime, confirmTime, refundedTime, makePaymentBtn, payment_msg;

    private int bookingId;
    DialogUtil dialogUtil;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bookingId = getArguments().getInt(Constants.BOOKING_ID);
        Log.i(Constants.TAG, bookingId + "");
    }

    @Override
    protected String getTitle(Resources res) {
        return res.getString(R.string.fragment_booking_detail);
    }

    @Override
    protected AppBarLayout getToolbar(LayoutInflater inflater, ViewGroup container) {
        return createToolbarFrom(R.layout.toolbar_small);
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_booking_details, container, false);
        dialogUtil = new DialogUtil(getContext());
        if (null == states) {
            states = new BookingStateHelper(view.getContext());
        }
        initView(view);
        initObserver();
        getBookingDetail();
        return view;
    }
    private void initView(View view){
        date = (TextView) view.findViewById(R.id.date);
        where = (TextView) view.findViewById(R.id.where);
        state = (TextView) view.findViewById(R.id.state);
        deposit = (TextView) view.findViewById(R.id.deposit);
        reason = (TextView) view.findViewById(R.id.reason);
        bookingTime = (TextView) view.findViewById(R.id.bookingTime);
        confirmTime = (TextView) view.findViewById(R.id.confirmedTime);
        refundedTime = (TextView) view.findViewById(R.id.refundedTime);
        makePaymentBtn = (TextView) view.findViewById(R.id.btn_payment);
        payment_msg = (TextView) view.findViewById(R.id.payment_msg);
    }

    private void initObserver(){
        observer = new Observer<BookingDetail>() {
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
            public void onNext(BookingDetail bookingDetail) {
                dialogUtil.dismissProgressDialog();
                initBooking(bookingDetail);
            }
        };
    }

    private void initBooking(BookingDetail bookingDetail) {
        long actualDate = getMilliSec(bookingDetail.getBooking().getDate());
        String d = Constants.dateFormat.format(actualDate);
        date.setText(GFunctions.convertDatetoViewType1(d));
        bookingTime.setText(bookingDetail.getBooking().getBooking_time());
        bookingTime.setText(GFunctions.convertDatetoViewType2(bookingDetail.getBooking().getBooking_time()));
        confirmTime.setText(GFunctions.convertDatetoViewType2(bookingDetail.getConfirmed()));
        refundedTime.setText(GFunctions.convertDatetoViewType2(bookingDetail.getRefunded()));
        payment_msg.setText(bookingDetail.getBooking().getPayment_msg());
        StringBuilder sb = new StringBuilder();
        sb.append(bookingDetail.getBooking().getFacility())
                .append(" ")
                .append('(').append(bookingDetail.getBooking().getTime()).append(')');
        where.setText(sb.toString());

        int bookingState = bookingDetail.getBooking().getBookingState();
        initState(bookingState);
        initPaymentButton(bookingState);

        deposit.setText(bookingDetail.getBooking().getPayment_amt());
    }

    private void initPaymentButton(int bookingState) {
        if (bookingState == Constants.BookingState.RESERVED) {
            makePaymentBtn.setVisibility(View.VISIBLE);
            makePaymentBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    makePayment();
                }
            });
        }
    }

    private void makePayment() {
        dialogUtil.showProgressDialog();
        RetrofitManager.getUserApiService()
                .getPaymentStatus(getToken(), bookingId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<PaymentDetail>() {
                    @Override
                    public void call(PaymentDetail detail) {
                        dialogUtil.dismissProgressDialog();
                        // go back after successfull booking
                        if (DialogUtil.isActivityAlive(getActivity())) {
                            getActivity().onBackPressed();
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

    private void initState(int bookingState) {
        StateHelper.StateData stateData = states.get(bookingState);
        state.setBackgroundColor(stateData.getColor());
        state.setText(stateData.getName());
        state.setCompoundDrawablesWithIntrinsicBounds(stateData.getIcon(), 0, 0, 0);
    }


    private void getBookingDetail(){
        dialogUtil.showProgressDialog();
        RetrofitManager.getBookingApiService()
                .detail(bookingId, getToken())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    @NonNull
    private String getToken() {
        SharedPreferences preferences = getContext().getSharedPreferences(Constants.USER_DATA,
                Context.MODE_PRIVATE);
        return preferences.getString(Constants.USER_TOKEN, "");
    }

    private long getMilliSec(String someDate){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        long timeStamp;
        try {
            Date date = sdf.parse(someDate);
            timeStamp = date.getTime();
            return timeStamp;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private long getMillSect(String someDate){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
        long timeStamp;
        try {
            Date date = sdf.parse(someDate);
            timeStamp = date.getTime();
            return timeStamp;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
