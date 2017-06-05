package com.smartgateway.app.fragment.facility;

import android.content.Context;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.smartgateway.app.Utils.DialogUtil;
import com.smartgateway.app.data.BookingDayData;
import com.smartgateway.app.data.BookingHourData;
import com.smartgateway.app.data.FacilityData;
import com.smartgateway.app.data.FacilityVariantData;
import com.smartgateway.app.fragment.state.StateHelper;

import java.util.HashSet;
import java.util.List;

import ru.johnlife.lifetools.activity.BaseMainActivity;
import ru.johnlife.lifetools.adapter.BaseAdapter;
import ru.johnlife.lifetools.fragment.BaseListFragment;

/**
 * Created by yanyu on 5/14/2016.
 *
 */
public class TimeSlotListFragment extends BaseListFragment<BookingHourData> {
	private static StateHelper states;
	private BookingDayData day;
	//    private SparseBooleanArray checks = new SparseBooleanArray(25);
	private FacilityData facility;
	private FacilityVariantData variant;
	private List<BookingHourData> hourList;
	private HashSet<String> session_ids;

	@Override
	protected BaseAdapter<BookingHourData> instantiateAdapter(Context context) {
		if (null == states) {
			states = new StateHelper(context, R.array.state_hours);
		}
		return new BaseAdapter<BookingHourData>(R.layout.item_booking_hour, hourList) {
			@Override
			protected ViewHolder<BookingHourData> createViewHolder(final View view) {
				return new ViewHolder<BookingHourData>(view) {
					TextView name = (TextView) view.findViewById(R.id.name);
					TextView peak = (TextView) view.findViewById(R.id.peak);
					TextView state = (TextView) view.findViewById(R.id.state);
					ImageView check = (ImageView) view.findViewById(R.id.check);
					LinearLayout llItem = (LinearLayout) view.findViewById(R.id.llItem);

					@Override
					protected void hold(final BookingHourData item) {
						name.setText(item.getName());
						peak.setVisibility(item.isPeak() ? View.VISIBLE : View.INVISIBLE);
						final boolean isBookable = item.getStatus().equals("bookable");
						if (isBookable) {
							state.setVisibility(View.GONE);
						} else {
							state.setVisibility(View.VISIBLE);
							final int tag = item.getState();
							StateHelper.StateData stateData = states.get(tag);
							state.setText(item.getStatus());
							state.setBackgroundColor(stateData.getColor());
						}

						llItem.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								if (!isBookable) return;
//                                if (tag != Constants.State.AVAILABLE) return;
								String id = String.valueOf(item.getSession_id());
								boolean isChecked = session_ids.contains(id) ? true : false;
								isChecked = !isChecked;
//                                checks.put(id, isChecked);
								if (isChecked) {
									check.setVisibility(View.VISIBLE);
									session_ids.add(id);
								} else {
									check.setVisibility(View.INVISIBLE);
									session_ids.remove(id);
								}
							}
						});
					}
				};
			}
		};
	}

	@Override
	protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = super.createView(inflater, container, savedInstanceState);
//        TextView headline = (TextView) view.findViewById(R.id.variant);
//        headline.setText(variantName);
		TextView subhead = (TextView) view.findViewById(R.id.date);
		subhead.setText(day.getDisplayName());
		session_ids = new HashSet<>();
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
		item.setTitle(R.string.confirm);
		item.setIcon(null);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.nav_add) {
			if (session_ids.size() > 0) {
				BaseMainActivity activity = (BaseMainActivity) getBaseActivity();
				DepositFragment fragment = new DepositFragment();
				fragment.setSessionIds(session_ids);
				activity.changeFragment(fragment, true);
			} else {
				DialogUtil.showErrorAlert(getContext(), getString(R.string.no_slot_select), null);
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected int getLayoutId() {
		return R.layout.fragment_time_slot;
	}

	@Override
	protected int getListId() {
		return R.id.list;
	}

	@Override
	protected String getTitle(Resources res) {
		return variant.getName();
	}

	@Override
	protected AppBarLayout getToolbar(LayoutInflater inflater, ViewGroup container) {
		return createToolbarFrom(R.layout.toolbar_small);
	}

	public TimeSlotListFragment setDay(BookingDayData day) {
		this.day = day;
		return this;
	}

	public TimeSlotListFragment setFacility(FacilityData facility) {
		this.facility = facility;
		return this;
	}

	public TimeSlotListFragment setVariant(FacilityVariantData variant) {
		this.variant = variant;
		return this;
	}

	public TimeSlotListFragment setHour(List<BookingHourData> hourList) {
		this.hourList = hourList;
		return this;
	}
}
