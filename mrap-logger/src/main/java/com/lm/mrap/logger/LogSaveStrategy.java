package com.lm.mrap.logger;


import com.lm.mrap.common.utils.DateUtil;
import com.lm.mrap.logger.config.LoggerConfig;

/**
 * @author liming
 * @version 1.0
 * @description: 日志保存策略
 * @date 2022/10/27 下午5:37
 */
public enum LogSaveStrategy {

    /**
     * 每日日志文件刷新一个名称
     */
    DAILY(
            new StrategyExchange() {

                private final static long DAY_SECONDS = 24 * 60 * 60;

                private long currentDaySeconds = DateUtil.getCurrentDaySeconds();

                @Override
                public boolean isChanged() {

                    long nextDaySeconds = System.currentTimeMillis() / 1000;

                    if ((currentDaySeconds + DAY_SECONDS) < nextDaySeconds) {
                        return true;
                    } else {
                        return false;
                    }
                }

                @Override
                public String newName(String originName) {
                    return new StringBuilder()
                            .append(originName)
                            .append("_")
                            .append(DateUtil.getCurrentDay(LoggerConfig.DEFAULT_DATE_FORMAT))
                            .toString();
                }
            }
    ),

    /**
     * 所有日志文件存储到一个文件下，文件名不变
     */
    ALLINONE(
            new StrategyExchange() {

                @Override
                public boolean isChanged() {
                    return false;
                }

                @Override
                public String newName(String originName) {
                    return originName;
                }
            }
    );

    private StrategyExchange strategyExchange;

    private LogSaveStrategy(StrategyExchange strategyExchange) {
        this.strategyExchange = strategyExchange;
    }

    public StrategyExchange getStrategyExchange() {
        return strategyExchange;
    }
}
