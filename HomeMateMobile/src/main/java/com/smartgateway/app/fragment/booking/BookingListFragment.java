package com.smartgateway.app.fragment.booking;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.smartgateway.app.R;
import com.smartgateway.app.Utils.Constants;
import com.smartgateway.app.Utils.DialogUtil;
import ru.johnlife.lifetools.util.RxBus;
import com.smartgateway.app.data.BookingData;
import com.smartgateway.app.data.event.BookingsEvent;
import com.smartgateway.app.data.model.Booking.BookingList;
import com.smartgateway.app.data.model.Detail;
import com.smartgateway.app.data.model.User.ResponseError;
import com.smartgateway.app.fragment.drawer.PersonalFragment;
import com.smartgateway.app.fragment.facility.FacilityListFragment;
import com.smartgateway.app.fragment.state.BookingStateHelper;
import com.smartgateway.app.fragment.state.StateHelper;
import com.smartgateway.app.service.NetworkService.ErrorAction;
import com.smartgateway.app.service.NetworkService.RetrofitManager;

import java.io.EOFException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.johnlife.lifetools.activity.BaseMainActivity;
import ru.johnlife.lifetools.adapter.BaseAdapter;
import ru.johnlife.lifetools.fragment.BaseListFragment;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by yanyu on 5/15/2016.
 */
public class BookingListFragment extends BaseListFragment<BookingData> {
	private static BookingStateHelper states;
	private List<BookingData> list = new ArrayList<>();
	int id;

