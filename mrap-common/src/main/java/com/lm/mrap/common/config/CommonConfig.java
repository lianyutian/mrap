package com.lm.mrap.common.config;


import com.lm.mrap.common.logger.InternalLogger;
import com.lm.mrap.common.logger.InternalLoggerFactory;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.File;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

/**
 * @author liming
 * @version 1.0
 * @description: 公共配置读取类
 * @date 2022/10/11 下午5:38
 */
public class CommonConfig {
    /**
     * 配置文件路径
     */
    private static String CONFIG_PATH = null;

    /**
     * 解析配置后的配置对象
     */
    private static Config CONFIG = null;

    /**
     * 程序配置路径
     */
    private static final String DEFAULT_APPLICATION_CONFIG = "conf/application.conf";

    /**
     * 程序日志配置
     */
    private static final String DEFAULT_LOGGER_CONFIG = "conf/log4j.properties";

    /**
     * 如果程序的配置不在默认路径中，需要使用 java -D 来配置路径，这个就是参数名
     */
    private static final String APPLICATION_CONFIG_PATH = "config.file";

    /**
     * 如果程序日志的log4j配置不在默认路径中，需要使用 java -D 来配置，这个就是参数名
     */
    private static final String LOGGER_CONFIG_PATH = "app.logger.properties";

    public static final String LOCAL_IP;

    public static Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private static final InternalLogger logger = getLogger(CommonConfig.class);



    /**
     * 初始化配置
     */
    static {
        // 获取当前机器IP
        try {
            LOCAL_IP = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        // 从进程中获取配置文件路径
        CONFIG_PATH = System.getProperty(APPLICATION_CONFIG_PATH, DEFAULT_APPLICATION_CONFIG);

        if (CONFIG_PATH.equals(DEFAULT_APPLICATION_CONFIG)) {
            CONFIG_PATH = getResourcePath(DEFAULT_APPLICATION_CONFIG, CONFIG_PATH);
        }

        File configFile = new File(CONFIG_PATH);

        if (!configFile.isFile()) {
            CONFIG = ConfigFactory.empty();
            System.out.println("应用配置文件没有被指定，请按照 -Dconfig.file=...的格式配置");
        } else {
            // 解析配置文件
            try {
                CONFIG = ConfigFactory.parseFile(configFile);
            } catch (Exception e) {
                System.out.println("配置文件解析失败： " + e.getMessage());
                System.exit(-1);
            }
        }
    }

    /**
     * 获取一个日志记录器
     *
     * @param name 记录器名称
     * @return
     */
    public static InternalLogger getLogger(String name) {

        return InternalLoggerFactory.getInstance(name);
    }

    /**
     * 获取一个日志记录器
     *
     * @param classz 记录日志的类信息
     * @return
     */
    public static InternalLogger getLogger(Class<?> classz) {

        return InternalLoggerFactory.getInstance(classz);
    }

    /**
     * 获取某个资源文件的绝对路径，如果获取不到，就使用默认值
     *
     * @param resourcePath 资源据经
     * @param defaultValue 默认值
     * @return 绝对路径
     */
    public static String getResourcePath(String resourcePath, String defaultValue) {
        URL resource = Thread.currentThread().getContextClassLoader().getResource(resourcePath);

        if (resource != null) {
            return resource.getPath();
        }
        return defaultValue;
    }

    /**
     * 获取 int 类型配置数据
     *
     * @param path 配置路径
     * @param defaultValue 配置默认值
     * @return 配置值
     */
    public static Integer getIntegerConfigOrElse(String path, int defaultValue) {
        return getConfigValue(path, defaultValue, CONFIG::getInt);
    }

    /**
     * 获取 String 类型配置数据
     *
     * @param path 配置路径
     * @param defaultVlue 配置默认值
     * @return 配置值
     */
    public static String getStringConfigOrElse(String path, String defaultVlue) {
        return getConfigValue(path, defaultVlue, CONFIG::getString);
    }

    /**
     * 获取 long 类型配置数据
     *
     * @param path 配置路径
     * @param defaultVlue 配置默认值
     * @return 配置值
     */
    public static Long getLongConfigOrElse(String path, Long defaultVlue) {
        return getConfigValue(path, defaultVlue, CONFIG::getLong);
    }

    /**
     * 获取 Boolean 类型配置数据
     *
     * @param path 配置路径
     * @param defaultVlue 配置默认值
     * @return 配置值
     */
    public static Boolean getBooleanConfigOrElse(String path, Boolean defaultVlue) {
        return getConfigValue(path, defaultVlue, CONFIG::getBoolean);
    }

    /**
     * 获取配置值的公共方法
     *
     * @param path 配置路径
     * @param defaultValue 如果值不为null，当没有配置时使用默认值，如果为null，当没有配置时将会报错退出程序
     * @param paseValue 不同配置使用不同的数据类型
     * @param <T> 配置值的类型
     * @return 配置值
     */
    private static <T> T getConfigValue(String path, T defaultValue, Function<String, T> paseValue) {
        T value = defaultValue;
        String errorMsg = "";

        if (CONFIG != null && CONFIG.hasPath(path)) {
            try {
                value = paseValue.apply(path);
            } catch (Exception e) {
                errorMsg = e.getMessage();
            }
        }

        if (value == defaultValue) {
            if (value != null) {
                logger.error("配置：{} 不存在或者没有配置 {} 将使用默认配置：{}", path, errorMsg, defaultValue);
            } else {
                logger.error("配置：{} 不存在或者没有配置 error：{}", path, errorMsg);
            }
        }

        return value;
    }
}
