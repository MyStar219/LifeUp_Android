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
import com.smartgateway.app.fragment.WaveInvitationFragment;

import ru.johnlife.lifetools.activity.BaseMainActivity;
import ru.johnlife.lifetools.adapter.BaseAdapter;
import ru.johnlife.lifetools.data.AbstractData;
import ru.johnlife.lifetools.fragment.BaseListFragment;

/**
 * SmartCommunityFragment
 * Created by MDev on 12/14/16.
 */

public class ServicesFragment extends BaseListFragment<ServicesFragment.ServiceData> {
    class ServiceData extends AbstractData {
        int iconId;
        String name;
        int type;
        public ServiceData(int type, int iconId, String name) {
            this.type = type;
            this.iconId = iconId;
            this.name = name;
        }

        public int getType() {
            return type;
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
        BaseAdapter<ServiceData> adapter = getAdapter();
        adapter.add(new ServiceData(0, R.drawable.ic_tmp_service1, getString(R.string.wave_invitation)));
        adapter.add(new ServiceData(1, R.drawable.ic_tmp_service2, "Facial Recognition"));
        return view;
    }

    @Override
    protected BaseAdapter<ServiceData> instantiateAdapter(Context context) {
        return new BaseAdapter<ServiceData>(R.layout.item_service) {
            @Override
            protected ViewHolder<ServiceData> createViewHolder(final View view) {
                return new ViewHolder<ServiceData>(view) {
                    private ImageView icService = (ImageView) view.findViewById(R.id.ic_service);
                    private TextView txtServiceName = (TextView) view.findViewById(R.id.txt_service_name);
                    private View btnNext = view.findViewById(R.id.btn_next);
                    @Override
                    protected void hold(final ServiceData item) {
                        icService.setImageResource(item.getIconId());
                        txtServiceName.setText(item.getName());
                        view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                switch (item.getType()) {
                                    case 0:
                                        ((BaseMainActivity) getBaseActivity()).changeFragment(new WaveInvitationFragment(), true);
                                        break;
                                    case 1:
                                        ((BaseMainActivity) getBaseActivity()).changeFragment(new FacialRecognitionFragment(), true);
                                         break;

                                }
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