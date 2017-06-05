package com.orvibo.homemate.device.allone2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.kookong.app.data.api.IrData;
import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Action;
import com.orvibo.homemate.data.AlloneControlData;
import com.orvibo.homemate.device.allone2.listener.OnKeyClickListener;
import com.orvibo.homemate.util.PhoneUtil;
import com.orvibo.homemate.view.custom.IrKeyButton;

import java.util.List;

/**
 * @author Smagret
 * @date 2016/04/01
 * allone 用于显示电视、机顶盒界面除主界面、数字界面外的其他按键
 */
public class MorekeyGridViewAdapter extends BaseAdapter {
    private List<IrData.IrKey> irKeys;
    private Context mContext;
    private int fre;
    private OnKeyClickListener clickListener;
    protected boolean isAction = false;//定时倒计时
    protected Action action;
    private boolean isUseRoundBg;//是否使用圆形的图标布局


    public MorekeyGridViewAdapter(Context context, List<IrData.IrKey> irKeys, int fre, OnKeyClickListener clickListener, boolean isUseRoundBg) {
        this.mContext = context;
        this.irKeys = irKeys;
        this.fre = fre;
        this.clickListener = clickListener;
        this.isUseRoundBg = isUseRoundBg;
    }


    /**
     * 更新数据
     *
     * @param irKeys
     * @param fre
     */
    public void updateData(List<IrData.IrKey> irKeys, int fre) {
        this.irKeys = irKeys;
        this.fre = fre;
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return irKeys == null ? 0 : irKeys.size();
    }

    @Override
    public Object getItem(int position) {
        return irKeys.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        IrData.IrKey irKey = irKeys.get(position);
        AlloneControlData data = new AlloneControlData(fre, irKey.pulse);
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            if (!isUseRoundBg)
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_custom_adapter, null);
            else
                convertView = LayoutInflater.from(mContext).inflate(R.layout.key_gv_item, null);
            holder.irKeyButton = (IrKeyButton) convertView
                    .findViewById(R.id.irKeyButton);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.irKeyButton.setControlData(data);
        holder.irKeyButton.setText(PhoneUtil.isCN(mContext) ? irKey.fname : irKey.fkey);
        holder.irKeyButton.setFid(irKey.fid);
        holder.irKeyButton.setTextColor(mContext.getResources().getColor(R.color.white));
        holder.irKeyButton.setTextColor(mContext.getResources().getColor(R.color.green));
        holder.irKeyButton.setMatched(true);
        if (isAction && action != null && action.getValue2() == irKey.fid) {
            holder.irKeyButton.setSelected(true);
        } else {
            holder.irKeyButton.setSelected(false);
        }
        final ViewHolder finalHolder = holder;
        holder.irKeyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.OnClick(finalHolder.irKeyButton);
            }
        });
        return convertView;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public void setAction(boolean action) {
        isAction = action;
    }

    private static class ViewHolder {
        private IrKeyButton irKeyButton;
    }
}
