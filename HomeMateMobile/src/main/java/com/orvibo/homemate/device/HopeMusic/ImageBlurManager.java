package com.orvibo.homemate.device.HopeMusic;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;
import com.smartgateway.app.R;
import com.orvibo.homemate.device.HopeMusic.widget.CircleImageView;
import com.videogo.universalimageloader.utils.L;

import org.json.JSONException;

import cz.msebera.android.httpclient.Header;

/**
 * Created by wuliquan on 2016/5/15.
 */
public class ImageBlurManager {
    private static ImageView songImagView;
    private static CircleImageView img;
    private static ImageView imageView;
    private static LinearLayout linearLayout;

    public ImageBlurManager(LinearLayout linearLayout,CircleImageView image){
        this.img = image;
        this.linearLayout=linearLayout;
    }
    public ImageBlurManager(ImageView imageView){
        this.songImagView = imageView;
    }
    public ImageBlurManager(ImageView imageView, CircleImageView image) {
          this.img = image;
         this.imageView=imageView;
    }

    private static class BlurAsyncTask extends AsyncTask<Bitmap, Integer, Bitmap> {

        @Override
        protected Bitmap doInBackground(Bitmap... params) {
            Bitmap blurBitmap = FastBlurUtil.doBlur(params[0],20,false);
            return blurBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            imageView.setImageBitmap(bitmap);
            imageView=null;
        }
    }

    /**
     * @param url
     *            要下载的文件URL
     * @throws Exception
     */
    public static void downloadFile(String url) throws Exception {

        try{
            String imgUrl = MusicConstant.IMG_URL+url;
            AsyncHttpClient client = new AsyncHttpClient();
            client.setTimeout(10 * 1000);// 超时时间10s
            client.get(imgUrl, new AsyncHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(responseBody, 0,
                            responseBody.length);
                    if(bitmap!=null){
                        if(linearLayout!=null){
                            Drawable drawable =new BitmapDrawable(bitmap);
                            linearLayout.setBackgroundDrawable(drawable);
                            linearLayout = null;
                        }
                        if(songImagView!=null) {
                            songImagView.setImageBitmap(bitmap);
                            songImagView=null;
                        }
                        if (img!=null){
                            img.setImageBitmap(bitmap);
                            new BlurAsyncTask().execute(bitmap);
                            img=null;
                        }

                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    if(songImagView!=null){
                        songImagView.setBackgroundResource(R.drawable.bg_portrait);
                        songImagView=null;
                    }
                    if (imageView!=null){
                        imageView.setBackgroundResource(R.drawable.bg_music);
                        imageView=null;
                    }
                    if(linearLayout!=null){
                        linearLayout.setBackgroundResource(R.drawable.bg_music);
                        linearLayout=null;
                    }


                }
            });
        }
        catch (Exception e){
            if(songImagView!=null){
                songImagView.setBackgroundResource(R.drawable.bg_portrait);
                songImagView=null;
            }
            if (imageView!=null){
                imageView.setBackgroundResource(R.drawable.bg_music);
                imageView=null;
            }
            if(linearLayout!=null){
                linearLayout.setBackgroundResource(R.drawable.bg_music);
                linearLayout=null;
            }
        }


    }


}