	@Override
	protected BaseAdapter<BookingData> instantiateAdapter(Context context) {
		if (null == states) {
			states = new BookingStateHelper(context);
		}
		return new BaseAdapter<BookingData>(R.layout.item_booking) {
			@Override
			protected ViewHolder<BookingData> createViewHolder(final View view) {
				return new ViewHolder<BookingData>(view) {
					private TextView date = (TextView) view.findViewById(R.id.date);
					private TextView where = (TextView) view.findViewById(R.id.where);
					private TextView state = (TextView) view.findViewById(R.id.state);

					{
						view.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								BookingDetailsFragment fragment = new BookingDetailsFragment();
								fragment.addParam(Constants.BOOKING_ID, getItem().getId());
								BaseMainActivity activity = (BaseMainActivity) getBaseActivity();
								activity.changeFragment(fragment, true);
							}
						});

						view.setOnLongClickListener(new View.OnLongClickListener() {
							@Override
							public boolean onLongClick(View v) {
								DialogUtil.showErrorAlert(getContext(), getString(R.string.remove_booking_record_confirm), getString(android.R.string.ok), getString(android.R.string.cancel),
										new View.OnClickListener() {
											@Override
											public void onClick(View v) {
												cancelBooking(getItem());
											}
										}, null);
								return true;
							}
						});
					}

					@Override
					protected void hold(BookingData item) {
						date.setText(item.getDate());
						where.setText(item.getWhere());
						StateHelper.StateData stateData = states.get(item.getState());
						state.setBackgroundColor(stateData.getColor());
						state.setText(stateData.getName());
						state.setCompoundDrawablesWithIntrinsicBounds(stateData.getIcon(), 0, 0, 0);
					}
				};
			}
		};
	}

	private void cancelBooking(BookingData bookingData) {
		final DialogUtil dialogUtil = new DialogUtil(getContext());
		dialogUtil.showProgressDialog();

		SharedPreferences preferences = getContext().getSharedPreferences(Constants.USER_DATA,
				Context.MODE_PRIVATE);
		String token = preferences.getString(Constants.USER_TOKEN, "");

		RetrofitManager.getBookingApiService().cancel(String.valueOf(bookingData.getId()), token)
					   .subscribeOn(Schedulers.io())
				       .observeOn(AndroidSchedulers.mainThread())
					   .subscribe(new Action1<Detail>() {
						   @Override
						   public void call(Detail detail) {
							   dialogUtil.dismissProgressDialog();
							   getBookingList();
						   }
					   }, new ErrorAction() {
						   @Override
						   public void call(ResponseError error) {
							   dialogUtil.dismissProgressDialog();
							   if (error.getReason() != null &&
								   error.getReason() instanceof EOFException){
							   	   getBookingList();
							   }
						   }
					   });
	}

	@Override
	protected String getTitle(Resources res) {
		return res.getString(R.string.fragment_booking_records);
	}

	@Override
	protected AppBarLayout getToolbar(LayoutInflater inflater, ViewGroup container) {
		return createToolbarFrom(R.layout.toolbar_small);
	}

	@Override
	protected int getLayoutId() {
		return R.layout.fragment_list;
	}

	@Override
	protected int getListId() {
		return R.id.list;
	}

	@Override
	protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = super.createView(inflater, container, savedInstanceState);

		getBookingList();
		return view;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		getActivity().getMenuInflater().inflate(R.menu.menu_add, menu);
		MenuItem item = menu.findItem(R.id.nav_add);
		item.setIcon(R.drawable.ic_add_booking);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.nav_add) {
			BaseMainActivity activity = (BaseMainActivity) getBaseActivity();
			FacilityListFragment fragment = new FacilityListFragment();
			activity.changeFragment(fragment, true);
		}
		return super.onOptionsItemSelected(item);
	}

	private void getBookingList() {
		list.clear();
		final DialogUtil dialogUtil = new DialogUtil(getContext());
		dialogUtil.showProgressDialog();
		SharedPreferences preferences = getContext().getSharedPreferences(Constants.USER_DATA,
				Context.MODE_PRIVATE);
		String token = preferences.getString(Constants.USER_TOKEN, "");
		RetrofitManager.getBookingApiService()
				.list(token)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Action1<BookingList>() {
					@Override
					public void call(BookingList bookingList) {
						dialogUtil.dismissProgressDialog();
						for (int i = 0; i < bookingList.getBookings().size(); i++) {
							String facility = bookingList.getBookings().get(i).getFacility();
							long day = getMilliSec(bookingList.getBookings().get(i).getDate());
							String selectedHours = bookingList.getBookings().get(i).getTime();
							String bookingState = bookingList.getBookings().get(i).getState();
							id = bookingList.getBookings().get(i).getBooking_id();
							BookingData data = new BookingData(facility, day, selectedHours, bookingState, id);
							list.add(data);
						}

						getAdapter().clear();
						getAdapter().addAll(list);
						RxBus.getDefaultInstance().post(new BookingsEvent(bookingList.getBookings().size()));
						SharedPreferences preferences = getContext().getSharedPreferences(Constants.PERSONAL_COUNT,
								Context.MODE_PRIVATE);
						SharedPreferences.Editor editor = preferences.edit();
						editor.putInt(Constants.BOOKINGS, bookingList.getBookings().size());
						editor.commit();
						// payment();

					}
				}, new ErrorAction() {
					@Override
					public void call(ResponseError error) {
						dialogUtil.dismissProgressDialog();
					}
				});
	}

	private long getMilliSec(String someDate) {
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

   /* private void payment(){


        SharedPreferences preferences = getContext().getSharedPreferences(Constants.USER_DATA,
                Context.MODE_PRIVATE);
        token = preferences.getString(Constants.USER_TOKEN, "");
       // bookingId = preferences.getString(Constants.BOOKING_ID, "");

        RetrofitManager.getUserApiService()
                .getPaymentStatus(token,id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<PaymentDetail>() {
                    @Override
                    public void onCompleted() {
                        Log.e("onCompleted booking",">>>>>>>>>");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("onError booking",">>>>>>>>>" + e);
                    }

                    @Override
                    public void onNext(PaymentDetail paymentDetail) {
                        Log.e("onNext booking",">>>>>>>>>" + paymentDetail.getDetail());
                    }
                });


    }*/
}
