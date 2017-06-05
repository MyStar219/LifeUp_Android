package com.orvibo.homemate.data;

import android.content.Context;

import com.smartgateway.app.R;

/**
 * 错误码。主机或服务器错误码都小于256，app错误码大于等于256
 *
 * @author smagret
 * @date 2015/2/8
 */
public class ErrorMessage {

    public static String getError(Context context, int errorCode) {
        String errorString = context.getResources().getString(
                R.string.UNKNOW_ERROR);
        switch (errorCode) {
            case ErrorCode.SUCCESS:
                errorString = context.getResources().getString(R.string.SUCCESS);
                break;
            case ErrorCode.FAIL:
                errorString = context.getResources().getString(R.string.FAIL);
                break;
            case ErrorCode.NOT_LOGIN_ERROR:
                errorString = context.getResources().getString(
                        R.string.NOT_LOGIN_ERROR);
                break;
            case ErrorCode.STORAGE_FULL_ERROR:
                errorString = context.getResources().getString(
                        R.string.STORAGE_FULL_ERROR);
                break;
            case ErrorCode.LOAD_IR_STORAGE_FULL_ERROR:
                errorString = context.getResources().getString(
                        R.string.LOAD_IR_STORAGE_FULL_ERROR);
                break;
            case ErrorCode.GATEWAY_BINDED:
                errorString = context.getResources().getString(
                        R.string.GATEWAY_BINDED);
                break;
            case ErrorCode.GATEWAY_NOT_BINDED:
                errorString = context.getResources().getString(
                        R.string.GATEWAY_NOT_BINDED);
                break;
            case ErrorCode.GATEWAY_DISCONNECT_SERVER:
                errorString = context.getResources().getString(R.string.NET_ERROR);
                break;
            case ErrorCode.OFFLINE_GATEWAY:
                errorString = context.getResources().getString(R.string.OFFLINE);
                break;
            case ErrorCode.OFFLINE_DEVICE:
                errorString = context.getResources().getString(R.string.OFFLINE_DEVICE);
                break;
            case ErrorCode.USER_NOT_BINDED:
                errorString = context.getResources().getString(
                        R.string.USER_NOT_BINDED);
                break;
            case ErrorCode.TIMING_COUNT_MAX_ERROR:
                errorString = context.getResources().getString(
                        R.string.DIMMING_COUNT_MAX_ERROR);
                break;
            case ErrorCode.WIFI_TIMING_COUNT_MAX_ERROR:
                errorString = context.getResources().getString(
                        R.string.WIFI_TIMING_COUNT_MAX_ERROR);
                break;
            case ErrorCode.WIFI_COUNTDOWN_COUNT_MAX_ERROR:
                errorString = context.getResources().getString(
                        R.string.WIFI_COUNTDOWN_COUNT_MAX_ERROR);
                break;
            case ErrorCode.PHONE_CODE_NOT_MATCH:
                errorString = context.getResources().getString(
                        R.string.PHONE_CODE_NOT_MATCH);
                break;
            case ErrorCode.USER_PASSWORD_ERROR:
                errorString = context.getResources().getString(
                        R.string.USER_PASSWORD_ERROR);
                break;
            case ErrorCode.USER_REGISTER_ERROR:
                errorString = context.getResources().getString(
                        R.string.USER_REGISTER_ERROR);
                break;
            case ErrorCode.AUTH_CODE_EXPIRED:
                errorString = context.getResources().getString(
                        R.string.AUTH_CODE_EXPIRED);
                break;
            case ErrorCode.SESSION_EXPIRED:
                errorString = context.getResources().getString(
                        R.string.SESSION_EXPIRED);
                break;
            case ErrorCode.ADMIN_LOGIN_SERVER_ERROR:
                errorString = context.getResources().getString(
                        R.string.ADMIN_LOGIN_SERVER_ERROR);
                break;
            case ErrorCode.MULTI_ADMIN_LOGIN_ERROR:
                errorString = context.getResources().getString(
                        R.string.ADMIN_LOGIN_FAIL);
                break;
            case ErrorCode.SERIAL_SAME_ERROR:
                errorString = context.getResources().getString(
                        R.string.SERIAL_SAME_ERROR);
                break;
            case ErrorCode.TIMEOUT_BIND:
                errorString = context.getResources().getString(
                        R.string.TIMEOUT_BIND);
                break;
            case ErrorCode.DEVICE_NOT_BIND_USERID:
                errorString = context.getResources().getString(
                        R.string.DEVICE_NOT_BIND_USERID);
                break;
            case ErrorCode.DATA_NOT_EXIST:
                errorString = context.getResources().getString(
                        R.string.DATA_NOT_EXIST);
                break;
            case ErrorCode.DEVICE_HAS_BIND:
                errorString = context.getResources().getString(
                        R.string.DEVICE_HAS_BIND);
                break;
            case ErrorCode.HAS_HANDLE:
                errorString = context.getResources().getString(
                        R.string.HAS_HANDLE);
                break;
            case ErrorCode.INVITE_NO_DEVICE:
                errorString = context.getResources().getString(
                        R.string.family_invite_no_device);
                break;
            case ErrorCode.TIMING_EXIST:
                errorString = context.getResources().getString(
                        R.string.TIMING_EXIST);
                break;
            case ErrorCode.SPECIAL_CHAR_ERROR:
                errorString = context.getResources().getString(
                        R.string.SPECIAL_CHAR_ERROR);
                break;
            case ErrorCode.CLOTHESHORSE_SET_FAIL:
                errorString = context.getResources().getString(
                        R.string.CLOTHESHORSE_SET_FAIL);
                break;
            case ErrorCode.DATA_CHANGED:
                errorString = context.getResources().getString(
                        R.string.DATA_CHANGED);
                break;
            case ErrorCode.VICENTER_BUSY:
                errorString = context.getResources().getString(
                        R.string.VICENTER_BUSY);
                break;
            case ErrorCode.SPECIAL_VICENTER_NETWORKING:
                errorString = context.getResources().getString(
                        R.string.SPECIAL_VICENTER_NETWORKING);
                break;
            case ErrorCode.ROOM_DELETED:
                errorString = context.getResources().getString(
                        R.string.ROOM_DELETED);
                break;
            case ErrorCode.COUNTDOWN_FAIL_OFFLINE:
                errorString = context.getResources().getString(
                        R.string.COUNTDOWN_FAIL_OFFLINE);
                break;
            case ErrorCode.GATEWAY_BIND_NOT_EXIST:
                errorString = context.getResources().getString(
                        R.string.GATEWAY_BIND_NOT_EXIST);
                break;
            case ErrorCode.DEVICE_FULL_STORE:
                errorString = context.getResources().getString(
                        R.string.LACK_OF_RESOURCES);
                break;
            case ErrorCode.LACK_OF_RESOURCES:
                errorString = context.getResources().getString(
                        R.string.LACK_OF_RESOURCES);
                break;
            case ErrorCode.REQUEST_FAIL:
                errorString = context.getResources().getString(
                        R.string.REQUEST_FAIL);
                break;
            case ErrorCode.SOCKET_DISCONNECT:
                errorString = context.getResources().getString(
                        R.string.SOCKET_DISCONNECT);
                break;
            case ErrorCode.SOCKET_EXCEPTION:
                errorString = context.getResources().getString(
                        R.string.SOCKET_EXCEPTION);
                break;
            case ErrorCode.COMMAND_EMPTY:
                errorString = context.getResources().getString(
                        R.string.COMMAND_EMPTY);
                break;
            case ErrorCode.ARGUMENT_EMPTY:
                errorString = context.getResources().getString(
                        R.string.ARGUMENT_EMPTY);
                break;
            case ErrorCode.TIMEOUT:
                errorString = context.getResources().getString(R.string.TIMEOUT);
                break;
            case ErrorCode.TIMEOUT_RK:
                errorString = context.getResources().getString(R.string.TIMEOUT_RK);
                break;
            case ErrorCode.TIMEOUT_CL:
                errorString = context.getResources().getString(R.string.TIMEOUT_CL);
                break;
            case ErrorCode.TIMEOUT_QS:
                errorString = context.getResources().getString(R.string.TIMEOUT_QS);
                break;
            case ErrorCode.TIMEOUT_QD:
                errorString = context.getResources().getString(R.string.TIMEOUT_QD);
                break;
            case ErrorCode.TIMEOUT_GSC:
                errorString = context.getResources()
                        .getString(R.string.TIMEOUT_GSC);
                break;
            case ErrorCode.TIMEOUT_CSC:
                errorString = context.getResources()
                        .getString(R.string.TIMEOUT_CSC);
                break;
            case ErrorCode.REGISTER_ERROR:
                errorString = context.getResources().getString(
                        R.string.REGISTER_ERROR);
                break;
            case ErrorCode.TIMEOUT_CU:
                errorString = context.getResources().getString(R.string.TIMEOUT_CU);
                break;
            case ErrorCode.TIMEOUT_DU:
                errorString = context.getResources().getString(R.string.TIMEOUT_DU);
                break;
            case ErrorCode.TIMEOUT_MP:
                errorString = context.getResources().getString(R.string.TIMEOUT_MP);
                break;
            case ErrorCode.TIMEOUT_CS:
                errorString = context.getResources().getString(R.string.TIMEOUT_CS);
                break;
            case ErrorCode.TIMEOUT_AR:
                errorString = context.getResources().getString(R.string.TIMEOUT_AR);
                break;
            case ErrorCode.TIMEOUT_AF:
                errorString = context.getResources().getString(R.string.TIMEOUT_AF);
                break;
            case ErrorCode.TIMEOUT_AD:
                errorString = context.getResources().getString(R.string.TIMEOUT_AD);
                break;
            case ErrorCode.TIMEOUT_MR:
                errorString = context.getResources().getString(R.string.TIMEOUT_MR);
                break;
            case ErrorCode.TIMEOUT_MF:
                errorString = context.getResources().getString(R.string.TIMEOUT_MF);
                break;
            case ErrorCode.TIMEOUT_MD:
                errorString = context.getResources().getString(R.string.TIMEOUT_MD);
                break;
            case ErrorCode.TIMEOUT_DR:
                errorString = context.getResources().getString(R.string.TIMEOUT_DR);
                break;
            case ErrorCode.TIMEOUT_DF:
                errorString = context.getResources().getString(R.string.TIMEOUT_DF);
                break;
            case ErrorCode.TIMEOUT_DD:
                errorString = context.getResources().getString(R.string.TIMEOUT_DD);
                break;
            case ErrorCode.TIMEOUT_MN:
                errorString = context.getResources().getString(R.string.TIMEOUT_MN);
                break;
            case ErrorCode.TIMEOUT_ND:
                errorString = context.getResources().getString(R.string.TIMEOUT_ND);
                break;
            case ErrorCode.TIMEOUT_HB:
                errorString = context.getResources().getString(R.string.TIMEOUT_HB);
                break;
            case ErrorCode.TIMEOUT_DS:
                errorString = context.getResources().getString(R.string.TIMEOUT_DS);
                break;
            case ErrorCode.TIMEOUT_DBD:
                errorString = context.getResources()
                        .getString(R.string.TIMEOUT_DBD);
                break;
            case ErrorCode.TIMEOUT_CD:
                errorString = context.getResources()
                        .getString(R.string.TIMEOUT_CD);
                break;
            case ErrorCode.TIMEOUT_AFR:
                errorString = context.getResources()
                        .getString(R.string.TIMEOUT_AFR);
                break;
            case ErrorCode.TIMEOUT_SL:
                errorString = context.getResources()
                        .getString(R.string.TIMEOUT_SL);
                break;

            case ErrorCode.TIMEOUT_SO:
                errorString = context.getResources()
                        .getString(R.string.TIMEOUT_SO);
                break;
            case ErrorCode.TIMEOUT_AT:
                errorString = context.getResources()
                        .getString(R.string.TIMEOUT_AT);
                break;
            case ErrorCode.TIMEOUT_MT:
                errorString = context.getResources()
                        .getString(R.string.TIMEOUT_MT);
                break;
            case ErrorCode.TIMEOUT_DT:
                errorString = context.getResources()
                        .getString(R.string.TIMEOUT_DT);
                break;
            case ErrorCode.TIMEOUT_ACT:
                errorString = context.getResources()
                        .getString(R.string.TIMEOUT_ACT);
                break;
            case ErrorCode.TIMEOUT_LO:
                errorString = context.getResources()
                        .getString(R.string.TIMEOUT_LO);
                break;
            case ErrorCode.TIMEOUT_DC:
                errorString = context.getResources()
                        .getString(R.string.TIMEOUT_DC);
                break;
            case ErrorCode.TIMEOUT_AL:
                errorString = context.getResources()
                        .getString(R.string.TIMEOUT_AL);
                break;
            case ErrorCode.TIMEOUT_ML:
                errorString = context.getResources()
                        .getString(R.string.TIMEOUT_ML);
                break;
            case ErrorCode.TIMEOUT_DLK:
                errorString = context.getResources()
                        .getString(R.string.TIMEOUT_DLK);
                break;
            case ErrorCode.TIMEOUT_AIK:
                errorString = context.getResources()
                        .getString(R.string.TIMEOUT_AIK);
                break;
            case ErrorCode.TIMEOUT_MIK:
                errorString = context.getResources()
                        .getString(R.string.TIMEOUT_MIK);
                break;
            case ErrorCode.TIMEOUT_DIK:
                errorString = context.getResources()
                        .getString(R.string.TIMEOUT_DIK);
                break;
            case ErrorCode.TIMEOUT_AC:
                errorString = context.getResources()
                        .getString(R.string.TIMEOUT_AC);
                break;
            case ErrorCode.TIMEOUT_ACL:
                errorString = context.getResources()
                        .getString(R.string.TIMEOUT_ACL);
                break;
            case ErrorCode.PHONE_ERROR:
                errorString = context.getResources()
                        .getString(R.string.PHONE_ERROR);
                break;
            case ErrorCode.AUTH_CODE_ERROR:
                errorString = context.getResources().getString(
                        R.string.AUTH_CODE_ERROR);
                break;

            case ErrorCode.USER_NOT_EXIST_ERROR:
                errorString = context.getResources().getString(
                        R.string.USER_NOT_EXIST_ERROR);
                break;

            case ErrorCode.LOGIN_MESSAGE_ERROR:
                errorString = context.getResources().getString(
                        R.string.LOGIN_MESSAGE_ERROR);
                break;

            case ErrorCode.DEVICE_OFFIINE_ERROR:
                errorString = context.getResources().getString(R.string.DEVICE_OFFIINE_ERROR);
                break;

            case ErrorCode.NET_DISCONNECT:
                errorString = context.getResources().getString(R.string.NET_DISCONNECT);
                break;

            case ErrorCode.MNDS_NOT_FOUND_GATEWAY:
                errorString = context.getResources().getString(
                        R.string.MNDS_NOT_FOUND_GATEWAY);
                break;
            case ErrorCode.USER_NOT_BIND_GATEWAY:
                errorString = context.getResources().getString(
                        R.string.USER_NOT_BIND_GATEWAY);
                break;
            case ErrorCode.LOGIN_FAIL:
                errorString = context.getResources().getString(R.string.LOGIN_FAIL);
                break;
            case ErrorCode.PASSWORD_ERROR:
                errorString = context.getResources().getString(R.string.PASSWORD_ERROR);
                break;
            case ErrorCode.LOGIN_SOME_GATEWAY_FAIL:
                errorString = context.getResources().getString(
                        R.string.LOGIN_SOME_GATEWAY_FAIL);
                break;
            case ErrorCode.WIFI_DISCONNECT:
                errorString = context.getResources().getString(
                        R.string.WIFI_DISCONNECT);
                break;

            case ErrorCode.STORAGE_SPACE_IS_FULL:
                errorString = context.getResources().getString(
                        R.string.STORAGE_SPACE_IS_FULL);
                break;

            case ErrorCode.FOUR_TIMER_ERROR:
                errorString = context.getResources().getString(
                        R.string.FOUR_TIMER_ERROR);
                break;

            case ErrorCode.TIMER_MSG_EQUAL_ERROR:
                errorString = context.getResources().getString(
                        R.string.TIMER_MSG_EQUAL_ERROR);
                break;
            case ErrorCode.NAME_ERROR:
                errorString = context.getResources().getString(
                        R.string.NAME_ERROR);
                break;
            case ErrorCode.SCENE_NAME_EMPTY_ERROR:
                errorString = context.getResources().getString(
                        R.string.SCENE_NAME_EMPTY_ERROR);
                break;
            case ErrorCode.DEVICE_NAME_NULL:
                errorString = context.getResources().getString(
                        R.string.DEVICE_NAME_NULL);
                break;
            case ErrorCode.DEVICE_NAME_IR_NULL:
                errorString = context.getResources().getString(
                        R.string.DEVICE_NAME_IR_NULL);
                break;
            case ErrorCode.BIND_NONE_ORDER:
                errorString = context.getResources().getString(
                        R.string.BIND_NONE_ORDER);
                break;
            case ErrorCode.ROOM_NOT_SELECT:
                errorString = context.getResources().getString(
                        R.string.ROOM_NOT_SELECT);
                break;
            case ErrorCode.SCENE_COUNT_MAX_ERROR:
                errorString = context.getResources().getString(
                        R.string.SCENE_COUNT_MAX_ERROR);
                errorString = String.format(errorString, Constant.SCENE_COUNT_MAX);
                break;
            case ErrorCode.ERROR_SL:
                errorString = context.getResources().getString(
                        R.string.ERROR_SL);
                break;
            case ErrorCode.SCENE_BIND_NO_DEVICE:
                errorString = context.getResources().getString(
                        R.string.SCENE_BIND_NO_DEVICE);
                break;
            case ErrorCode.REMOTE_NOT_ENTER_SETTING:
                errorString = context.getResources().getString(
                        R.string.REMOTE_NOT_ENTER_SETTING);
                break;
            case ErrorCode.NOT_COMPLETE:
                errorString = context.getResources().getString(
                        R.string.NOT_COMPLETE);
                break;

            case ErrorCode.LINKAGE_NONE_CONDITION:
                errorString = context.getResources().getString(
                        R.string.LINKAGE_NONE_CONDITION);
                break;
            case ErrorCode.LINKAGE_NONE_TIME_AND_WEEK:
                errorString = context.getResources().getString(
                        R.string.LINKAGE_NONE_TIME_AND_WEEK);
                break;
            case ErrorCode.REMOTE_ERROR:
                errorString = context.getResources().getString(
                        R.string.REMOTE_ERROR);
                break;
            case ErrorCode.USER_EXIST:
                errorString = context.getResources().getString(
                        R.string.USER_EXIST);
                break;

            case ErrorCode.TIMEOUT_QW:
                errorString = context.getResources().getString(
                        R.string.TIMEOUT_QW);
                break;

            case ErrorCode.NOT_FOUND_VICETER_HOST:
                errorString = context.getResources().getString(
                        R.string.NOT_FOUND_VICETER_HOST);
                break;
            case ErrorCode.CLOSE_SHORSE_DEVICE_EMPTY:
                errorString = context.getResources().getString(
                        R.string.CLOSE_SHORSE_DEVICE_EMPTY);
                break;
            case ErrorCode.TIMEOUT_ACD:
                errorString = context.getResources().getString(
                        R.string.TIMEOUT_ACD);
                break;
            case ErrorCode.TIMEOUT_MCD:
                errorString = context.getResources().getString(
                        R.string.TIMEOUT_MCD);
                break;
            case ErrorCode.TIMEOUT_DCD:
                errorString = context.getResources().getString(
                        R.string.TIMEOUT_DCD);
                break;
            case ErrorCode.TIMEOUT_ACCD:
                errorString = context.getResources().getString(
                        R.string.TIMEOUT_ACCD);
                break;
            case ErrorCode.AUTH_UNLOCK_RESEND_NO_DATA:
                errorString = context.getResources().getString(R.string.AUTH_UNLOCK_RESEND_NO_DATA);
                break;
            case ErrorCode.COUNTDOWN_EXIST:
                errorString = context.getResources().getString(R.string.COUNTDOWN_EXIST);
                break;
            case ErrorCode.ERROR_CONNECT_SERVER_FAIL:
                errorString = context.getResources().getString(
                        R.string.ERROR_CONNECT_SERVER_FAIL);
                break;
            case ErrorCode.IR_DEVICE_ADD_MAX_ERROR:
                errorString = context.getResources().getString(
                        R.string.IR_DEVICE_ADD_MAX_ERROR);
                break;
            case ErrorCode.USER_NO_AUTHORIZE:
                errorString = context.getResources().getString(
                        R.string.USER_NO_AUTHORIZE);
                break;
            case ErrorCode.NO_ADMIN_PERMISSIONS:
                errorString = context.getResources().getString(
                        R.string.NO_ADMIN_PERMISSIONS);
                break;
            case ErrorCode.LINKAGE_BIND_MAX_ERROR:
                errorString = context.getResources().getString(
                        R.string.LINKAGE_BIND_MAX_ERROR);
                break;
            case ErrorCode.IR_IS_LEARNING:
                errorString = context.getResources().getString(
                        R.string.IR_IS_LEARNING);
                break;
            case ErrorCode.SECURITY_MEMBER_LIMIT:
                errorString = context.getString(R.string.SECURITY_MEMBER_LIMIT);
                break;
            case ErrorCode.SECURITY_MEMBER_EXIST:
                errorString = context.getString(R.string.SECURITY_MEMBER_EXIST);
                break;
            case ErrorCode.HUBS_NOT_FOUND:
                errorString = context.getResources().getString(
                        R.string.HUBS_NOT_FOUND);
                break;
            case ErrorCode.MODE_EXIST:
                errorString = context.getResources().getString(
                        R.string.MODE_EXIST);
                break;
            case ErrorCode.COLLECTION_EXIST:
                errorString= context.getResources().getString(
                        R.string.COLLECTION_EXIST);
                break;
            case ErrorCode.DEVICE_DESCRIPTION_INFORMATION_NO_UPDATE:
                errorString = context.getResources().getString(
                        R.string.DEVICE_DESCRIPTION_INFORMATION_NO_UPDATE);
                break;
            case ErrorCode.MODIFY_FAILURE:
                errorString = context.getResources().getString(
                        R.string.MODIFY_FAILURE);
                break;
            case ErrorCode.UPLOAD_FILE_FORMAT_ERROR:
                errorString = context.getResources().getString(
                        R.string.UPLOAD_FILE_FORMAT_ERROR);
                break;
            case ErrorCode.MD5_CHECK_FAILURE:
                errorString = context.getResources().getString(
                        R.string.MD5_CHECK_FAILURE);
                break;
            case ErrorCode.HEAD_NOT_SET:
                errorString = context.getResources().getString(
                        R.string.HEAD_NOT_SET);
                break;
            case ErrorCode.NO_USE_TIPS:
                errorString = context.getResources().getString(
                        R.string.NO_USE_TIPS);
                break;
            case ErrorCode.DEVICE_NOT_BOUND:
                errorString = context.getResources().getString(
                        R.string.DEVICE_NOT_BOUND);
                break;
            case ErrorCode.BIND_OTHER_ACCOUNT:
                errorString = context.getResources().getString(
                        R.string.BIND_OTHER_ACCOUNT);
                break;
            case ErrorCode.START_TIME_AND_END_TIME_IS_SAME:
                errorString = context.getResources().getString(
                        R.string.START_TIME_AND_END_TIME_IS_SAME);
                break;
            case ErrorCode.HOST_UPGRADING:
                errorString = context.getResources().getString(
                        R.string.HOST_UPGRADING);
                break;

            default:
                errorString = context.getResources().getString(
                        R.string.UNKNOW_ERROR);
                errorString = String.format(errorString, errorCode);
                break;
        }

        return errorString;
    }
}
