package com.orvibo.homemate.common;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.smartgateway.app.WeijuHelper;

import ru.johnlife.lifetools.activity.BaseMainActivity;
import ru.johnlife.lifetools.adapter.BaseAdapter;
import ru.johnlife.lifetools.data.AbstractData;
import ru.johnlife.lifetools.fragment.BaseListFragment;

/**
 * SmartCommunityFragment
 * Created by MDev on 12/14/16.
 */

public class HistoryFragment extends BaseListFragment<HistoryFragment.RecordItem> {
    class RecordItem extends AbstractData {
        int iconId;
        String name;
        public RecordItem(int iconId, String name) {
            this.iconId = iconId;
            this.name = name;
        }

        public int getIconId() {
            return iconId;
        }

        public String getName() {
            return name;
        }
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.createView(inflater, container, savedInstanceState);
        BaseAdapter<RecordItem> adapter = getAdapter();
        adapter.add(new RecordItem(R.drawable.ic_guest, getString(R.string.intercom_call_record)));
        adapter.add(new RecordItem(R.drawable.ic_arrive, getString(R.string.unlock_records)));
        return view;
    }

    @Override
    protected BaseAdapter<RecordItem> instantiateAdapter(Context context) {
        return new BaseAdapter<RecordItem>(R.layout.item_service) {
            @Override
            protected ViewHolder<RecordItem> createViewHolder(final View view) {
                return new ViewHolder<RecordItem>(view) {
                    private ImageView icService = (ImageView) view.findViewById(R.id.ic_service);
                    private TextView txtServiceName = (TextView) view.findViewById(R.id.txt_service_name);
                    private View btnNext = view.findViewById(R.id.btn_next);
                    @Override
                    protected void hold(final RecordItem item) {
                        icService.setImageResource(item.getIconId());
                        txtServiceName.setText(item.getName());
                        view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ((BaseMainActivity)getBaseActivity()).changeFragment(UsersFragment.newInstance(item.getName()), true);
                            }
                        });
                    }
                };
            }
        };
    }

    @Override
    protected AppBarLayout getToolbar(LayoutInflater inflater, ViewGroup container) {
        return null;
    }

    @Override
    protected String getTitle(Resources res) {
        return "";
    }
}