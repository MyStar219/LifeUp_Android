package com.orvibo.homemate.view.custom;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.smartgateway.app.R;

/**
 * Created by yuwei on 2016/4/1.
 * 自定义allone2界面的对话框
 */
public class Allone2TipsDialog extends Dialog implements View.OnClickListener{

    private Context mContext;
    private String title;

    private TextView tv_title;
    private TextView tv_the_rightOne;
    private TextView tv_another_one;

    private Allone2DialogCliclListener allone2DialogCliclListener;

    public Allone2TipsDialog(Context context,String titleText,Allone2DialogCliclListener allone2DialogCliclListener) {
        super(context);
        mContext = context;
        this.title = titleText;
        this.allone2DialogCliclListener = allone2DialogCliclListener;
        setCanceledOnTouchOutside(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);//设置dialog圆角效果
        setContentView(R.layout.dialog_allone2_control_tips);

        initView();
        initListener();
    }

    private void initView(){
        tv_title = (TextView)findViewById(R.id.tv_allone2_title);
        tv_the_rightOne = (TextView)findViewById(R.id.tv_btn_rightone);
        tv_another_one = (TextView)findViewById(R.id.tv_btn_another);
        tv_title.setText(title);
    }

    private void initListener(){
        tv_the_rightOne.setOnClickListener(this);
        tv_another_one.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_btn_rightone:
                allone2DialogCliclListener.topTextViewClick();
                break;
            case R.id.tv_btn_another:
                allone2DialogCliclListener.bottomTextViewClick();
                break;
        }
        dismiss();
    }

    public interface Allone2DialogCliclListener{
        public void topTextViewClick();
        public void bottomTextViewClick();
    }
}
