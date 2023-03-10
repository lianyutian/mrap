package com.lm.mrap.common.executor;

/**
 * TaskResult
 *
 * @author liming
 * @version 1.0
 * @since 2023/3/6 11:27
 */
public class TaskResult<T> {

    public final static class NoneResult<T> extends TaskResult<T> {

        public static NoneResult<Object> NONERESULT = new NoneResult<>(null, null);

        private NoneResult(T result, Throwable cause) {
            super(result, cause);
        }
    }

    private final T result;
    private final Throwable throwable;
    private final Object outgoing;

    public TaskResult(T result, Throwable throwable, Object outgoing) {
        this.result = result;
        this.throwable = throwable;
        this.outgoing = outgoing;
    }

    public TaskResult(T result, Throwable throwable) {
        this(result, throwable, null);
    }

    public T result() {
        return result;
    }

    public Throwable cause() {

        return throwable;
    }

    public boolean isSuccess() {
        return throwable == null;
    }

    public Object getData() {
        return isSuccess() ? result : throwable;
    }

    public Object getOutgoing() {
        return outgoing;
    }
}
