package com.orvibo.homemate.user.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.user.LoginActivity;
import com.orvibo.homemate.util.StringUtil;

import java.util.List;

public class LoginAccountAdapter extends BaseAdapter {
    private Context context;
    private List<String> accounts;
    private View.OnClickListener onClickListener;
    private boolean isFocus = true;

    public LoginAccountAdapter(Context context, List<String> accounts) {
        this.context = context;
        this.accounts = accounts;
    }

    public LoginAccountAdapter(Context context, List<String> accounts, boolean isFocus) {
        this.context = context;
        this.accounts = accounts;
        this.isFocus = isFocus;
    }

    // 获取数据量
    @Override
    public int getCount() {
        return accounts.size();
    }

    // 获取对应ID项对应的Item
    @Override
    public Object getItem(int position) {
        return accounts.get(position);
    }

    // 获取对应项ID
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(parent.getContext(), R.layout.login_account_item, null);
            holder.accountTextView = (TextView) convertView.findViewById(R.id.accountTextView);
            holder.deleteImageView = (ImageView) convertView.findViewById(R.id.deleteImageView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        String account = accounts.get(position);
        if (isFocus) {
            holder.accountTextView.setText(StringUtil.hideUserNameMiddleWord(account));
        }else{
            holder.accountTextView.setText(account);
        }
        holder.deleteImageView.setOnClickListener(onClickListener);
        holder.deleteImageView.setTag(account);
        if (!isFocus) {
            holder.deleteImageView.setVisibility(View.GONE);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.setTag(R.id.accountTextView, getItem(position));
                    ((LoginActivity) context).onPopupWindowItemClick(v, position);
                }
            });
        }
        return convertView;
    }

    // ViewHolder用于缓存控件
    class ViewHolder {
        public TextView accountTextView;
        public ImageView deleteImageView;
    }

    public void setOnDeleteListener(View.OnClickListener onDeleteListener) {
        this.onClickListener = onDeleteListener;
    }

    public void setData(List<String> accounts) {
        this.accounts = accounts;
    }
    public void removedAccout(String accout){
        if(accounts!=null&&!accounts.isEmpty()){
            accounts.remove(accout);
        }
    }
}
