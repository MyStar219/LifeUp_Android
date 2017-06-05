package com.orvibo.homemate.util;

import com.orvibo.homemate.bo.Account;
import com.orvibo.homemate.dao.AccountDao;

/**
 * Created by baoqi on 2016/6/14.
 */
public class JudgeLocationUtil {

    public static boolean isLocation(String userId) {
        if (StringUtil.isEmpty(userId)) {
            return false;
        }
        Account account = new AccountDao().selAccountByUserId(userId);
        if (account != null) {
            if (account.getCountry() != null || account.getCity() != null || account.getState() != null) {
                return true;
            }
        }
        return false;
    }


}
