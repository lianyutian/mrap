package com.lm.mrap.sync.utils;

import com.lm.mrap.common.utils.DateUtil;
import com.lm.mrap.db.kv.config.HBaseConfTable;
import com.lm.mrap.db.kv.config.HBaseConfTableType;
import com.lm.mrap.db.kv.config.HBaseConfig;

import java.util.HashMap;

/**
 * @author liming
 * @version 1.0
 * @description: TODO
 * @date 2022/11/11 下午2:18
 */
public class MapUtil {

    public static HashMap<String, String> updateByQualifier(String writeDataTable, HashMap<String, String> map,
                                                            HBaseConfTable hBaseConfTable) {

        String oldTable = HBaseConfig.EXCHANGE_TABLE_QUAILIFIERS[0];
        String activeTable = HBaseConfig.EXCHANGE_TABLE_QUAILIFIERS[1];
        String updateTime = HBaseConfig.EXCHANGE_TABLE_QUAILIFIERS[2];

        String currentValidNewDataTable = null;

        if (HBaseConfTableType.EXCHANGE_TABLE.equals(hBaseConfTable.gethBaseConfTableType())) {
            if (map.size() != 0) {

                if (map.containsKey(activeTable)) {
                    currentValidNewDataTable = map.get(activeTable);
                }

                map.put(oldTable, currentValidNewDataTable);
                map.put(activeTable, writeDataTable);

            } else {

                map.put(oldTable, writeDataTable);
                map.put(activeTable, writeDataTable);
            }

            map.put(
                    updateTime,
                    DateUtil.getCurrentDay("yyyy-MM-dd HH:mm:ss:SSS")
            );
        } else {
            map.clear();
        }

        return map;
    }
}
