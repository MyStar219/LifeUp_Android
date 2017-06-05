package com.orvibo.homemate.user;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.utils.DiskCacheUtils;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;
import com.orvibo.homemate.application.ViHomeApplication;
import com.orvibo.homemate.bo.Account;
import com.orvibo.homemate.bo.ThirdAccount;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.core.InfoPushManager;
import com.orvibo.homemate.core.MinaSocket;
import com.orvibo.homemate.core.UserManage;
import com.orvibo.homemate.core.load.LoadManage;
import com.orvibo.homemate.core.load.LoadParam;
import com.orvibo.homemate.dao.AccountDao;
import com.orvibo.homemate.dao.ThirdAccountDao;
import com.orvibo.homemate.data.BottomTabType;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.GetEmailType;
import com.orvibo.homemate.data.GetSmsType;
import com.orvibo.homemate.data.LoadDataType;
import com.orvibo.homemate.data.RegisterType;
import com.orvibo.homemate.data.UserBindType;
import com.orvibo.homemate.event.ViewEvent;
import com.orvibo.homemate.model.login.Logout;
import com.orvibo.homemate.model.main.MainEvent;
import com.orvibo.homemate.model.user.GetAccountIcon;
import com.orvibo.homemate.model.user.ModifyAccountIcon;
import com.orvibo.homemate.sharedPreferences.PicCache;
import com.orvibo.homemate.sharedPreferences.SessionIdCache;
import com.orvibo.homemate.sharedPreferences.UpdateTimeCache;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.AppTool;
import com.orvibo.homemate.util.BitmapUtil;
import com.orvibo.homemate.util.FileUtils;
import com.orvibo.homemate.util.LoadUtil;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.PopupWindowUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.DialogFragmentTwoButton;
import com.orvibo.homemate.view.custom.NavigationCocoBar;
import com.smartgateway.app.R;
import com.tencent.stat.StatService;

import java.io.File;
import java.util.List;

import de.greenrobot.event.EventBus;


/**
 * Created by orvibo on 2015/5/28.
 */
public class UserActivity extends BaseActivity implements View.OnClickListener,
        NavigationCocoBar.OnLeftClickListener,
        DialogFragmentTwoButton.OnTwoButtonClickListener
