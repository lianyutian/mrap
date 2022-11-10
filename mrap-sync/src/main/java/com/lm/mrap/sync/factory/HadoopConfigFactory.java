package com.lm.mrap.sync.factory;

import org.apache.hadoop.conf.Configuration;

/**
 * @author liming
 * @version 1.0
 * @description: hadoop配置工厂类
 * @date 2022/10/20 上午11:03
 */
public class HadoopConfigFactory {
    public static Configuration configuration;

    static {
        configuration = new Configuration();
    }
}
