package com.smartgateway.app.fragment;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.evideo.weiju.callback.InfoCallback;
import com.evideo.weiju.info.CommandError;
import com.evideo.weiju.info.unlock.CreateUnlockWaveInfo;
import com.evideo.weiju.info.unlock.UnlockWaveInfo;
import com.evideo.weiju.info.unlock.UnlockWaveInfoList;
import com.smartgateway.app.R;
import com.smartgateway.app.Utils.DialogUtil;
import com.smartgateway.app.WeijuHelper;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import ru.johnlife.lifetools.fragment.BaseAbstractFragment;

public class WaveInvitationFragment extends BaseAbstractFragment {

    private RecyclerView recyclerView;
    private DialogUtil dialogUtil;

    private WaveAdapter adapter;

    @Override
    protected String getTitle(Resources res) {
        return res.getString(R.string.wave_invitation);
    }

    @Override
    protected AppBarLayout getToolbar(LayoutInflater inflater, ViewGroup container) {
        return createToolbarFrom(R.layout.toolbar_small);
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wave_invitation, container, false);

        dialogUtil = new DialogUtil(getActivity());
        recyclerView = (RecyclerView) view.findViewById(R.id.up_menu);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        showWaves();
    }

    public void showWaves() {
        Log.d("WaveInvitation", "start showing waves");
        final WeijuHelper weijuHelper = new WeijuHelper();
        weijuHelper.fetchWaves(getContext().getApplicationContext(), 1, new InfoCallback<UnlockWaveInfoList>() {
            @Override
            public void onSuccess(UnlockWaveInfoList unlockWaveInfoList) {
                final List<UnlockWaveInfo> list = unlockWaveInfoList.getList();

                UnlockWaveInfo plus = new UnlockWaveInfo();
                plus.setType(-1);
                list.add(0, plus);

                recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
                adapter = new WaveAdapter(list);
                recyclerView.setAdapter(adapter);

                fetchRestCounts(list, weijuHelper);
            }

            @Override
            public void onFailure(CommandError error) {
                DialogUtil.showErrorAlert(getContext(), "Sorry, can' fetch waves " +
                                                        error.getStatus() +
                                                        " message:" +
                                                        error.getMessage(),
                        null);
            }
        });
    }

    // Loop the List and update the Remaining Count
    public void fetchRestCounts(final List<UnlockWaveInfo> list, final WeijuHelper weijuHelper) {
        for (int i = 0; i < list.size(); i++) {
            // Skip if the first item is the Add Wave icon
            final UnlockWaveInfo unlockWaveInfo = list.get(i);
            if (unlockWaveInfo.getType() == -1) {
                continue;
            }

            final String waveId = String.valueOf(unlockWaveInfo.getId());
            weijuHelper.fetchWaveDetail(getContext().getApplicationContext(),
                    waveId, new InfoCallback<UnlockWaveInfo>() {//设置回调
                @Override
                public void onSuccess(UnlockWaveInfo result) {
                    Log.d("WaveInvitation", "left for id " + waveId + " rest count:" + result.getRest_count() +
                                            ", expiry: " + getDate(result.getExpired_time()));
                    unlockWaveInfo.setRest_count(result.getRest_count());
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onFailure(CommandError error) {
                    Log.e("WaveInvitation", "Sorry, can' fetch wave info for id " +waveId + " error:" +
                                    error.getStatus() +
                                    " message:" +
                                    error.getMessage());
                }
            });

            // todo uncomment to fix bug in api - simultanious api calls
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time*1000);
        String date = DateFormat.format("dd MMM yyyy k:mm:ss a", cal).toString();
        return date;
    }

    public void reload() {
        showWaves();
    }

    class WaveAdapter extends RecyclerView.Adapter<WaveAdapter.ViewHolder> {
        private List<UnlockWaveInfo> list;

        public class ViewHolder extends RecyclerView.ViewHolder {

            ImageView icon;
            TextView date;
            TextView unlockLeft;
            View board;

            public ViewHolder(View rootView) {
                super(rootView);
                icon = (ImageView)rootView.findViewById(R.id.icon);
                date = (TextView)rootView.findViewById(R.id.wave_date);
                unlockLeft = (TextView)rootView.findViewById(R.id.wave_unlocks_left);
                board = rootView.findViewById(R.id.board);
            }
        }

        public WaveAdapter(List<UnlockWaveInfo> list) {
            this.list = list;
        }

        @Override
        public WaveAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_wave, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final UnlockWaveInfo element = list.get(position);

            int type = element.getType();
            if (type == -1) {
                holder.icon.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.icon_wave_plus));
                holder.date.setVisibility(View.GONE);
                holder.unlockLeft.setVisibility(View.GONE);
            } else {
                holder.icon.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.icon_wave_blue));
                holder.date.setVisibility(View.VISIBLE);
                holder.unlockLeft.setVisibility(View.VISIBLE);
                holder.date.setText(String.valueOf(getDate(element.getCreate_time())));
                holder.unlockLeft.setText(getString(R.string.unlocks_left) + " " + element.getRest_count());
            }

            holder.board.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (element.getType() == -1) {
                        createWave();
                    } else {
                        WaveDialogFragment waveDialogFragment = WaveDialogFragment.newInstance(element, WaveInvitationFragment.this);
                        waveDialogFragment.show(getChildFragmentManager(), WaveDialogFragment.class.getSimpleName());
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }

    public void createWave() {
        dialogUtil.showProgressDialog();
        Log.d("WaveInvitation", "start creating wave");
        WeijuHelper weijuHelper = new WeijuHelper();
        weijuHelper.createUnlockWave(getContext(), 1, new InfoCallback<CreateUnlockWaveInfo>() {
            @Override
            public void onSuccess(CreateUnlockWaveInfo unlockWaveInfo) {
                dialogUtil.dismissProgressDialog();
                showWaves();
            }

            @Override
            public void onFailure(CommandError error) {
                dialogUtil.dismissProgressDialog();
                DialogUtil.showErrorAlert(getContext(), "Sorry, can' create wave " +
                                error.getStatus() +
                                " message:" +
                                error.getMessage(),
                        null);
            }
        });
    }
}