//        , Load.OnLoadListener
{
    private static final String TAG = UserActivity.class.getSimpleName();
    private View contentView;
    private NavigationCocoBar navigationBar;
    private RelativeLayout userNicknameRelativeLayout;
    private TextView userNicknameTextView;
    private RelativeLayout userPhoneRelativeLayout;
    private TextView userPhoneTextView;
    private RelativeLayout userEmailRelativeLayout;
    private TextView userEmailTextView;
    private RelativeLayout userPwdChangeRelativeLayout;
    private ImageView userPasswordChangeImageView1;
    private ImageView userPasswordChangeImageView2;
    private ImageView weChatNormalImageView, qqNormalImageView, sinaNormalImageView;
    private Button userLogoutButton;

    private AccountDao mAccountDao;
    private Account account;
    private ThirdAccountDao thirdAccountDao;
//    private Load mLoad;

    private PopupWindow selectPicAlert;
    private static final int REQUEST_CODE_CAPTURE_CAMEIA = 0x11; // 拍照
    private static final int REQUEST_CODE_PICK_IMAGE = 0x12; // 相册
    private static final int PHOTO_RESULT = 0x13; // 结果

    private static String IMG_PATH = FileUtils.getSDPath() + "HomeMate";

    private RelativeLayout userPicRelativeLayout;
    private ImageView userPicImageView;

    private int crop = 256;
    private ImageLoader imageLoader = ImageLoader.getInstance();
    private RelativeLayout mAuthLogin;
    private ImageView mEmail_auth_line;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        mAccountDao = new AccountDao();
        thirdAccountDao = new ThirdAccountDao();
        init();
    }

    private void init() {
        contentView = findViewById(R.id.contentView);
        navigationBar = (NavigationCocoBar) findViewById(R.id.navigationBar);
        navigationBar.setOnLeftClickListener(this);
        userPicRelativeLayout = (RelativeLayout) findViewById(R.id.userPicRelativeLayout);
        userPicRelativeLayout.setOnClickListener(this);
        userPicImageView = (ImageView) findViewById(R.id.userPicImageView);
        userNicknameRelativeLayout = (RelativeLayout) findViewById(R.id.userNicknameRelativeLayout);
        userNicknameRelativeLayout.setOnClickListener(this);
        userNicknameTextView = (TextView) findViewById(R.id.userNicknameTextView);
        userPhoneRelativeLayout = (RelativeLayout) findViewById(R.id.userPhoneRelativeLayout);
        userPhoneRelativeLayout.setOnClickListener(this);
        userPhoneTextView = (TextView) findViewById(R.id.userPhoneTextView);
        userEmailRelativeLayout = (RelativeLayout) findViewById(R.id.userEmailRelativeLayout);
        userEmailRelativeLayout.setOnClickListener(this);
        userEmailTextView = (TextView) findViewById(R.id.userEmailTextView);
        userPwdChangeRelativeLayout = (RelativeLayout) findViewById(R.id.userPasswordChangeRelativeLayout);
        userPwdChangeRelativeLayout.setOnClickListener(this);
        userPasswordChangeImageView1 = (ImageView) findViewById(R.id.userPasswordChangeImageView1);
        userPasswordChangeImageView2 = (ImageView) findViewById(R.id.userPasswordChangeImageView2);
        weChatNormalImageView = (ImageView) findViewById(R.id.weChatNormalImageView);
        qqNormalImageView = (ImageView) findViewById(R.id.qqNormalImageView);
        sinaNormalImageView = (ImageView) findViewById(R.id.sinaNormalImageView);
        userLogoutButton = (Button) findViewById(R.id.userLogoutButton);
        userLogoutButton.setOnClickListener(this);
        account = mAccountDao.selCurrentAccount(UserCache.getCurrentUserId(mAppContext));
        if (account == null && UserManage.getInstance(mAppContext).isLogined() && MinaSocket.isServerConnected()) {
            LogUtil.d(TAG, "init()-Could not found " + userName + "'s account info,start to reload user info.");
            UpdateTimeCache.resetUpdateTime(mAppContext, userId);
            reloadUserInfo();
        }
        mAuthLogin = (RelativeLayout) findViewById(R.id.userThirdRelativeLayout);
        mAuthLogin.setOnClickListener(this);
        mEmail_auth_line = (ImageView) findViewById(R.id.email_auth_line);
        hideThirdAuthViews();
    }

    /**
     * 海外版本需要隐藏第三方登录授权接口
     */
    private void hideThirdAuthViews() {
        if (isOverseasVersion) {
            mAuthLogin.setVisibility(View.GONE);
            // userEmailRelativeLayout.setVisibility(View.GONE);
            //  mEmail_auth_line.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }


    private void refresh() {
        account = mAccountDao.selCurrentAccount(UserCache.getCurrentUserId(mAppContext));
        LogUtil.e(TAG, account + "");
        if (account != null) {
            setUserInfo(account);
        }
    }

    private void setUserInfo(Account account) {
        String nickname = account.getUserName();
        if (!TextUtils.isEmpty(nickname)) {
            userNicknameTextView.setText(nickname);
        }
        String userPhone = account.getPhone();
        if (!TextUtils.isEmpty(userPhone)) {
            if (StringUtil.isPhone(userPhone)) {
                userPhoneTextView.setText(StringUtil.hidePhoneMiddleNumber(userPhone));
            }
        }
        String userEmail = account.getEmail();
        if (!TextUtils.isEmpty(userEmail)) {
            userEmailTextView.setText(StringUtil.hideEmailMiddleWord(userEmail));
        }
        List<ThirdAccount> thirdAccounts = thirdAccountDao.selThirdAccountByUserId(userId);
        weChatNormalImageView.setImageResource(R.drawable.bg_wechat_unauthorized);
        qqNormalImageView.setImageResource(R.drawable.bg_qq_unauthorized);
        sinaNormalImageView.setImageResource(R.drawable.bg_microblog_unauthorized);
        for (ThirdAccount thirdAccount : thirdAccounts) {
            if (thirdAccount.getRegisterType() == RegisterType.WEIXIN_USER) {
                weChatNormalImageView.setImageResource(R.drawable.bg_wechat);
            } else if (thirdAccount.getRegisterType() == RegisterType.QQ_USER) {
                qqNormalImageView.setImageResource(R.drawable.bg_qq);
            } else if (thirdAccount.getRegisterType() == RegisterType.SINA_USER) {
                sinaNormalImageView.setImageResource(R.drawable.bg_microblog);
            }
        }
        if (account.getRegisterType() != RegisterType.REGISTER_USER && TextUtils.isEmpty(userPhone) && TextUtils.isEmpty(userEmail)) {
            userPwdChangeRelativeLayout.setVisibility(View.GONE);
            userPasswordChangeImageView1.setVisibility(View.GONE);
            userPasswordChangeImageView2.setVisibility(View.GONE);
        } else {
            userPwdChangeRelativeLayout.setVisibility(View.VISIBLE);
            userPasswordChangeImageView1.setVisibility(View.VISIBLE);
            userPasswordChangeImageView2.setVisibility(View.VISIBLE);
        }
        setPic();
    }

    private void reloadUserInfo() {
        showProgress();
        //读取服务器所有数据
        LoadParam loadParam = LoadParam.getLoadServerParam(mAppContext);
        loadParam.lastUpdateTime = 0;
        LoadUtil.noticeLoadServerData(loadParam);
    }

    @Override
    public void onLeftClick(View v) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_UserInfo_Back), null);
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.userPicRelativeLayout: {
                //头像，显示选择图片方式
                if (selectPicAlert == null) {
                    View dialog_view = this.getLayoutInflater().inflate(R.layout.dialog_user_select_pic_type, null);
                    dialog_view.findViewById(R.id.btn_camera).setOnClickListener(this);
                    dialog_view.findViewById(R.id.btn_photo).setOnClickListener(this);
                    dialog_view.findViewById(R.id.btn_cancel).setOnClickListener(this);
                    dialog_view.findViewById(R.id.select_pic_popup).setOnClickListener(this);
                    selectPicAlert = new PopupWindow(dialog_view, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
                    PopupWindowUtil.initPopup(selectPicAlert, getResources().getDrawable(R.color.popup_bg), 2);
                }
                selectPicAlert.showAtLocation(contentView, Gravity.NO_GRAVITY, 0, 0);
                break;
            }
            case R.id.select_pic_popup:
                selectPicAlert.dismiss();
                break;
            case R.id.btn_camera: {
                //选择拍照
                String state = Environment.getExternalStorageState();
                if (state.equals(Environment.MEDIA_MOUNTED)) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(IMG_PATH, "temp_cropped.jpg")));
                    startActivityForResult(intent, REQUEST_CODE_CAPTURE_CAMEIA);
                } else {
                    ToastUtil.showToast(R.string.remoteplayback_SDCard_disable_use);
                }
                selectPicAlert.dismiss();
                break;
            }
            case R.id.btn_photo: {
                //选择图片
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setDataAndType(MediaStore.Images.Media.INTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
                selectPicAlert.dismiss();
                break;
            }
            case R.id.btn_cancel: {
                selectPicAlert.dismiss();
                break;
            }
            case R.id.userNicknameRelativeLayout: {
                StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_UserInfo_nickName), null);
                Intent intent = new Intent(this, UserNicknameActivity.class);
                if (account != null) {
                    intent.putExtra(Constant.NICKNAME, account.getUserName());
                    startActivity(intent);
                }
                break;
            }
            case R.id.userPhoneRelativeLayout: {
                StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_UserInfo_Phone), null);
                if (account != null) {
                    String phone = account.getPhone();
                    if (TextUtils.isEmpty(phone)) {
                        Intent intent = new Intent(this, UserPhoneBindActivity.class);
                        intent.putExtra(Constant.GET_SMS_TYPE, GetSmsType.BIND_PHONE);
                        intent.putExtra(Constant.USER_BIND_TYPE, UserBindType.BIND_PHONE);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(this, UserPhoneInfoActivity.class);
                        startActivity(intent);
                    }
                }
                break;
            }
            case R.id.userEmailRelativeLayout: {
                StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_UserInfo_Email), null);
                if (account != null) {
                    String email = account.getEmail();
                    if (TextUtils.isEmpty(email)) {
                        Intent intent = new Intent(this, UserEmailBindActivity.class);
                        intent.putExtra(Constant.GET_EMAIL_TYPE, GetEmailType.BIND_EMAIL);
                        intent.putExtra(Constant.USER_BIND_TYPE, UserBindType.BIND_EMAIL);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(this, UserEmailInfoActivity.class);
                        startActivity(intent);
                    }
                }
                break;
            }
            case R.id.userPasswordChangeRelativeLayout: {
                StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_UserInfo_ModifyPsd), null);
                Intent intent = new Intent(this, UserPasswordChangeActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.userLogoutButton: {
                StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_UserInfo_Exit), null);
                DialogFragmentTwoButton dialogFragmentTwoButton = new DialogFragmentTwoButton();
                dialogFragmentTwoButton.setTitle(getString(R.string.user_logout));
                dialogFragmentTwoButton.setContent(getString(R.string.user_logout_tip));
                dialogFragmentTwoButton.setLeftButtonText(getString(R.string.cancel));
                dialogFragmentTwoButton.setRightButtonText(getString(R.string.confirm));
                dialogFragmentTwoButton.setOnTwoButtonClickListener(this);
                dialogFragmentTwoButton.show(getFragmentManager(), "");
                break;
            }
            case R.id.userThirdRelativeLayout: {
                Intent intent = new Intent(this, UserThirdActivity.class);
                startActivity(intent);
                break;
            }
        }
    }

    @Override
    protected void onRefresh(ViewEvent event) {
        super.onRefresh(event);
        if (event.loadDataType == LoadDataType.LOAD_SERVER_DATAT) {
//            dismissDialog();
            stopProgress();
            account = mAccountDao.selCurrentAccount(UserCache.getCurrentUserId(mAppContext));
            if (account != null) {
                setUserInfo(account);
            }
        }
//        if (Conf.SERVER_LOAD.equals(event.uid)) {
//            dismissDialog();
//            account = mAccountDao.selCurrentAccount(UserCache.getCurrentUserId(mAppContext));
//            if (account != null) {
//                setUserInfo(account);
//            }
//        }
    }

