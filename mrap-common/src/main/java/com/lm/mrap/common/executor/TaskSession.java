package com.lm.mrap.common.executor;

import java.util.List;

/**
 * TaskSession
 *
 * @author liming
 * @version 1.0
 * @since 2023/3/6 11:19
 */
public interface TaskSession<T, S extends TaskSession<T, S>> {

    void retrySession(S session);

    List<S> retrySessions();

    S currentSession();

    void success(T result);

    void failed(Throwable throwable);

    void timeout();

    long lost();

    long sessionLost();

    long startTime();

    long endTime();

    SessionMark sessionMark();

    TaskStatus status();

    boolean statusExchange(TaskStatus sourceStatus, TaskStatus destinationStatus);
}
