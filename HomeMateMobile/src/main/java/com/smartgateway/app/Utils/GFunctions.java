package com.smartgateway.app.Utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * GFunctions
 * Created by Anna on 28/6/16.
 */
public class GFunctions {
    public static String convertDatetoViewType(String strDate) {
        try {
            DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            DateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy", Locale.US);
            Date date = inputFormat.parse(strDate);
            strDate = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return strDate;
    }

    public static String convertDatetoViewType2(String strDate) {
        try {
            DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            DateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.US);
            Date date = inputFormat.parse(strDate);
            strDate = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return strDate;
    }

    public static String convertDatetoViewType1(String strDate) {//"MMM dd, yyyy" ->"dd MMM yyyy"
        try {
//            DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            DateFormat inputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
            DateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy", Locale.US);
            Date date = inputFormat.parse(strDate);
            strDate = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return strDate;
    }
}
