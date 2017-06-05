package com.orvibo.homemate.device.HopeMusic;

/**
 * Created by wuliquan on 2016/6/22.
 */
public class MusicConstant {

    /**
     * 以下是一些错误码
     */
    //需要重新登陆
    public static final String ERROR_RELOGIN = "1";
    public static final String NO_DATA="没有相应数据";
    public static final String NO_PERMISSION = "无权操作";
    public static final String NEED_RELOGIN ="请重新登陆";
    public static final String DEVICE_OFFLINE = "设备未在线";
    /**
     * 有下面两种播放状态
     */
    //暂停状态
    public static final String STATE_1 ="1";
    //播放状态
    public static final String STATE_2 ="2";
    /**
     * 有以下六种效果
     */
    //古典
    public static final String EFFECT_0 ="0";
    //现代
    public static final String EFFECT_1 ="1";
    //摇滚
    public static final String EFFECT_2 ="2";
    //流行
    public static final String EFFECT_3 ="3";
    //舞曲
    public static final String EFFECT_4 ="4";
    //原声
    public static final String EFFECT_5 ="5";

    /**
     * 有以下三种音源选择
     */
    //本机
    public static final String SOURCE_1 ="1";
    //外接
    public static final String SOURCE_2 ="2";
    //蓝牙
    public static final String SOURCE_3 ="3";

    /**
     * 有以下三种模式选择
     */
    //随机
    public  static final String MODEL_1 ="1";
    //循环
    public static final String MODEL_2 ="2";
    //单曲
    public static final String MODEL_3 ="3";

    /**
     * 以下是一些额外补充的控制命令
     */
    public static final String MUSIC_PLAY_POSITION="music_play_position";

    public static final String MUSIC_PROGRESS = "MusicProgress";

    /**
     * 图片的url前端
     */
    public static final String IMG_URL="http://nbhope.cn:1010/";

    /**
     * 向往反馈编码
     */
    public static final String [] ERROR_CODE  ={
            "00000",//"默认信息"
            "10001",//"执行成功"
            "10002",//"执行失败"
            "10003",//"执行异常"
            "10004",//"无效的参数"
            "10005",//"没有相应数据"
            "11000",//"没有找到对应的歌曲信息"
            "11001",//"删除原有设备上传的歌曲信息失败"
            "11002",//"歌曲列表名不能超过40个字节"
            "11003",//"歌曲列表名不能为空"
            "12000",//"验证简易密码失败"
            "12001",//"保存用户简易密码失败"
            "12002",//"登录异常"
            "12003",//"错误的AppKey"
            "12004",//"签名不正确"
            "12005",//"用户名或密码有误"
            "12006",//"与服务器时间未同步"
            "12007",//"用户名过长"
            "12008",//"被分享的用户不存在"
            "12009",//"手机号未注册"
            "12010",//"验证码不能为空"
            "12011",//"请先点击忘记密码获取验证码"
            "12012",//"输入验证码错误"
            "12013",//"输入验证码错误3次，失效"
            "12014",//"修改密码异常"
            "12015",//"修改密码成功"
            "12016",//"手机号或旧密码有误"
            "12017",// "该手机号已被注册"
            "12018",//"注册失败"
            "12019",//"验证失败"
            "12020",//"验证成功"
            "12021",//"输入验证码有误"
            "12022",//"手机号已注册"
            "12023",//"请重新登陆"
            "13000",//"已分享"
            "13001",//"未找到当前用户场景列表信息"
            "13002",//"分享失败"
            "14000",//"设备已被添加过"
            "14001",//"非法设备"
            "14002",//"无效设备"
            "14003",// "设备未在线"
            "14004",//"未找到指定设备下指定动作相关参数信息"
            "14005",// "未找到设备相关的动作信息"
            "14006",//"获取消息通知失败"
    };



}
