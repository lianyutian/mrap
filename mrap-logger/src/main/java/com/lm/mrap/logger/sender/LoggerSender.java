package com.lm.mrap.logger.sender;

import io.netty.buffer.ByteBuf;
import java.util.List;

/**
 * @author liming
 * @version 1.0
 * @description: 日志发送器接口，定义了日志发送需要的公共接口
 * @date 2022/10/27 下午3:14
 */
public interface LoggerSender {

    void write(List<ByteBuf> datas);

    void close();
}