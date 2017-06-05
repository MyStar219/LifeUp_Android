package com.smartgateway.app.fragment;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import com.evideo.weiju.callback.InfoCallback;
import com.evideo.weiju.info.CommandError;
import com.evideo.weiju.info.Info;
import com.evideo.weiju.info.unlock.UnlockWaveInfo;
import com.smartgateway.app.R;
import com.smartgateway.app.Utils.DialogUtil;
import com.smartgateway.app.WeijuHelper;

public class WaveDialogFragment extends DialogFragment implements View.OnClickListener {

    private UnlockWaveInfo unlockWaveInfo;
    private DialogUtil dialogUtil;
    WaveInvitationFragment parent;
    public static WaveDialogFragment newInstance(UnlockWaveInfo unlockWaveInfo, WaveInvitationFragment parent) {

        WaveDialogFragment dialog = new WaveDialogFragment();
        dialog.parent = parent;
        Bundle args = new Bundle();
        args.putSerializable("unlock_wave_info", unlockWaveInfo);
        dialog.setArguments(args);

        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            unlockWaveInfo = (UnlockWaveInfo) getArguments().getSerializable("unlock_wave_info");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_wave, container, false);

        dialogUtil = new DialogUtil(getActivity());

        view.findViewById(R.id.btn_play).setOnClickListener(this);
        view.findViewById(R.id.btn_share).setOnClickListener(this);
        view.findViewById(R.id.btn_delete).setOnClickListener(this);
        view.findViewById(R.id.btn_cancel).setOnClickListener(this);

        Window window = getDialog().getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);

        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_share:
                share();
                break;
            case R.id.btn_play:
                playWave();
                break;
            case R.id.btn_cancel:
                closeDialog();
                break;
            case R.id.btn_delete:
                deleteWave();
                break;
        }
    }

    private void deleteWave() {
        dialogUtil.showProgressDialog();
        WeijuHelper weijuHelper = new WeijuHelper();
        weijuHelper.deleteWave(getContext().getApplicationContext(), unlockWaveInfo, new InfoCallback<Info>() {
            @Override
            public void onSuccess(Info info) {
                cancelProgress();
                Toast.makeText(getActivity(), R.string.wave_deleted, Toast.LENGTH_SHORT).show();
                closeDialog();
            }

            @Override
            public void onFailure(CommandError error) {
                handleFailure(error);
                closeDialog();
            }
        });
    }

    public void closeDialog() {
        cancelProgress();
        dismiss();
        parent.reload();
    }

    public void cancelProgress() {
        if (dialogUtil != null) {
            dialogUtil.dismissProgressDialog();
        }
    }

    private void share() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");

        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.unlock_wave_link) + unlockWaveInfo.getAudio_url());
        startActivity(Intent.createChooser(intent, getString(R.string.share_unlock_wave_title)));
        closeDialog();
    }

    private void playWave() {
        dialogUtil.showProgressDialog();

        try {
            Log.d("Dashboard", "start download and play wave url " + unlockWaveInfo.getAudio_url());
            final MediaPlayer player = new MediaPlayer();
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setDataSource(unlockWaveInfo.getAudio_url());
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    cancelProgress();
                    player.start();
                    closeDialog();
                }
            });
            player.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    Log.e("Dashboard", "media player error : " + what + " extra: " + extra);
                    handleFailure(null);
                    closeDialog();
                    return true;
                }
            });

            player.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
            handleFailure(null);
        }
    }

    public void handleFailure(CommandError error) {
        cancelProgress();

        if (error == null) {
            DialogUtil.showErrorAlert(getContext(), "Sorry, some error happened", null);
        } else {
            DialogUtil.showErrorAlert(getContext(), "Sorry, error happened " +
                                                    error.getStatus() +
                                                    " message:" +
                                                    error.getMessage(),
                                      null);
        }
    }
}
