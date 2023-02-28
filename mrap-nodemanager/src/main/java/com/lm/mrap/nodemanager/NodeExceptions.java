package com.lm.mrap.nodemanager;

/**
 * @author liming
 * @version 1.0
 * @description:
 * @date 2022/11/15 上午10:34
 */
public class NodeExceptions {

    public static class NodeExistsException extends RuntimeException {

        public NodeExistsException(Throwable e) {
            super(e);
        }

        public NodeExistsException(String message) {
            super(message);
        }

        public NodeExistsException(String message, Throwable e) {
            super(message, e);
        }
    }

    public static class TempNodeNotChildException extends RuntimeException {

        public TempNodeNotChildException(Throwable e) {
            super(e);
        }

        public TempNodeNotChildException(String message) {
            super(message);
        }

        public TempNodeNotChildException(String message, Throwable e) {
            super(message, e);
        }

    }

    public static class NodeNotExistsException extends RuntimeException {

        public NodeNotExistsException(Throwable e) {
            super(e);
        }

        public NodeNotExistsException(String message) {
            super(message);
        }

        public NodeNotExistsException(String message, Throwable e) {
            super(message, e);
        }

    }

    public static class NodeCreationException extends RuntimeException {

        public NodeCreationException(Throwable e) {
            super(e);
        }

        public NodeCreationException(String message) {
            super(message);
        }

        public NodeCreationException(String message, Throwable e) {
            super(message, e);
        }

    }

    public static class NodeDeleteException extends RuntimeException {

        public NodeDeleteException(Throwable e) {
            super(e);
        }

        public NodeDeleteException(String message) {
            super(message);
        }

        public NodeDeleteException(String message, Throwable e) {
            super(message, e);
        }

    }

    public static class NodeGetChildException extends RuntimeException {

        public NodeGetChildException(Throwable e) {
            super(e);
        }

        public NodeGetChildException(String message) {
            super(message);
        }

        public NodeGetChildException(String message, Throwable e) {
            super(message, e);
        }

    }

    public static class NodeWriteException extends RuntimeException {

        public NodeWriteException(Throwable e) {
            super(e);
        }

        public NodeWriteException(String message) {
            super(message);
        }

        public NodeWriteException(String message, Throwable e) {
            super(message, e);
        }

    }

    public static class NodeReadException extends RuntimeException {

        public NodeReadException(Throwable e) {
            super(e);
        }

        public NodeReadException(String message) {
            super(message);
        }

        public NodeReadException(String message, Throwable e) {
            super(message, e);
        }

    }

    public static class NodeWatchException extends RuntimeException {

        public NodeWatchException(Throwable e) {
            super(e);
        }

        public NodeWatchException(String message) {
            super(message);
        }

        public NodeWatchException(String message, Throwable e) {
            super(message, e);
        }

    }
}