//    @Override
//    public void onLoadFinish(String uid, int result) {
//        if (!isFinishingOrDestroyed()) {
//            UserActivity.this.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    dismissDialog();
//                    account = mAccountDao.selAccount(userName);
//                    if (account != null) {
//                        setUserInfo(account);
//                    }
//                }
//            });
//        }
//    }

    @Override
    public void onLeftButtonClick(View view) {
        StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_UserInfo_CancelExit), null);
    }

    @Override
    public void onRightButtonClick(View view) {
        LogUtil.d(TAG, "onRightButtonClick()-exit account");
        new Logout(mAppContext).logout(userName, currentMainUid);
        AppTool.setHeartbeart(mAppContext, false);
        StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_UserInfo_ConfirmExit), null);
        InfoPushManager.getInstance(mAppContext).cancelAllNotification(UserCache.getCurrentUserId(mContext));
        InfoPushManager.getInstance(mAppContext).setLogined(false);
        InfoPushManager.getInstance(mAppContext).unregisters();
        String userId = null;
        if (account != null) {
            userId = account.getUserId();
        }
        EventBus.getDefault().post(new MainEvent(BottomTabType.TWO_BOTTOM_TAB, true));
        UserManage.getInstance(mAppContext).exitAccount(userId);
        Intent intent = new Intent(mContext, LoginActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        LoadManage.getInstance().clearAllLoadedCount();
        finish();

    }

    @Override
    protected void onDestroy() {
//        if (mLoad != null) {
//            mLoad.removeListener(this);
//            mLoad.cancelLoad();
//        }l7
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_PICK_IMAGE) {
                if (data != null) {
                    Uri contactData = data.getData();
                    startPhotoCrop(contactData);
                }
            } else if (requestCode == REQUEST_CODE_CAPTURE_CAMEIA) {
                startPhotoCrop(Uri.fromFile(new File(IMG_PATH, "temp_cropped.jpg")));
            } else if (requestCode == PHOTO_RESULT) {
                Bitmap photo = BitmapUtil.decodeUriAsBitmap(Uri.fromFile(new File(IMG_PATH, "temp_cropped2.jpg")), UserActivity.this);
                modifyAccountIcon(photo);//将photo上传到服务器并显示
            }
        }
    }

    /**
     * 调用系统图片编辑进行裁剪
     */
    public void startPhotoCrop(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);// 裁剪框比例
        intent.putExtra("aspectY", 1);
        intent.putExtra("scale", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(IMG_PATH, "temp_cropped2.jpg")));
        intent.putExtra("return-data", false);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true); // no face detection
        startActivityForResult(intent, PHOTO_RESULT);
    }

    /**
     * 将photo上传到服务器并显示
     *
     * @param photo
     */
    private void modifyAccountIcon(Bitmap photo) {
        if (photo != null) {
            showDialog(null, getString(R.string.user_pic_changing));
            photo = BitmapUtil.resizeImage(photo, crop, crop); //裁剪成256*256
            final File picTemp = new File(IMG_PATH, "picTemp.png");
            BitmapUtil.bmpToPng(photo, picTemp); //将图片转换成png格式
        /*
         * 将photo上传到服务器
         */
            ModifyAccountIcon modifyAccountIcon = new ModifyAccountIcon() {
                @Override
                public void onModifyAccountIconResult(String picUrl, int errorCode, String errorMessage) {
                    if (errorCode == ErrorCode.SUCCESS) {
                        dismissDialog();
                        PicCache.savePic(UserActivity.this, userName, picUrl);
                        DiskCacheUtils.removeFromCache(picUrl, imageLoader.getDiskCache());
                        MemoryCacheUtils.removeFromCache(picUrl, imageLoader.getMemoryCache());
                        ImageLoader.getInstance().displayImage(picUrl, userPicImageView, ViHomeApplication.getImageOptions());
//                        ImageLoader.getInstance().displayImage(picUrl, userPicImageView, ViHomeApplication.getImageOptions(R.drawable.bg_head_portrait_normal));
                    } else if (errorCode == ErrorCode.NOT_LOGIN) {
                        LoadUtil.noticeAutoLogin(mAppContext);
                        contentView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startModifyAccountIcon(Constant.SOURCE, userName, SessionIdCache.getServerSessionId(mContext), //sessionId
                                        "png", //picType
                                        picTemp, //data
                                        FileUtils.getFileMD5(picTemp)); //md5
                            }
                        }, 2000);
                    } else {
                        dismissDialog();
                        ToastUtil.toastError(errorCode);
                    }
                }
            };
            modifyAccountIcon.startModifyAccountIcon(Constant.SOURCE, userName, SessionIdCache.getServerSessionId(this), //sessionId
                    "png", //picType
                    picTemp, //data
                    FileUtils.getFileMD5(picTemp)); //md5
        } else {
            LogUtil.e(TAG, "modifyAccountIcon()-获取不到photo" + photo);
        }
