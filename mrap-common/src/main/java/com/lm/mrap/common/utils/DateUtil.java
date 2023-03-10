package com.lm.mrap.common.utils;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author liming
 * @version 1.0
 * @description: 日期工具类
 * @date 2022/10/20 下午2:02
 */
public class DateUtil {
    public static String getCurrentDay(String format) {
        return getDay(format, new Date());
    }

    public static String getDay(String format, Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        return simpleDateFormat.format(date);
    }

    public static long getCurrentDaySeconds() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");

        try {
            return simpleDateFormat.parse(getCurrentDay("yyyyMMdd")).getTime() / 1000;
        } catch (ParseException e) {
            //Logger.error(e.getMessage(), e);
        }

        return 0L;
    }
}
