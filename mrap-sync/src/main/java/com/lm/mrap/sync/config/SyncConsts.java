package com.lm.mrap.sync.config;

import com.lm.mrap.common.config.CommonConfig;

/**
 * @author liming
 * @version 1.0
 * @description: 默认配置类
 * @date 2022/10/11 下午5:28
 */
public class SyncConsts {
    public static final String SYSTEM_PROPERTY_TABLES = "conf.file.synchronization.tables";

    public static final String SYNC_NO_FLAG = "N";

    public static final String SYNC_YES_FLAG = "Y";

    public static final String[] XML_CONF_KEY = {
            "rowkey",
            "qualifier_name",
            "region_qty",
            "column",
            "client_ip",
            "table_name",
            "model_name",
            "item_qty",
            "write_method",
            "write_role"
    };

    public static final String[] SYNC_WRITE_METHOD = {"Append", "AppendBulkload"};

    public static final String WRITE_METHOD_INIT_STR = "Init";



}
