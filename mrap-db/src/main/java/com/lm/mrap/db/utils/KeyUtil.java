package com.lm.mrap.db.utils;

import com.lm.mrap.common.utils.StringUtil;
import com.lm.mrap.logger.Logger;

/**
 * @author liming
 * @version 1.0
 * @description: HBase rowkey 前缀处理工具类
 * @date 2022/11/10 下午2:16
 */
public class KeyUtil {

    private static final String PARAM1 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    private static final String PARAM2 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    public static final String TABLE_SYMBOL = "\t";

    public static String getPrimaryKey(String key) {

        if (!key.isEmpty()) {

            String key1 = key.split("\\|\\|")[0];
            int hCode = key1.hashCode();
            int wHCode = Math.abs(hCode % PARAM1.length());
            int nHCode = Math.abs(hCode % PARAM2.length());

            char wPrefix = PARAM1.charAt(wHCode);
            char nPrefix = PARAM2.charAt(nHCode);

            return String.valueOf(wPrefix) + nPrefix + "_" + key;
        } else {

            return StringUtil.EMPTY_STRING;
        }
    }

    public static int getKeyPartition(String searchKey, String[] arr) {

        int arrLength = arr.length;

        if (arrLength == 0) {

            Logger.error(
                    "KeyUtil.getKeyPartition",
                    "arr is empty, please check table config info",
                    "searchKey: " + searchKey
            );

            return -1;
        }

        if (arrLength == 1) {
            return search1(searchKey, arr);
        } else
            return searchN(searchKey, arr);
    }

    private static int search1(String key, String[] arr) {

        int result = -1;

        int compareResult = compare(arr[0], key);
        if (compareResult == 1) {
            return 0;
        }
        if (compareResult == 0 || compareResult < 0) {
            return 1;
        }

        return result;
    }

    private static int searchN(String key, String[] arr) {

        int start = 0;
        int end = arr.length;
        int tmp = -1;

        while (start < end) {

            tmp = start + (end - start) / 2;

            if (tmp == 0) {

                return search1(key, arr);
            } else {

                int compareResult = compare(key, arr[tmp]);
                if (compareResult == 1 || compareResult == 0) {

                    if (tmp == end - 1 || key.compareTo(arr[tmp + 1]) < 0) {
                        return tmp + 1;
                    } else {
                        start = tmp;
                    }
                }

                if (compareResult == -1) {

                    if (key.compareTo(arr[tmp - 1]) >= 0) {
                        return tmp;
                    } else {
                        end = tmp;
                    }
                }
            }
        }

        return tmp;
    }

    private static int compare(String left, String right) {

        int compareResult = left.compareTo(right);

        return Integer.compare(compareResult, 0);
    }
}
