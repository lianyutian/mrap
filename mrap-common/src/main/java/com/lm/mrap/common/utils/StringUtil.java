package com.lm.mrap.common.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author liming
 * @version 1.0
 * @description: String工具类
 * @date 2022/11/2 下午4:52
 */
public class StringUtil {

    public static final String EMPTY_STRING = "";

    public static final String SLASH_STRING = "/";

    public static final String TABLE_SYMBOL = "\t";

    public static final String COMMA_SYMBOL = ",";

    public static List<String> strSplit(String str, String splitSymbol) {

        final List<String> res = new ArrayList<>();
        int pos, prev = 0;

        while ((pos = str.indexOf(splitSymbol, prev)) != -1) {

            res.add(str.substring(prev, pos).trim());
            prev = pos + 1;

        }

        res.add(str.substring(prev).trim());

        return res;
    }

    public static String nullString(String str) {
        return str == null ? "null" : str;
    }

    public static String exToString(Throwable e) {

        if (e == null) {
            return "";
        }

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter, true);
        e.printStackTrace();
        printWriter.flush();
        stringWriter.flush();

        return stringWriter.toString();
    }

    public static String combinePath(String parentPath, String childPath) {

        String newPath = parentPath;

        if (childPath.startsWith("/")) {
            newPath += childPath;
        } else {
            newPath += "/" + childPath;
        }

        return newPath;
    }

    public static String combineString(String str, String ...strs) {

        if (strs.length == 0) {
            return str;
        }

        StringBuilder builder = new StringBuilder();
        builder.append(str);

        for (String s : strs) {
            builder.append(s);
        }

        return builder.toString();
    }

}
