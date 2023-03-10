package com.lm.mrap.common.builder;

/**
 * 构建器公共接口，所有使用构建器模式来构建对象的类必须实现这个接口
 *
 * @author liming
 * @version 1.0
 * @since 2023/3/1 14:21
 */
public interface Builder<T> {

    T build();
}
