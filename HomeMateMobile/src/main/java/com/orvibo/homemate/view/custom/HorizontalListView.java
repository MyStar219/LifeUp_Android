package com.orvibo.homemate.view.custom;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.smartgateway.app.R;

public class HorizontalListView extends RecyclerView
{
    //被高亮显示的条目下标
    private int checkIndex;
    private View currentView;
    private View temporaryView;
    private Handler handler =new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what==0){
                if(mItemScrollChangeListener!=null){
                    View view = (View) msg.obj;
                    Object object = view.getTag();
                    if(object!=null){
                       int tag =  (int)object;
                        if(tag==-1){
                            return;
                        }
                        mItemScrollChangeListener.onChange(view,tag );
                    }

                }
            }
        }
    };
    public int getPosition() {
        if(currentView!=null) {
            return (int) currentView.getTag();
        }
        return -1;
    }


    public void setCheckIndex(int index){
        checkIndex = index;
    }
    public int getCheckIndex(){
        return checkIndex;
    }
    private OnItemScrollChangeListener mItemScrollChangeListener;

    public void setOnItemScrollChangeListener(
            OnItemScrollChangeListener mItemScrollChangeListener)
    {
        this.mItemScrollChangeListener = mItemScrollChangeListener;
    }
    public void setCheckPosition(int position){
        if(position<0){
            return;
        }
        smoothScrollToPosition(position);

    }
    public interface OnItemScrollChangeListener
    {
        void onChange(View view, int position);
    }

    public HorizontalListView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        this.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int size=getChildCount();

                boolean isGetCheck=false;
                boolean isSetCheck=false;
                for(int i=0;i<size;i++){
                    ViewGroup newView = (ViewGroup) getChildAt(i);
                    RectProgressBar view= (RectProgressBar)newView.getChildAt(0);
                    TextView data =(TextView)newView.getChildAt(1);
                    if(newView.getLeft()>checkIndex*newView.getWidth()-newView.getWidth()/2&&newView.getLeft()<checkIndex*newView.getWidth()+newView.getWidth()/2) {
                        currentView = view;
                        if (view.getCheck()){
                            isGetCheck=true;
                            isSetCheck=true;
                        }else{
                            isSetCheck=true;
                            view.setCheck(true);
                            data.setTextColor(getResources().getColor(R.color.temprature_green));
                        }


                    }else{
                        if (view.getCheck()){
                            isGetCheck=true;
                        }
                        view.setCheck(false);
                        data.setTextColor(getResources().getColor(R.color.energy_bar));
                    }

                    if(isGetCheck&&isSetCheck){
                        break;
                    }
                }
                if(currentView!=temporaryView) {
                    temporaryView = currentView;
                    Message msg = new Message();
                    msg.what = 0;
                    msg.obj = currentView;
                    handler.sendMessage(msg);
                }
            }
        });
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        super.onLayout(changed, l, t, r, b);

        int size=getChildCount();
        for(int i=0;i<size;i++){
            ViewGroup newView = (ViewGroup) getChildAt(i);
            RectProgressBar view= (RectProgressBar)newView.getChildAt(0);
            TextView date =(TextView)newView.getChildAt(1);
            if(newView.getLeft()>checkIndex*newView.getWidth()-newView.getWidth()/2&&newView.getLeft()<checkIndex*newView.getWidth()+newView.getWidth()/2) {
                currentView = view;
                view.setCheck(true);
                date.setTextColor(getResources().getColor(R.color.temprature_green));

            }else{
                view.setCheck(false);
                date.setTextColor(getResources().getColor(R.color.energy_bar));
            }
        }

        if(currentView!=temporaryView) {
            temporaryView = currentView;
            Message msg = new Message();
            msg.what = 0;
            msg.obj = currentView;
            handler.sendMessage(msg);
        }

    }




}
