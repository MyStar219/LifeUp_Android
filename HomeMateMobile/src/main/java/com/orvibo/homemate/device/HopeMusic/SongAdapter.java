package com.orvibo.homemate.device.HopeMusic;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.device.HopeMusic.Bean.Song;
import com.orvibo.homemate.device.HopeMusic.listener.OnSongChangeListener;

import java.util.ArrayList;

import cn.nbhope.smarthomelib.app.type.HopeCommandType;

/**
 * Created by wuliquan on 2016/5/16.
 */
public class SongAdapter extends BaseAdapter {
    private Context mcontext;
    private LayoutInflater mInflater;
    private OnSongChangeListener listener;
    public ArrayList<Song> songList = new ArrayList<Song>();
    /**构造函数*/
    public SongAdapter(Context context, ArrayList<Song> songList1) {
        this.mcontext = context;
        this.mInflater = LayoutInflater.from(context);
        if (songList1!=null) {
            this.songList.clear();
            this.songList.addAll(songList1);
        }
    }
    public void setOnSongChangeListener(OnSongChangeListener listener){
        this.listener=listener;
    }

    public void setPlayIndex(int index){
        setSongListIndexSelect(index);
        notifyDataSetChanged();
    }
   public void freshSongList(ArrayList<Song> list){
       if (list!=null) {
           this.songList.clear();
           this.songList.addAll(list);
           notifyDataSetChanged();
       }
   }
    public void loadMoreList(ArrayList<Song> list){
        if (list!=null) {
            this.songList.addAll(list);
            notifyDataSetChanged();
        }
    }
    public void setPaly(){
        Song song = getSongSelect();
        if(song!=null)
        song.setPlaying(true);
        notifyDataSetChanged();
    }
    public void setPause(){
        Song song = getSongSelect();
        if(song!=null)
        song.setPlaying(false);
        notifyDataSetChanged();
    }

    public ArrayList<Song> getSongList(){
        return songList;
    }
    //设备掉线情况
    public void disConnectServer(){
        if(songList!=null){
            int size =songList.size();
            for(Song song:songList){
                song.setSelect(false);
                song.setPlaying(false);
            }
        }
        notifyDataSetChanged();
    }
    @Override
    public int getCount() {
        return songList.size();
    }

    @Override
    public Object getItem(int i) {
        return songList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    private class ViewHolder{
        private TextView songName;
        private TextView singer_name_tv;
        private ImageView animationView;
    }
    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if(view==null){
            view =  mInflater.inflate(R.layout.item_music_song,null);
            viewHolder = new ViewHolder();
            viewHolder.songName = (TextView)view.findViewById(R.id.song_name_tv);
            viewHolder.singer_name_tv= (TextView)view.findViewById(R.id.singer_name_tv);
            viewHolder.animationView = (ImageView)view.findViewById(R.id.animation_im);
            view.setTag(viewHolder);
        }else{
            viewHolder= (ViewHolder) view.getTag();
        }
        viewHolder.songName.setText(songList.get(i).getTitle());
        if(songList.get(i).getArtist().contains("unknown")){
            viewHolder.singer_name_tv.setText(mcontext.getResources().getString(R.string.unknown));
        }else{
            viewHolder.singer_name_tv.setText(songList.get(i).getArtist());
        }

        if(songList.get(i).isSelect()){
            viewHolder.songName.setTextColor(mcontext.getResources().getColor(R.color.green));
            viewHolder.singer_name_tv.setTextColor(mcontext.getResources().getColor(R.color.green));
            viewHolder.animationView.setVisibility(View.VISIBLE);
            viewHolder.animationView.setBackgroundResource(R.drawable.anim);
            AnimationDrawable animation = (AnimationDrawable) viewHolder.animationView.getBackground();
            if (songList.get(i).isPlaying()){
                animation.start();
            }else{
                animation.stop();
            }

        }else{
            viewHolder.songName.setTextColor(mcontext.getResources().getColor(R.color.black));
            viewHolder.singer_name_tv.setTextColor(mcontext.getResources().getColor(R.color.device_name_en_color));
            viewHolder.animationView.setVisibility(View.GONE);
            viewHolder.animationView.setBackgroundResource(0);
        }
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    if (listener!=null){
                        /**
                         *     public static String HOPECOMMAND_TYPE_MUSICPLAY = "MusicPlay";
                               public static String HOPECOMMAND_TYPE_MUSICPLAYEX = "MusicPlayEx";
                               public static String HOPECOMMAND_TYPE_MUSICPAUSE = "MusicPause";
                         */
                        if(songList.get(i).isSelect()){
                            if (songList.get(i).isPlaying()){
                                listener.change(HopeCommandType.HOPECOMMAND_TYPE_MUSICPAUSE,true,songList.get(i));}
                            else{
                                listener.change(HopeCommandType.HOPECOMMAND_TYPE_MUSICPLAY,true,songList.get(i));
                            }
                        }else{
                            listener.change(HopeCommandType.HOPECOMMAND_TYPE_MUSICPLAYEX,true,songList.get(i));
                        }

                    }
            }
        });
        return view;
    }

    private void setSongListIndexSelect(int position){
        if(songList!=null){
            int size= songList.size();
            for (int i=0;i<size;i++){
                if(position==i){
                    songList.get(i).setPlaying(true);
                    songList.get(i).setSelect(true);
                }else{
                    songList.get(i).setPlaying(false);
                    songList.get(i).setSelect(false);
                }

            }
        }
    }

    public Song getSongSelect(){
        if(songList!=null){
            int size= songList.size();
            for (int i=0;i<size;i++){
                if(songList.get(i).isSelect()){
                   return songList.get(i);
                }

            }
        }
        return null;
    }
}
