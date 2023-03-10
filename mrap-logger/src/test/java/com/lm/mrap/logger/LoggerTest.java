package com.lm.mrap.logger;

import com.lm.mrap.common.utils.SleepUitl;
import org.junit.Test;

import java.io.IOException;

/**
 * @author liming
 * @version 1.0
 * @description: 日志测试类
 * @date 2022/10/28 下午4:40
 */
public class LoggerTest {
    @Test
    public void testLog() throws IOException {

        Logger.init("test");

        Logger.info("i am a info log", "ths is a info log");
        Logger.debug("i am a debug log", "ths is a debug log");
        Logger.warn("i am a warn log", "ths is a warn log");
        Logger.error("i am a error log", "ths is a error log");

        for (int i = 1000; i > 0; i--) {

            Logger.log("REQUEST", "i am a new request log", "that is a request log");
            try {
                SleepUitl.second(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testPrintOsName() {
        System.out.println(System.getProperty("os.name"));
    }
}
