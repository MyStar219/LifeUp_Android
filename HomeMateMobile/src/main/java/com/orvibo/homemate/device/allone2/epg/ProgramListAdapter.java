package com.orvibo.homemate.device.allone2.epg;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.smartgateway.app.R;
import com.orvibo.homemate.application.ViHomeApplication;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.data.IntentKey;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by yuwei on 2016/4/12.
 */
public class ProgramListAdapter extends BaseAdapter implements View.OnClickListener {


    public static final int TYPE_EMPTY_VIEW = 0;
    public static final int TYPE_CONTECT_VIEW = 1;
    public static final int TYPE_COUNT = 2;

    private Context mContext;
    private List<PairProgramHasChannelName> mSingleDataList;
    private LayoutInflater inflater;
    private NumberFormat numberFormat;
    private ChangeChannelListener mChangeChannelListener;//换台回调接口
    private Device device;

    public void setSingleDataList(List<PairProgramHasChannelName> singleDataList) {
        if (singleDataList == null)
            mSingleDataList = new ArrayList<>();
        else
            mSingleDataList = singleDataList;
    }

    public ProgramListAdapter(Context context, Device device, List<PairProgramHasChannelName> singleDataList, ChangeChannelListener changeChannelListener) {
        this.mContext = context;
        this.device = device;
        inflater = LayoutInflater.from(context);
        // 创建一个数值格式化对象
        numberFormat = NumberFormat.getInstance();
        // 设置精确到小数点后2位
        numberFormat.setMaximumFractionDigits(2);
        this.mChangeChannelListener = changeChannelListener;
        setSingleDataList(singleDataList);
    }

    public void changeData(List<PairProgramHasChannelName> singleDataList) {
        setSingleDataList(singleDataList);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mSingleDataList.size();
    }

    @Override
    public Object getItem(int position) {
        return mSingleDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return TYPE_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        PairProgramHasChannelName pairProgramHasChannelName = mSingleDataList.get(position);
        if (pairProgramHasChannelName.getChannelInfo()==null && pairProgramHasChannelName.getPairProgram()==null){
            return TYPE_EMPTY_VIEW;
        }
        return TYPE_CONTECT_VIEW;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int type = getItemViewType(position);
        switch (type){
            case TYPE_EMPTY_VIEW:
                EmptyViewHolder emptyViewHolder = null;
                if (null == convertView){
                    emptyViewHolder = new EmptyViewHolder();
                    convertView = inflater.inflate(R.layout.empty_list_item, null);
                    emptyViewHolder.emptyIcon = (ImageView) convertView.findViewById(R.id.icon);
                    emptyViewHolder.emptyText = (TextView) convertView.findViewById(R.id.title);
                    convertView.setTag(emptyViewHolder);
                }else {
                    emptyViewHolder = (EmptyViewHolder) convertView.getTag();
                }
                emptyViewHolder.emptyIcon.setImageResource(R.drawable.bg_no_device);
                emptyViewHolder.emptyText.setText(R.string.common_no_data);
                break;
            case TYPE_CONTECT_VIEW:
                ViewHolder holder = null;
                if (null == convertView) {
                    holder = new ViewHolder();
                    convertView = inflater.inflate(R.layout.epg_programs_list_item, null);
                    holder.iv_program = (ImageView) convertView.findViewById(R.id.iv_program);
                    holder.iconHd = (ImageView) convertView.findViewById(R.id.iconHd);
                    holder.iconFinish = (TextView) convertView.findViewById(R.id.iconFinish);
                    holder.change_channel = (TextView) convertView.findViewById(R.id.change_channel);
                    holder.tv_program_name = (TextView) convertView.findViewById(R.id.tv_program_name);
                    holder.tv_channel_name = (TextView) convertView.findViewById(R.id.tv_channel_name);
                    holder.pgb_program_progress = (ProgressBar) convertView.findViewById(R.id.pgb_program_progress);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }
                final PairProgramHasChannelName pairProgramHasChannelName = mSingleDataList.get(position);
                ImageLoader.getInstance().displayImage(pairProgramHasChannelName.getPairProgram().thumb, holder.iv_program, ViHomeApplication.getImageOptionsHasRoundAngle(R.drawable.pic_tv_program_default, 0));
                holder.tv_program_name.setText(pairProgramHasChannelName.getPairProgram().sn);
                holder.tv_channel_name.setText(pairProgramHasChannelName.getChannelInfo().name);
                if (pairProgramHasChannelName.getChannelInfo().isHd== 0)//标清
                    holder.iconHd.setVisibility(View.GONE);
                else
                    holder.iconHd.setVisibility(View.VISIBLE);
                holder.tv_channel_name.setOnClickListener(this);
                holder.tv_channel_name.setTag(pairProgramHasChannelName);
                holder.iv_program.setOnClickListener(this);
                holder.iv_program.setTag(pairProgramHasChannelName);
                holder.tv_program_name.setOnClickListener(this);
                holder.tv_program_name.setTag(pairProgramHasChannelName);
                //计算节目播放百分比
                Date nowDate = new Date();
                long playTime = nowDate.getTime() - pairProgramHasChannelName.getPairProgram().cdate.getTime();
                long totalPlayTime = pairProgramHasChannelName.getPairProgram().cedate.getTime() - pairProgramHasChannelName.getPairProgram().cdate.getTime();
                //计算百分比时做校验
                float p = (float) playTime / totalPlayTime;
                if (p > 1 || p < 0)
                    p = 1;
                String progress = numberFormat.format(p * 100);
                holder.iconFinish.setVisibility(p == 1 ? View.VISIBLE : View.GONE);
                float progressFloat = Float.parseFloat(progress);
                holder.pgb_program_progress.setProgress((int) progressFloat);
                holder.change_channel.setOnClickListener(this);
                holder.change_channel.setTag(pairProgramHasChannelName);
                break;
        }
        return convertView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_channel_name: {
                PairProgramHasChannelName pairProgramHasChannelName = (PairProgramHasChannelName) v.getTag();
                Intent intent = new Intent(mContext, ChannelDetailActivity.class);
                intent.putExtra(IntentKey.PAIR_PROGRAM_HAS_CHANNEL_NAME, pairProgramHasChannelName);
                intent.putExtra(IntentKey.DEVICE, device);
                mContext.startActivity(intent);
                break;
            }
            case R.id.change_channel: {
                PairProgramHasChannelName pairProgramHasChannelName = (PairProgramHasChannelName) v.getTag();
                mChangeChannelListener.changeChannelClick(pairProgramHasChannelName);
                break;
            }
            case R.id.iv_program:
            case R.id.tv_program_name:
                PairProgramHasChannelName pairProgramHasChannelName = (PairProgramHasChannelName) v.getTag();
                Intent intent = new Intent(mContext, ProgramDetailActivity.class);
                intent.putExtra(IntentKey.PAIR_PROGRAM_HAS_CHANNEL_NAME, pairProgramHasChannelName);
                intent.putExtra(IntentKey.DEVICE, device);
                mContext.startActivity(intent);
                break;
        }
    }

    class ViewHolder {
        ImageView iv_program;
        TextView iconFinish;//节目结束标示
        ImageView iconHd;
        TextView change_channel;
        TextView tv_program_name;
        TextView tv_channel_name;
        ProgressBar pgb_program_progress;
    }

    /**
     * 新增数据为空时的item
     */
    class EmptyViewHolder{
        ImageView emptyIcon;
        TextView emptyText;
    }
}
