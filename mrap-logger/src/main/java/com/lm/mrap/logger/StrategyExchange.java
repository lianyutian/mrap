package com.lm.mrap.logger;

/**
 * @author liming
 * @version 1.0
 * @description: 策略切换接口
 * @date 2022/10/27 下午5:37
 */
public interface StrategyExchange {

    boolean isChanged();

    String newName(String originName);
}
