package com.orvibo.homemate.room.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Gateway;
import com.orvibo.homemate.dao.FloorDao;
import com.orvibo.homemate.dao.GatewayDao;
import com.orvibo.homemate.dao.RoomDao;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.StringUtil;

import java.util.List;

/**
 * @author smagret
 * @date 2015/04/02
 */
public class HomeAdapter extends BaseAdapter {
    private static final String TAG = HomeAdapter.class.getSimpleName();
    private LayoutInflater mInflater;
    private List<String> uids;
    private RoomDao roomDao;
    private FloorDao floorDao;
    private GatewayDao gatewayDao;
    private int roomNo;
    private int floorNo;
    private String homeName;
    private String userName;
    private Context mContext;
    private String mCurrentMainUid;

    /**
     * @param context
     * @param uids
     */
    public HomeAdapter(Context context, List<String> uids, String curMainUid) {
        mContext = context;
        this.uids = uids;
        this.mCurrentMainUid = curMainUid;
        mInflater = LayoutInflater.from(context);
        roomDao = new RoomDao();
        floorDao = new FloorDao();
        gatewayDao = new GatewayDao();
        userName = UserCache.getCurrentUserName(mContext);

    }

    public void refresh(String curMainUid) {
        this.mCurrentMainUid = curMainUid;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return uids == null ? 0 : uids.size();
    }

    @Override
    public Object getItem(int position) {
        return uids == null ? null : uids.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.home_list_item, parent,
                    false);
            holder.imageViewEdit = (ImageView) convertView
                    .findViewById(R.id.imageViewEdit);
            holder.textViewHomeName = (TextView) convertView
                    .findViewById(R.id.textViewHomeName);
            holder.textViewContent = (TextView) convertView.findViewById(R.id.textViewContent);
            holder.checkBoxSelectHome = (CheckBox) convertView.findViewById(R.id.checkBoxSelectHome);
            holder.linearLayoutHomeMessage = (LinearLayout) convertView.findViewById(R.id.linearLayoutHomeMessage);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        String uid = uids.get(position);
        Gateway gateway = gatewayDao.selGatewayByUid(uid);
        if (gateway != null) {
            homeName = gateway.getUserName();
        }
        if (StringUtil.isEmpty(homeName)) {
            homeName = uid;
        }
        floorNo = floorDao.selFloorNo(uid);
        roomNo = roomDao.selRoomNo(uid);
        LogUtil.d(TAG, "getView() - uid=" + uid + " floorNo = " + floorNo + " roomNo = " + roomNo);

        String str = mContext.getResources().getString(
                R.string.home_maeeage);
        String homeMessage = String.format(str, floorNo, roomNo);
        holder.textViewHomeName.setText(homeName);
        holder.textViewContent.setText(homeMessage);
        holder.checkBoxSelectHome.setChecked(uid.equals(mCurrentMainUid));
        holder.checkBoxSelectHome.setTag(R.id.checkBoxSelectHome, uid);
        holder.linearLayoutHomeMessage.setContentDescription(uid + "|" + floorNo);
        holder.linearLayoutHomeMessage.setTag(R.id.tag_uid,uid);
        holder.imageViewEdit.setTag(R.id.imageViewEdit, uid);
//		convertView.setTag(R.id.tag_device, device);
        return convertView;
    }

    private class ViewHolder {
        private ImageView imageViewEdit;
        private TextView textViewHomeName;
        private TextView textViewContent;
        private CheckBox checkBoxSelectHome;
        private LinearLayout linearLayoutHomeMessage;
    }

}
