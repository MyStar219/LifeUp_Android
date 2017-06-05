package com.orvibo.homemate.device.allone2.epg;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.smartgateway.app.R;
import com.orvibo.homemate.application.ViHomeApplication;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuwei on 2016/4/12.
 */
public class ProgramChannelListAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private List<PairProgramHasChannelName> mPairProgramHasChannelNameList;
    private ChangeChannelListener mChangeChannelListener;

    public void setPairProgramHasChannelNameList(List<PairProgramHasChannelName> pairProgramHasChannelNameList) {
        if(pairProgramHasChannelNameList==null)
            this.mPairProgramHasChannelNameList = new ArrayList<>();
        else
            mPairProgramHasChannelNameList = pairProgramHasChannelNameList;
    }

    public ProgramChannelListAdapter(Context context,List<PairProgramHasChannelName> pairProgramHasChannelNameList,ChangeChannelListener changeChannelListener){
        inflater = LayoutInflater.from(context);
        mChangeChannelListener = changeChannelListener;
        setPairProgramHasChannelNameList(pairProgramHasChannelNameList);
    }

    public void changeData(List<PairProgramHasChannelName> pairProgramHasChannelNameList){
        setPairProgramHasChannelNameList(pairProgramHasChannelNameList);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mPairProgramHasChannelNameList.size();
    }

    @Override
    public Object getItem(int position) {
        return mPairProgramHasChannelNameList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mPairProgramHasChannelNameList.get(position).getChannelInfo().channelId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if(convertView==null){
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.program_chennal_list_item,null);
            holder.iv_program_chennal_icon = (ImageView) convertView.findViewById(R.id.iv_program_chennal_icon);
            holder.tv_program_chennal_name = (TextView) convertView.findViewById(R.id.tv_program_chennal_name);
            holder.tv_program_name = (TextView) convertView.findViewById(R.id.tv_program_name);
            //holder.tv_program_chennal_id = (TextView) convertView.findViewById(R.id.tv_program_chennal_id);
            holder.change_channel = (TextView) convertView.findViewById(R.id.change_channel);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        final PairProgramHasChannelName pairProgramHasChannelName = mPairProgramHasChannelNameList.get(position);
        ImageLoader.getInstance().displayImage(pairProgramHasChannelName.getChannelInfo().logo, holder.iv_program_chennal_icon, ViHomeApplication.getImageOptionsHasRoundAngle(R.drawable.pic_tv_station_default,0));
//        ImageLoader.getInstance().displayImage(pairProgramHasChannelName.getChannelInfo().logo,holder.iv_program_chennal_icon);

        holder.tv_program_name.setText(pairProgramHasChannelName.getPairProgram().sn);
        //holder.tv_program_chennal_id.setText(pairProgramHasChannelName.getChannelInfo().num+"");
        if (pairProgramHasChannelName.getChannelInfo().isHd==ProgramGuidesFragment.HD_PROGRAM){
            holder.tv_program_chennal_name.setText(pairProgramHasChannelName.getChannelInfo().name+"(HD)");
        }else{
            holder.tv_program_chennal_name.setText(pairProgramHasChannelName.getChannelInfo().name);
        }
        holder.change_channel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChangeChannelListener.changeChannelClick(pairProgramHasChannelName);
            }
        });
        return convertView;
    }

    class ViewHolder{
        ImageView iv_program_chennal_icon;
        TextView tv_program_chennal_name;
        TextView tv_program_name;
        //TextView tv_program_chennal_id;
        TextView change_channel;
    }
}
