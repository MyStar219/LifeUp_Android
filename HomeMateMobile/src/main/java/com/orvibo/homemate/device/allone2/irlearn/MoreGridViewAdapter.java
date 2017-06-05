package com.orvibo.homemate.device.allone2.irlearn;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Action;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.KKIr;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.device.allone2.listener.OnIrKeyLongClickListener;
import com.orvibo.homemate.device.allone2.listener.OnKeyClickListener;
import com.orvibo.homemate.view.custom.IrKeyButton;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Smagret
 * @date 2016/04/01
 * allone 用于显示电视、机顶盒界面除主界面、数字界面外的其他按键
 * update by yuwei
 */
public class MoreGridViewAdapter extends BaseAdapter {
    private final String TAG = MoreGridViewAdapter.class.getSimpleName();

    //最大按钮数为30个，30个后不可以再添加
    private static final int MAXBUTTONCOUNT = 30;

    private List<KKIr> irKeys;
    private Context mContext;
    private boolean isStartLearn;
    private boolean isMaxButtonCount = false;
    private OnKeyClickListener clickListener;
    private OnIrKeyLongClickListener mOnIrKeyLongClickListener;

    private Device device;

    protected boolean isAction = false;//定时倒计时
    protected Action action;

    public void setIrKeys(List<KKIr> irKeys, boolean isLearn) {
        if (irKeys == null) {
            this.irKeys = new ArrayList<>();
        } else {
            this.irKeys = irKeys;
        }
        isMaxButtonCount = this.irKeys.size() == MAXBUTTONCOUNT;
        if (isLearn && this.irKeys.size() < MAXBUTTONCOUNT) {
            KKIr kkIr = new KKIr();
            this.irKeys.add(kkIr);
        }
    }

    /**
     * 更新数据
     *
     * @param irKeys
     * @param isLearn
     */
    public void updateData(List<KKIr> irKeys, boolean isLearn) {
        setIrKeys(irKeys, isLearn);
        this.isStartLearn = isLearn;
        notifyDataSetChanged();
    }


    public MoreGridViewAdapter(Context context, List<KKIr> irKeys, boolean isLearn, OnKeyClickListener clickListener, OnIrKeyLongClickListener mOnIrKeyLongClickListener, Device device) {
        this.mContext = context;
        setIrKeys(irKeys, isLearn);
        this.isStartLearn = isLearn;
        this.clickListener = clickListener;
        this.mOnIrKeyLongClickListener = mOnIrKeyLongClickListener;
        this.device = device;
    }

    @Override
    public int getCount() {
        return irKeys == null ? 0 : irKeys.size();
    }

    @Override
    public KKIr getItem(int position) {
        return irKeys.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        KKIr irKey = getItem(position);
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_custom_adapter, null);
            holder.irKeyButton = (IrKeyButton) convertView.findViewById(R.id.irKeyButton);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (isAction && action != null && action.getValue2() == irKey.getFid()) {
            holder.irKeyButton.setSelected(true);
        } else {
            holder.irKeyButton.setSelected(false);
        }
        if (position == irKeys.size() - 1 && isStartLearn && position <= (MAXBUTTONCOUNT - 1) && !isMaxButtonCount) {
            holder.irKeyButton.setBackgroundResource(R.drawable.btn_custom_remote_control_add_selector);
            holder.irKeyButton.setText("");
            holder.irKeyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent addKeyIntent = new Intent(mContext, EditIrButtonActivity.class);
                    addKeyIntent.putExtra(IntentKey.DEVICE, device);
                    addKeyIntent.putExtra(IntentKey.ALLONE_LEARN_RID, Integer.parseInt(device.getIrDeviceId()));
                    addKeyIntent.putExtra(EditIrButtonActivity.OPERATION_TYPE, EditIrButtonActivity.OPERATION_TYPE_ADD);
                    mContext.startActivity(addKeyIntent);
                }
            });
        } else {
            holder.irKeyButton.setLearned(!TextUtils.isEmpty(irKey.getPluse()));
            holder.irKeyButton.setStartLearn(isStartLearn);
            holder.irKeyButton.setKkIr(irKey);
            holder.irKeyButton.setText(irKey.getfName());
            final IrKeyButton irKeyButton = holder.irKeyButton;
            holder.irKeyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.OnClick(irKeyButton);
                }
            });
            holder.irKeyButton.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mOnIrKeyLongClickListener.onLongClick(irKeyButton);
                    return false;
                }
            });
        }
        return convertView;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public void setAction(boolean action) {
        isAction = action;
    }

    private static class ViewHolder {
        IrKeyButton irKeyButton;
    }
}
