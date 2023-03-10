package com.lm.mrap.common.executor;

import java.util.List;

/**
 * Task
 *
 * @author liming
 * @version 1.0
 * @since 2023/3/6 11:26
 */
public interface Task<T, S extends TaskSession<T, S>> {

    void task(List<TaskResult<T>> data, S taskSession, boolean isRetry);
}
