package com.lm.mrap.common.exceptions;

import com.lm.mrap.common.utils.StringUtil;
import io.netty.buffer.ByteBuf;
import org.jooq.lambda.tuple.Tuple2;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * 公共对象测试条件的集合
 *
 * @author liming
 * @version 1.0
 * @since 2023/2/28 14:23
 */
public class ExceptionConditions {

    public static <T1, T2> Predicate<Tuple2<T1, T2>> combineCondition(
            Predicate<T1> condition1,
            Predicate<T2> condition2,
            CommonException.Logic logic) {

        return (Tuple2<T1, T2> data) ->

                CommonException.Logic.logic(
                        logic,
                        condition1.test(data.v1()),
                        condition2.test(data.v2())
                );
    }

    public static final Predicate<Object> IS_NULL = Objects::isNull;

    public static final Predicate<Integer> LETTER_THAN_0 = (Integer number) -> number > 0;

    public static final Predicate<Integer> LOWER_THAN_0 = (Integer number) -> number < 0;

    public static final Predicate<Integer> EQUAL_THAN_0 = (Integer number) -> number == 0;

    public static final Predicate<Integer> LETTER_EQUAL_THAN_0 = (Integer number) -> number >= 0;

    public static final Predicate<Integer> LOWER_EQUAL_THAN_0 = (Integer number) -> number <= 0;

    public static final Predicate<Long> LOWER_EQUAL_THAN_0L = (Long number) -> number <= 0;

    public static final Predicate<Tuple2<Integer, Integer>> NOT_EQUAL_INT = (Tuple2<Integer, Integer> arg) -> !Objects.equals(arg.v1(), arg.v2());

    public static final Predicate<Map> MAP_IS_EMPTY = Map::isEmpty;

    public static final Predicate<String> STRING_IS_EMPTY = String::isEmpty;

    public static final Predicate<String> STRING_IS_NOT_EMPTY = (String str) -> !str.isEmpty();

    public static final Predicate<String> STRING_NOT_PATH = (String str) -> !str.contains(StringUtil.SLASH_STRING);

    public static final Predicate<ByteBuf> BYTEBUF_IS_EMPTY = (ByteBuf buf) -> !buf.isReadable();

    public static final Predicate<Short> LOWER_SHORT_THAN_0 = (Short number) -> number < 0;

    public static final Predicate<Tuple2<Short, Short>> LETTER_SHORT = (Tuple2<Short, Short> item) -> item.v1() > item.v2();

    public static final Predicate<Tuple2<Long, Long>> LETTER_LONG = (Tuple2<Long, Long> item) -> item.v1() > item.v2();

    public static final Predicate<Tuple2<Integer, Integer>> LETTER_INT = (Tuple2<Integer, Integer> item) -> item.v1() > item.v2();

    public static final Predicate<Tuple2<Integer, Integer>> LOWER_INT = (Tuple2<Integer, Integer> item) -> item.v1() < item.v2();

    public static final Predicate<String> PATH_NOT_IS_DIR = (String path) -> !(new File(path)).isDirectory();

    public static final Predicate<List<String>> STRING_LIST_IS_EMPTY = List::isEmpty;

    public static final Predicate<Tuple2<String, String>> STRING_EQUALS = (Tuple2<String, String> item) -> item.v1().equals(item.v2());

    public static final Predicate<Tuple2<List<String>, String>> STRING_CONTAINS_LIST = (Tuple2<List<String>, String> item) -> item.v1().contains(item.v2());









}
