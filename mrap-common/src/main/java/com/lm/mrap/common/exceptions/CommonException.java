package com.lm.mrap.common.exceptions;

import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author liming
 * @version 1.0
 * @description: 异常操作的公共方法集合
 * @date 2022/10/28 上午10:31
 */
public class CommonException {

    public enum Logic {
        OR,
        AND;

        /**
         * 条件计算方法，计算两个boolean类型变量的l条件的结果
         *
         * @param l 条件运算标识
         * @param b1 第一个boolean变量
         * @param b2 第二个boolean变量
         * @return 返回运算后的结果，如果传入的条件不支持，则返回参数错误异常
         */
        public static boolean logic(Logic l, boolean b1, boolean b2) {

            switch (l) {

                case OR:
                    return b1 || b2;
                case AND:
                    return b1 && b2;
                default:
                    throw new IllegalArgumentException("Logic 只有OR 和 AND");
            }
        }

        public static <T> boolean logic(Logic l, Predicate<T> condition, T obj1, T obj2) {

            switch (l) {

                case OR:
                    return condition.test(obj1) || condition.test(obj2);
                case AND:
                    return condition.test(obj1) && condition.test(obj2);
                default:
                    throw new IllegalArgumentException("Logic 只有OR 和 AND");
            }
        }

        public static <T> boolean logic(Logic l, boolean result, Predicate<T> condition, T obj) {

            switch (l) {

                case OR:
                    return result || condition.test(obj);
                case AND:
                    return result && condition.test(obj);
                default:
                    throw new IllegalArgumentException("Logic 只有OR 和 AND");
            }
        }
    }

    /**
     * 将两个字符串合并
     *
     * @param msg
     * @param suffix
     * @return
     */
    public static String combineMessage(String msg, String suffix) {
        return new StringBuilder().append(msg).append(suffix).toString();
    }

    /**
     * 判断对象是否是null, 如果是null抛出异常
     * 如果有多个对象需要同时做比较，需要指定对象间的逻辑关系
     * 如果最终的结果是true，则抛出异常
     *
     * @param message 需要记录的异常信息
     * @param logic 多个对象间的逻辑关系
     * @param obj 最少一个对象
     * @param objs 多个对象使用可变参数
     */
    public static void throwNotAllowIfNull(String message, Logic logic, Object obj, Object ... objs) {
        throwNotAllow(() -> message, ExceptionConditions.IS_NULL, logic, obj, objs);
    }

    public static void throwNotAllowIfNull(Supplier<String> message, Logic logic, Object obj, Object ... objs) {
        throwNotAllow(message, ExceptionConditions.IS_NULL, logic, obj, objs);
    }

    public static void throwNotAllowIfNull(String message, Object obj) {
        throwNotAllow(message, ExceptionConditions.IS_NULL, obj);
    }

    public static <T> void throwNotAllow(String message, Predicate<T> condition, T obj) {
        throwNotAllow(() -> message, condition, Logic.OR, obj);
    }

    public static void throwNotAllow(String message) {

        throw new NotAllowException(message);
    }

    public static <T> void throwNotAllow(Supplier<String> message, Predicate<T> condition, T obj) {

        throwNotAllow(message, condition, Logic.OR, obj);
    }

    /**
     * 同时测试多个对象是否满足condition条件，多个对象之间使用logic逻辑进行运算
     * 如果多个对象的逻辑结果经过condition测试的结果经过logic运算后为true，则抛出异常
     *
     * @param message 本次异常的信息
     * @param condition 测试条件
     * @param logic 多对象之间测试结果的逻辑关系
     * @param obj 一个对象
     * @param objs 多个对象
     * @param <T> 对象类型
     */
    public static <T> void throwNotAllow(Supplier<String> message, Predicate<T> condition, Logic logic, T obj, T ... objs) {

        boolean flag = condition.test(obj);

        for (T t : objs) {
            flag = Logic.logic(logic, flag, condition, t);
        }

        if (flag) {
            throw  new NotAllowException(message.get());
        }
    }

    public static <T> void throwNotAllow(String message, Predicate<T> condition, Logic logic, T obj, T ... objs) {

        throwNotAllow(
                () -> message,
                condition,
                logic,
                obj,
                objs
        );
    }
}