//        ModifyAccountIcon modifyAccountIcon = new ModifyAccountIcon() {
//            @Override
//            public void onModifyAccountIconResult(String picUrl, int errorCode, String errorMessage) {
//                if (errorCode == ErrorCode.SUCCESS) {
//                    dismissDialog();
//                    PicCache.savePic(UserActivity.this, userName, picUrl);
//                    DiskCacheUtils.removeFromCache(picUrl, imageLoader.getDiskCache());
//                    MemoryCacheUtils.removeFromCache(picUrl, imageLoader.getMemoryCache());
//                    ImageLoader.getInstance().displayImage(picUrl, userPicImageView, ViHomeApplication.getCircleImageOptions(R.drawable.bg_head_portrait_normal));
//                } else if (errorCode == ErrorCode.NOT_LOGIN) {
//                    LoadUtil.noticeAutoLogin(mAppContext);
//                    contentView.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            startModifyAccountIcon(Constant.SOURCE, userName, SessionIdCache.getServerSessionId(mContext), //sessionId
//                                    "png", //picType
//                                    picTemp, //data
//                                    FileUtils.getFileMD5(picTemp)); //md5
//                        }
//                    }, 2000);
//                } else {
//                    dismissDialog();
//                    ToastUtil.toastError(errorCode);
//                }
//            }
//        };
//        modifyAccountIcon.startModifyAccountIcon(Constant.SOURCE, userName, SessionIdCache.getServerSessionId(this), //sessionId
//                "png", //picType
//                picTemp, //data
//                FileUtils.getFileMD5(picTemp)); //md5
    }

    /**
     * 设置头像
     */
    private void setPic() {
        final String share_picUrl = PicCache.getPicUrl(mContext, userName);
        ImageLoader.getInstance().displayImage(share_picUrl, userPicImageView, ViHomeApplication.getCircleImageOptions(R.drawable.bg_head_portrait_normal));

        //向服务器请求查询头像的url
        GetAccountIcon getAccountIcon = new GetAccountIcon() {
            @Override
            public void onGetAccountIconResult(String picUrl, int errorCode, String errorMessage) {
                switch (errorCode) {
                    case ErrorCode.SUCCESS: {
                        if (!picUrl.equalsIgnoreCase(share_picUrl)) {
                            PicCache.savePic(mContext, userName, picUrl);
                            DiskCacheUtils.removeFromCache(picUrl, ImageLoader.getInstance().getDiskCache());
                            MemoryCacheUtils.removeFromCache(picUrl, ImageLoader.getInstance().getMemoryCache());
                            ImageLoader.getInstance().displayImage(picUrl, userPicImageView, ViHomeApplication.getCircleImageOptions(R.drawable.bg_head_portrait_normal));
                        }
                        break;
                    }
                }
            }
        };
        getAccountIcon.startGetAccountIcon(Constant.SOURCE, userName, SessionIdCache.getServerSessionId(mContext));
    }
}
