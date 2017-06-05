package com.orvibo.homemate.device.control.adapter;

/**
 * Created by wuliquan on 2016/7/6.
 */

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.energy.EnergyUploadDay;
import com.orvibo.homemate.bo.energy.EnergyUploadMonth;
import com.orvibo.homemate.util.DisplayUtils;
import com.orvibo.homemate.view.custom.RectProgressBar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class EnergyMonthAdapter extends
        RecyclerView.Adapter<EnergyMonthAdapter.ViewHolder>
{
    private Context mContext;
    private float maxScale;
    private int isCheck;
    private int flag;
    //一屏显示的条目数
    private int countScreen = 3;
    /**
     * ItemClick的回调接口
     * @author zhy
     *
     */
    public interface OnItemClickLitener
    {
        void onItemClick(View view, int position);
    }

    private OnItemClickLitener mOnItemClickLitener;

    public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener)
    {
        this.mOnItemClickLitener = mOnItemClickLitener;
    }

    private LayoutInflater mInflater;
    private List<EnergyUploadMonth> mDatas =new ArrayList<>();

    public EnergyMonthAdapter(Context context,List< EnergyUploadMonth> datats)
    {   mContext =context;
        this.maxScale = calcuteMaxScale(getMonthMaxEnergy(datats));
        mInflater = LayoutInflater.from(context);
        packData(countScreen,datats);
    }

    public void freshData(List<EnergyUploadMonth> Datas){
        if(Datas!=null){
            this.maxScale = calcuteMaxScale(getMonthMaxEnergy(Datas));
            packData(countScreen,Datas);
            notifyDataSetChanged();
        }

    }
    public int getCountScreen(){
        return countScreen;
    }
    public EnergyUploadMonth getEnergyUploadMonth(int position){
        return mDatas.get(position);
    }
    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        public ViewHolder(View arg0)
        {
            super(arg0);
        }

        private RectProgressBar bar;
        public TextView title;
    }

    @Override
    public int getItemCount()
    {
        return mDatas.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
    {
        View view = mInflater.inflate(R.layout.item_energy_bar,
                viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.bar = (RectProgressBar)view.findViewById(R.id.bar);
        viewHolder.title = (TextView) view
                .findViewById(R.id.textView);

        ViewGroup.LayoutParams lp=viewHolder.title.getLayoutParams();
        lp.width = (int)calcuteItemWidth();
        viewHolder.title.setLayoutParams(lp);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int i)
    {



        if(!mDatas.get(i).getIsActive()){
            viewHolder.bar.setTag(-1);
            viewHolder.title.setText("");
            viewHolder.bar.setProgress(0);
        }else{
            viewHolder.bar.setTag(i);
            viewHolder.title.setText(decodeDate(mDatas.get(i).getMonth()));
            if(maxScale>0){
                String energy = mDatas.get(i).getEnergy();
                if(energy!=null) {
                    float value = Float.parseFloat(energy);
                    viewHolder.bar.setProgress( Float.parseFloat(decodeFloatValue(value))/ maxScale);
                }
                else{
                    viewHolder.bar.setProgress(0);
                }
            }else{
                viewHolder.bar.setProgress(0);
            }



            //如果设置了回调，则设置点击事件
            if (mOnItemClickLitener != null)
            {
                viewHolder.itemView.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {   int tag = (int) viewHolder.bar.getTag();
                        if(isCanClick(i)&tag!=-1){
                        mOnItemClickLitener.onItemClick(viewHolder.itemView, i);
                    }
                    }
                });

            }
        }


    }

    /**
     * 编码显示
     * @param data
     * @return
     */
   private String decodeDate(String data){
       if(data!=null&data.contains("-")){
           String[] time = data.split("-");
           Calendar c = Calendar.getInstance();
           int month_index = c.get(Calendar.MONTH);
           int year = c.get(Calendar.YEAR);
           if((year+"").equals(time[0])) {
               if (month_index + 1 == Integer.parseInt(time[1])) {
                   return mContext.getString(R.string.this_month);
               } else if (month_index == Integer.parseInt(time[1])) {
                   return mContext.getString(R.string.pre_month);
               }
           }
         return   data.replace("-","/");
       }
       return "";

   }

    /**
     * 根据一屏显示的条目数来设置item的宽度
     * @return
     */
    private float calcuteItemWidth(){
        float screenWidth = DisplayUtils.getScreenWidth(mContext);
        float marginLeft =DisplayUtils.dipToPx(mContext,50);
        float validityWidth =screenWidth-marginLeft;
        float indexWidth   = DisplayUtils.dipToPx(mContext,4);
        return (validityWidth-indexWidth*countScreen)/countScreen;

    }

    /**
     * 判断该item是否可点击
     * @param position
     * @return
     */
    private boolean isCanClick(int position){
        if (position<1||position>mDatas.size()-1){
            return false;
        }
        return true;
    }
    /**
     * 用来填充无效数据，实现左右端可以滑动到中间
     * @param flag
     */
    private void packData(int flag,List<EnergyUploadMonth> datas){
        if(datas!=null) {
            mDatas.clear();
            //向头部添加三个无效数据
            for (int i = 0; i < 1; i++) {
                EnergyUploadMonth energy = new EnergyUploadMonth();
                mDatas.add(energy);
            }
            mDatas.addAll(datas);
            //向尾部添加三个无效数据
            for (int i = 0; i < 1; i++) {
                EnergyUploadMonth energy = new EnergyUploadMonth();
                mDatas.add(energy);
            }
        }
    }
    private float getMonthMaxEnergy(List<EnergyUploadMonth> baseEnergyLis){
        float max=0f;
        if(baseEnergyLis!=null){
            for (EnergyUploadMonth energyUploadMonth:baseEnergyLis){
                String energy = energyUploadMonth.getEnergy();
                if(energy!=null){
                    float value= Float.parseFloat(energy);
                    if(value>max){
                        max =value;
                    }
                }
            }
            return max;
        }
        return max;
    }

    // 计算出最大电量刻度
    private float calcuteMaxScale(float maxValue) {

        // 最大刻度值
        float maxPowerScale = 0;

        // 求出位数
        int max = (int) maxValue;
        int max_length = String.valueOf(max).length();

        // 比例
        double scale = 0.03;

        // 小于0
        if (maxValue < 1&&maxValue>=0.01f) {
            scale = 0.03;
        }else if(maxValue <0.01f){
            scale = 0.003;
        }
        // 大于0
        else {
            for (int i = 0; i < max_length; i++) {
                scale = scale * 10;
            }
            // 如果大于 10 , 去掉小数, 然后＋1 eg: 10.99 ---> 11
            if (maxValue > 10) {
                maxValue = (int) maxValue + 1;
            }
        }
        int powerValue1_3 = (int) (maxValue / scale);

        if (powerValue1_3 * scale == maxValue) {
            maxPowerScale = maxValue;         // 如果除尽，则直接为最大刻度
        } else {
            maxPowerScale = (float)((powerValue1_3 + 1) * scale);
        }

        return maxPowerScale;
    }
    /**
     * 日/周/月/日均用电量≥0.01，小数点后最多显示2位，四舍五入，
     *（如果用电量为12/1.1,显示为12/1.1,而不是12.00/1.10）
     * <0.01,  小数点后最多显示4位，四舍五入
     *（如果用电量是0.03，显示为0.03,而不是0.030）
     * @return
     */
    private String decodeFloatValue(float value){
        String result = "";
        if(value>=0.01f){
            String value1 = (int)(value*100+0.5)+"";
            if(value1.length()==1) {
                result = "0.0"+value1;
            }else if(value1.length()==2){
                result = "0."+(value1.endsWith("0")?value1.substring(0,1):value1);
            }else{
                result = value1.substring(0,value1.length()-2)+"."+value1.substring(value1.length()-2,value1.length());

            }
        }else{
            String value1 = (int)(value*10000+0.5)+"";
            if(value1.length()==1) {
                result =  "0.000"+value1;
            }else {
                result = "0.00"+value1;
            }
        }
        return deleteEndZero(result);
    }

    /**、
     * 去掉字符串末尾的“0”
     * @param string
     * @return
     */
    private String deleteEndZero(String string){
        if(!string.contains(".")){
            return string;
        }
        if(string.endsWith("0")){
            String result = string.substring(0,string.length()-1);
            if(result.endsWith("0")){
                return deleteEndZero(result);
            }else if(result.endsWith(".")){
                return result.substring(0,result.length()-1);
            }
            else{
                return result;
            }
        }else{
            return string;
        }
    }

}
