package com.lm.mrap.sync.utils;

import org.apache.hadoop.conf.Configuration;

import java.io.IOException;

/**
 * @author liming
 * @version 1.0
 * @description: hadoop 配置类
 * @date 2022/11/4 下午1:53
 */
public class HadoopConfiguration {
    private Configuration configuration;

    public HadoopConfiguration() {
        configuration = new Configuration();
    }

    public HdfsDealUtil getHdfsDealUtil() throws IOException {

        HdfsDealUtil hdfsDealUtil = new HdfsDealUtil(configuration);

        return hdfsDealUtil;
    }

    public Configuration getConfiguration() {
        return configuration;
    }
}
