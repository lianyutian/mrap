package com.lm.mrap.sync.config;

import com.lm.mrap.common.config.CommonConfig;

/**
 * @author liming
 * @version 1.0
 * @description: HDFS默认路径配置类
 * @date 2022/10/20 下午2:20
 */
public class HdfsConfig {
    private static final String INGESTION_CONFIG_ROOT_PATH = "sync";

    private static final String HBASE_ROOT_PATH = INGESTION_CONFIG_ROOT_PATH + ".hdfs.hbase";

    public static final String BASE_PATH = CommonConfig.getStringConfigOrElse(HBASE_ROOT_PATH + ".base_path", "/lbdpada-hdfs/devsup");

    public static final String TABLE_DATA_PATH = BASE_PATH + "/table/";

    public static final String HFILE_PATH = "/hfile";

    public static final String R_FILE_PATH = "/rfile";

    public static final String RDD_FILE_PATH = "/rddfile";

    public static final String DATA_FILE_PATH = "/data_file";

    public static final String HBASE_REGION_INFO_PATH = BASE_PATH + "/region_info/";

    public static final String REDIS_INFO_PATH = BASE_PATH + "/redis_info/";

    public static final String TABLE_IS_READED = BASE_PATH + "/talbe_is_readed";

    public static final String XML_FILE = "/spark_config.xml";

    public static final String DATA_SIZE_FILE = "/file_size";
}
