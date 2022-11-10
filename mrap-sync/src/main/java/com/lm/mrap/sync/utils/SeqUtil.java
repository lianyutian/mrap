package com.lm.mrap.sync.utils;

import com.lm.mrap.common.config.CommonConfig;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.lm.mrap.sync.config.SyncConfig.TALBE_CONF_PATH;
import static com.lm.mrap.sync.config.SyncConsts.SYSTEM_PROPERTY_TABLES;

/**
 * @author liming
 * @version 1.0
 * @description: TODO
 * @date 2022/11/2 下午3:04
 */
public class SeqUtil {
    private static final int IS_NEW_TABLE_INDEX = 3;

    public static List<String> fileToList() throws IOException {

        BufferedReader linesBufferedReader = bufferedReader();

        return putListItem(linesBufferedReader);
    }

    private static BufferedReader bufferedReader() throws IOException {
        String tablePath = getConfigAbsolutePath(TALBE_CONF_PATH, SYSTEM_PROPERTY_TABLES);

        InputStream inputStream = Files.newInputStream(new File(tablePath).toPath());

        return new BufferedReader(new InputStreamReader(inputStream));
    }

    public static String getConfigAbsolutePath(String confPath, String systemPropertyName) {
        String tablePath = System.getProperty(systemPropertyName, confPath);

        if (tablePath.equals(confPath)) {

            tablePath = CommonConfig.getResourcePath(confPath, tablePath);

        }

        return tablePath;
    }

    private static List<String> putListItem(BufferedReader linesBufferedReader) throws IOException {

        List<String> lines = new ArrayList<>();

        int i = 0;
        String line = null;
        while ((line = linesBufferedReader.readLine()) != null) {
            lines.add(i, line);
            i++;
        }

        return lines;
    }
}
