package com.orvibo.homemate.device.allone2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.smartgateway.app.R;
import com.orvibo.homemate.view.custom.IrKeyButton;

/**
 * Created by snown on 2016/7/9.
 * 音响fragment
 */
public class SpeakerBoxControlFragment extends BaseAlloneControlFragment implements View.OnClickListener {

    private IrKeyButton irKeyButtonStop;//停止
    private IrKeyButton irKeyButtonPlay;//播放
    protected IrKeyButton irKeyButtonPause;//暂停
    protected IrKeyButton irKeyButtonRewind;//快退
    protected IrKeyButton irKeyButtonForward;//快进
    protected IrKeyButton irKeyButtonPrevious;//上一曲
    protected IrKeyButton irKeyButtonNext;//下一曲
    protected IrKeyButton irKeyButtonMute;//静音


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_speaker_box_control, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        initView(view);
        super.onViewCreated(view, savedInstanceState);
    }

    private void initView(View view) {
        irKeyButtonStop = (IrKeyButton) view.findViewById(R.id.irKeyButtonStop);
        irKeyButtonPlay = (IrKeyButton) view.findViewById(R.id.irKeyButtonPlay);
        irKeyButtonPause = (IrKeyButton) view.findViewById(R.id.irKeyButtonPause);
        irKeyButtonRewind = (IrKeyButton) view.findViewById(R.id.irKeyButtonRewind);
        irKeyButtonForward = (IrKeyButton) view.findViewById(R.id.irKeyButtonForward);
        irKeyButtonPrevious = (IrKeyButton) view.findViewById(R.id.irKeyButtonPrevious);
        irKeyButtonNext = (IrKeyButton) view.findViewById(R.id.irKeyButtonNext);
        irKeyButtonMute = (IrKeyButton) view.findViewById(R.id.irKeyButtonMute);
        mainIrKeyButtons.add(irKeyButtonStop);
        mainIrKeyButtons.add(irKeyButtonPlay);
        mainIrKeyButtons.add(irKeyButtonPause);
        mainIrKeyButtons.add(irKeyButtonRewind);
        mainIrKeyButtons.add(irKeyButtonForward);
        mainIrKeyButtons.add(irKeyButtonPrevious);
        mainIrKeyButtons.add(irKeyButtonNext);
        mainIrKeyButtons.add(irKeyButtonMute);
    }

}
