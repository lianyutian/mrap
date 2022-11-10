package com.lm.mrap.logger;

import com.lm.mrap.common.config.CommonConfig;
import com.lm.mrap.common.utils.DateUtil;
import com.lm.mrap.logger.config.LoggerConfig;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.internal.StringUtil;

/**
 * @author liming
 * @version 1.0
 * @description: 日志格式化类
 * @date 2022/10/28 下午4:22
 */
public class LoggerFormat {

    private static final byte LEFT = '[';

    private static final byte RIGHT = ']';

    public static ByteBuf format(String mark, String arg, String ... args) {

        ByteBuf byteBuf = Unpooled.buffer();

        wrapPrefix(byteBuf, defaultProcess(mark));
        wrapItem(byteBuf, defaultProcess(arg));

        for (String item : args) {
            wrapItem(byteBuf, defaultProcess(item));
        }

        return byteBuf;
    }

    private static void wrapItem(ByteBuf byteBuf, String arg) {

        byteBuf.writeByte(LEFT);
        byteBuf.writeBytes(arg.getBytes());
        byteBuf.writeByte(RIGHT);
    }

    private static void wrapPrefix(ByteBuf byteBuf, String mark) {

        wrapItem(byteBuf, mark);
        wrapItem(byteBuf, DateUtil.getCurrentDay(LoggerConfig.DEFAULT_DATE_FORMAT));
        wrapItem(byteBuf, CommonConfig.LOCAL_IP);
    }

    private static String defaultProcess(String str) {
        return str == null ? StringUtil.EMPTY_STRING : str;
    }
}
