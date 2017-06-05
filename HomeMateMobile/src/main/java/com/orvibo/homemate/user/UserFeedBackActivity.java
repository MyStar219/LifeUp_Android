package com.orvibo.homemate.user;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Account;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.dao.AccountDao;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.sharedPreferences.SessionIdCache;
import com.orvibo.homemate.util.AppTool;
import com.orvibo.homemate.util.FileUtils;
import com.orvibo.homemate.util.MyLogger;
import com.orvibo.homemate.util.NetUtil;
import com.orvibo.homemate.util.PhoneUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.util.UploadImageUtil;
import com.orvibo.homemate.view.custom.DialogFragmentTwoButton;
import com.orvibo.homemate.view.custom.NavigationGreenBar;
import com.orvibo.homemate.view.popup.FullScreenImagePopup;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * created by baoqi on 2016/7/18
 * update by yuwei
 */
public class UserFeedBackActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = UserFeedBackActivity.class.getSimpleName();

    private NavigationGreenBar mNavigationGreenBar;
    private EditText mContent_et;
    private GridView mPic_gridView;
    private EditText mPhoneOrEmailEt;
    private List<Bitmap> picPathList = new ArrayList<Bitmap>();
    private PicGridAdapter mPicGridAdapter;
    private static final int REQUEST_CODE_PICK_IMAGE = 0x12; // 相册
    private Bitmap mEmptyBitmap;
    private TextView mContentSize_tv;
    //private int contentLength = 500;
    private static int MAXUPLOADPHOTO = 1;
    private UploadImageUtil mUploadImageUtil;
    private StringBuilder mStringBuilder;
    private String phone,email;
    private String feedBackContent;
    private FullScreenImagePopup fullScreenImagePopup;
    //上传图片的本地路径
    private String uploadImagePath;
    //至少输入的字符数
    private static final int MIN_INPUT_LENGTH = 1;
    //最大输入的字符数
    private static final int MAX_INPUT_LENGTH = 500;
    boolean uploadImageChange = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_feedback);
        //软键盘弹出时不遮挡输入框
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        initView();
        initData();
        initEvent();
    }

    private void initEvent() {
        mPic_gridView.setOnItemClickListener(this);
        mContent_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String content = mContent_et.getText().toString().trim();
                int length = 0;
                if (content != null && !TextUtils.isEmpty(content)) {
                    length = content.length();
                }
                //计算右下角还可写文字数字
                if (length <= MAX_INPUT_LENGTH && length>=0) {
                    mContentSize_tv.setText((MAX_INPUT_LENGTH - length) + "");
                }
                //控制发送按钮的颜色和是否可点
                if (length<MIN_INPUT_LENGTH){
                    mNavigationGreenBar.setRightTextViewEnable(false);
                    mNavigationGreenBar.setRightTextColor(getResources().getColor(R.color.gray_white));
                }else {
                    mNavigationGreenBar.setRightTextViewEnable(true);
                    mNavigationGreenBar.setRightTextColor(getResources().getColor(R.color.white));
                }


            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void initData() {
        mEmptyBitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.icon_add_normal_user_feedback);
        mUploadImageUtil = new UploadImageUtil(this);
        mStringBuilder = new StringBuilder();
        feedBackContent = FeedBackPreference.getFeedBackEditText(mAppContext,userId,TAG);
        mStringBuilder.append(FeedBackPreference.getUploadImagePid(mAppContext,userId,TAG));
        uploadImagePath = FeedBackPreference.getUploadImagePath(mAppContext,userId,TAG);
        if (!TextUtils.isEmpty(uploadImagePath)){
            try {
                picPathList.add(mUploadImageUtil.getBitmap(uploadImagePath));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        fullScreenImagePopup = new FullScreenImagePopup();
        if (!TextUtils.isEmpty(feedBackContent)){
            mContent_et.setText(feedBackContent);
        }
        //如果读取上次没有发送的反馈内容超过1个字符，“发送”按钮点亮
        if (feedBackContent!=null && feedBackContent.length()>=MIN_INPUT_LENGTH){
            mNavigationGreenBar.setRightTextViewEnable(true);
            mNavigationGreenBar.setRightTextColor(getResources().getColor(R.color.white));
            mContentSize_tv.setText(MAX_INPUT_LENGTH-feedBackContent.length()+"");
        }
        //获取手机或者邮箱
        Account account = new AccountDao().selMainAccountdByUserName(userName);
        phone = account==null?"":account.getPhone();
        email = account==null?"":account.getEmail();
        if (phone != null){
            mPhoneOrEmailEt.setText(phone);
        }else if (email != null){
            mPhoneOrEmailEt.setText(email);
        }
        mPicGridAdapter = new PicGridAdapter();
        mPic_gridView.setAdapter(mPicGridAdapter);
        if (picPathList != null && picPathList.size() < MAXUPLOADPHOTO) {
            picPathList.add(mEmptyBitmap);
        }
        mPicGridAdapter.notifyDataSetChanged();
    }

    private void initView() {
        mNavigationGreenBar = (NavigationGreenBar) findViewById(R.id.navigationBar);
        mNavigationGreenBar.setRightTextViewEnable(false);
        mNavigationGreenBar.setRightTextColor(getResources().getColor(R.color.gray_white));
        mContent_et = (EditText) findViewById(R.id.inputcontent_editText);
        mPic_gridView = (GridView) findViewById(R.id.pic_gridView);
        mPhoneOrEmailEt = (EditText) findViewById(R.id.inputPhoneOrEmail_editText);
        mContentSize_tv = (TextView) findViewById(R.id.contentsize_tv);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if ((position==picPathList.size() - 1) && picPathList.get(picPathList.size() - 1).equals(mEmptyBitmap)) {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setDataAndType(MediaStore.Images.Media.INTERNAL_CONTENT_URI, "image/*");
            startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
        }else {
            fullScreenImagePopup.show(UserFeedBackActivity.this,uploadImagePath);
        }

    }

    private class PicGridAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            if (picPathList != null && picPathList.size() > 0) {
                return picPathList.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            if (picPathList != null && picPathList.size() > 0) {
                return picPathList.get(position);
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            PicViewHolder picViewHolder = null;
            if (convertView == null) {
                picViewHolder = new PicViewHolder();
                convertView = View.inflate(UserFeedBackActivity.this, R.layout.item_userfeedback, null);
                picViewHolder.picImagView = (ImageView) convertView.findViewById(R.id.item_userfeedback_pic);
                picViewHolder.delImageView = (ImageView) convertView.findViewById(R.id.item_userfeedback_delete_pic);
                convertView.setTag(picViewHolder);
            } else {
                picViewHolder = (PicViewHolder) convertView.getTag();
            }
            Bitmap photo = (Bitmap) getItem(position);
            if (photo != null && photo.equals(mEmptyBitmap)) {
                picViewHolder.picImagView.setImageResource(R.drawable.user_feed_back_add_select);
                picViewHolder.delImageView.setVisibility(View.INVISIBLE);
            } else {
                picViewHolder.picImagView.setImageBitmap(photo);
                picViewHolder.delImageView.setVisibility(View.VISIBLE);
                picViewHolder.delImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (picPathList.size() == MAXUPLOADPHOTO && !picPathList.get(picPathList.size() - 1).equals(mEmptyBitmap)) {
                            picPathList.remove(position);
                            picPathList.add(mEmptyBitmap);
                        } else {
                            picPathList.remove(position);
                        }
                        //目前只有一个图片，所以删除整个picid
                        mStringBuilder.delete(0,mStringBuilder.length());
                        uploadImagePath = "";
                        FeedBackPreference.removeUploadImagePath(mAppContext,userId,TAG);
                        FeedBackPreference.removeUploadImagePid(mAppContext,userId,TAG);
                        notifyDataSetChanged();
                        uploadImageChange = true;
                    }
                });
            }
            return convertView;
        }

        class PicViewHolder {
            private ImageView picImagView;
            private ImageView delImageView;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_PICK_IMAGE) {
                if (data != null) {
                    try {
                        Uri contactData = data.getData();
                        String path = mUploadImageUtil.getLocalImagePath(this,contactData);
                        uploadImagePath = path;
                        Bitmap photo = mUploadImageUtil.getBitmap(path);
                        //Bitmap photo = BitmapUtil.decodeUriAsBitmap(contactData, UserFeedBackActivity.this);
                        if (picPathList != null && picPathList.size() < MAXUPLOADPHOTO) {
                            picPathList.add(0, photo);
                        } else if (picPathList.size() == MAXUPLOADPHOTO && picPathList.get(picPathList.size() - 1).equals(mEmptyBitmap)) {
                            picPathList.remove(picPathList.size() - 1);
                            picPathList.add(0, photo);
                        }
                        mPicGridAdapter.notifyDataSetChanged();
                        uploadFeedBackIcon(path);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 将photo上传到服务器并显示
     *
     * @param path
     */
    private void uploadFeedBackIcon(String path) {
        showDialogNow();
        File picTemp = new File(path);
        String fileName = picTemp.getName();
        String picType =fileName.substring(fileName.lastIndexOf(".")+1);
        /*
         * 将photo上传到服务器
         */
        UploadFeedBackIcon mUploadFeedBackIcon = new UploadFeedBackIcon() {
            @Override
            public void onUploadFeedBackIconResult(String picId, int errorCode, String errorMessage) {
                dismissDialog();
                if (errorCode == ErrorCode.SUCCESS) {
                    if (mStringBuilder.length()>0){
                        mStringBuilder.append(",");
                    }
                    mStringBuilder.append(picId);
                    uploadImageChange = true;
                } else {
                    ToastUtil.toastError(errorCode);
                }
            }
        };
        mUploadFeedBackIcon.startUploadFeedBackIcon(Constant.SOURCE, userName, SessionIdCache.getServerSessionId(this), //sessionId
                picType, //picType
                picTemp, //data
                FileUtils.getFileMD5(picTemp)); //md5
    }

    @Override
    public void leftTitleClick(View v) {
        closeActivity();
    }

    private void closeActivity(){
        boolean needSaveContent = false;
        if (feedBackContent != null){
            needSaveContent = !feedBackContent.equals(mContent_et.getText().toString().trim()) && !TextUtils.isEmpty(mContent_et.getText().toString().trim());
        }
        if (needSaveContent || uploadImageChange || !TextUtils.isEmpty(mStringBuilder.toString())){
            //有反馈内容没有发送，提示用户是否放弃
            DialogFragmentTwoButton dialogFragmentTwoButton = new DialogFragmentTwoButton();
            dialogFragmentTwoButton.setContent(getString(R.string.close_feedback_tips));
            dialogFragmentTwoButton.setRightButtonText(getString(R.string.save));
            dialogFragmentTwoButton.setLeftButtonText(getString(R.string.discard));
            dialogFragmentTwoButton.setOnTwoButtonClickListener(new DialogFragmentTwoButton.OnTwoButtonClickListener() {
                @Override
                public void onLeftButtonClick(View view) {
                    FeedBackPreference.removeFeedBackEditText(mAppContext,userId,TAG);
                    FeedBackPreference.removeUploadImagePath(mAppContext,userId,TAG);
                    FeedBackPreference.removeUploadImagePid(mAppContext,userId,TAG);
                    finish();
                }

                @Override
                public void onRightButtonClick(View view) {
                    //保存未发送的反馈内容
                    FeedBackPreference.saveFeedBackEditText(mAppContext,userId,TAG,mContent_et.getText().toString().trim());
                    if (!TextUtils.isEmpty(uploadImagePath)){
                        FeedBackPreference.saveUploadImagePath(mAppContext,userId,TAG,uploadImagePath);
                    }
                    if (!TextUtils.isEmpty(mStringBuilder.toString())){
                        FeedBackPreference.saveUploadImagePid(mAppContext,userId,TAG,mStringBuilder.toString());
                    }
                    finish();
                }
            });
            dialogFragmentTwoButton.show(getFragmentManager(), "");
        }else if (feedBackContent.equals(mContent_et.getText().toString().trim())){
            finish();
        }else {
            FeedBackPreference.removeFeedBackEditText(mAppContext,userId,TAG);
            finish();
        }
    }

    @Override
    public void rightTitleClick(View v) {
        if (!NetUtil.isNetworkEnable(this)){
            ToastUtil.showToast(R.string.NET_DISCONNECT);
            return;
        }
        String contactMode = mPhoneOrEmailEt.getText().toString().trim();
        if (!isRightContactMode(contactMode)){
            ToastUtil.showToast(R.string.please_input_the_right_email);
            return;
        }
        String content = mContent_et.getText().toString().trim();
        showDialog();
        //发送反馈内容回调
        SendFeedBackContent mSendFeedBackContent = new SendFeedBackContent() {
            @Override
            public void onUploadFeedBackResult(int errorCode, String errorMessage) {
                dismissDialog();
                if (errorCode == ErrorCode.SUCCESS){
                    ToastUtil.showToast(R.string.send_success);
                    //发送成功，清除缓存的草稿
                    FeedBackPreference.removeFeedBackEditText(mAppContext,userId,TAG);
                    FeedBackPreference.removeUploadImagePath(mAppContext,userId,TAG);
                    FeedBackPreference.removeUploadImagePid(mAppContext,userId,TAG);
                    finish();
                }else{
                    ToastUtil.showToast(R.string.send_fail);
                }
            }
        };
        mSendFeedBackContent.sendFeedBackContent(Constant.SOURCE,
                userName,
                SessionIdCache.getServerSessionId(this),
                mStringBuilder.toString(),
                content,
                contactMode,
                PhoneUtil.getPhoneTime(),
                TimeZone.getDefault().getDisplayName(),
                Build.MODEL,
                PhoneUtil.getPhoneSystemVersion(),
                AppTool.getAppVersionName(mAppContext),
                userName);
    }

    @Override
    public void onBackPressed() {
        closeActivity();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (picPathList != null) {
            picPathList.clear();
            picPathList = null;
        }
    }

    /**
     * 判断填写的联系方式是否正确
     * @param contactMode
     * @return
     */
    private boolean isRightContactMode(String contactMode){
        if (!TextUtils.isEmpty(contactMode)){
            //如果填写了联系方式，则需要判断格式是否正确
            String phone = "\\d+-?\\d+";
            Pattern p = Pattern.compile(phone);
            Matcher m = p.matcher(contactMode);
            if (!m.matches()){
                //不是固定电话或者手机号码，则判断是否是邮箱
                if (!contactMode.contains("@")){
                    //邮箱格式错误
                    return false;
                }else {
                    //正确的邮箱方式
                    return true;
                }
            }else{
                //是固定电话或者手机号码
                MyLogger.kLog().i("the right contact mode");
                return true;
            }
        }else{
            //没有填写联系方式，不用判断，直接返回true
            return true;
        }
    }
}
