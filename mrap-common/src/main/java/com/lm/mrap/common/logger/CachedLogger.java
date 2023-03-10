package com.lm.mrap.common.logger;

import com.lm.mrap.common.utils.DateUtil;
import com.lm.mrap.common.utils.StringUtil;

/**
 * TODO
 *
 * @author liming
 * @version 1.0
 * @since 2023/3/3 17:32
 */
public class CachedLogger implements InternalLogger {

    private final String myName;

    private final static String MESSAGE_FORMAT = "[{}] [{}] [{}] {}";


    public CachedLogger(String myName) {
        this.myName = myName;
    }

    @Override
    public String name() {
        return myName;
    }

    @Override
    public boolean isTraceEnabled() {
        return false;
    }

    @Override
    public void trace(String msg) {

    }

    @Override
    public void trace(String format, Object arg) {

    }

    @Override
    public void trace(String format, Object argA, Object argB) {

    }

    @Override
    public void trace(String format, Object... arguments) {

    }

    @Override
    public void trace(String msg, Throwable t) {

    }

    @Override
    public void trace(Throwable t) {

    }

    @Override
    public boolean isDebugEnabled() {
        return false;
    }

    @Override
    public void debug(String msg) {

    }

    @Override
    public void debug(String format, Object arg) {

    }

    @Override
    public void debug(String format, Object argA, Object argB) {

    }

    @Override
    public void debug(String format, Object... arguments) {

    }

    @Override
    public void debug(String msg, Throwable t) {

    }

    @Override
    public void debug(Throwable t) {

    }

    @Override
    public boolean isInfoEnabled() {
        return false;
    }

    @Override
    public void info(String msg) {
        if (isDebugEnabled()) {
            sendMessage("INFO", message(msg));
        }
    }

    private String message(String msg) {
        return msg;
    }

    private void sendMessage(String level, String message) {
        if (isDebugEnabled() || !level.equals("INFO")) {
            FormattingTuple tuple = MessageFormatter.arrayFormat(
                    MESSAGE_FORMAT,
                    new Object[]{
                            level,
                            DateUtil.getCurrentDay("yyyy-mm-dd"),
                            name(),
                            message
                    }
            );
        }
    }

    @Override
    public void info(String format, Object arg) {
        if (isDebugEnabled()) {
            sendMessage("INFO", message(format, arg));
        }
    }

    private String message(String format, Object arg) {
        FormattingTuple tuple = MessageFormatter.format(format, arg);
        return tuple.getMessage();
    }

    @Override
    public void info(String format, Object argA, Object argB) {
        if (isDebugEnabled()) {
            sendMessage("INFO", message(format, argA, argB));
        }
    }

    private String message(String format, Object argA, Object argB) {
        FormattingTuple tuple = MessageFormatter.format(format, argA, argB);
        return tuple.getMessage();
    }

    @Override
    public void info(String format, Object... arguments) {
        if (isDebugEnabled()) {
            sendMessage("INFO", message(format, arguments));
        }
    }

    private String message(String format, Object... arguments) {
        FormattingTuple tuple = MessageFormatter.format(format, arguments);
        return tuple.getMessage();
    }


    @Override
    public void info(String msg, Throwable t) {
        if (isDebugEnabled()) {
            sendMessage("INFO", message(msg, t));
        }
    }

    private String message(String msg, Throwable t) {
        FormattingTuple tuple = MessageFormatter.format("{} {}", msg, StringUtil.exToString(t));

        return tuple.getMessage();
    }

    @Override
    public void info(Throwable t) {
        if (isDebugEnabled()) {
            sendMessage("INFO", message(t));
        }
    }

    private String message(Throwable t) {
        return StringUtil.exToString(t);
    }

    @Override
    public boolean isWarnEnabled() {
        return false;
    }

    @Override
    public void warn(String msg) {
        sendMessage("WARN", message(msg));
    }

    @Override
    public void warn(String format, Object arg) {
        sendMessage("WARN", message(format, arg));
    }

    @Override
    public void warn(String format, Object... arguments) {
        sendMessage("WARN", message(format, arguments));
    }

    @Override
    public void warn(String format, Object argA, Object argB) {
        sendMessage("WARN", message(format, argA, argB));
    }

    @Override
    public void warn(String msg, Throwable t) {
        sendMessage("WARN", message(msg, t));
    }

    @Override
    public void warn(Throwable t) {
        sendMessage("WARN", message(t));
    }

    @Override
    public boolean isErrorEnabled() {
        return false;
    }

    @Override
    public void error(String msg) {
        sendMessage("ERROR", message(msg));
    }

    @Override
    public void error(String format, Object arg) {
        sendMessage("ERROR", message(format, arg));
    }

    @Override
    public void error(String format, Object argA, Object argB) {
        sendMessage("ERROR", message(format, argA, argB));
    }

    @Override
    public void error(String format, Object... arguments) {
        sendMessage("ERROR", message(format, arguments));
    }

    @Override
    public void error(String msg, Throwable t) {
        sendMessage("ERROR", message(msg, t));
    }

    @Override
    public void error(Throwable t) {
        sendMessage("ERROR", message(t));
    }

    @Override
    public boolean isEnabled(InternalLogLevel level) {
        return false;
    }

    @Override
    public void log(InternalLogLevel level, String msg) {

    }

    @Override
    public void log(InternalLogLevel level, String format, Object arg) {

    }

    @Override
    public void log(InternalLogLevel level, String format, Object argA, Object argB) {

    }

    @Override
    public void log(InternalLogLevel level, String format, Object... arguments) {

    }

    @Override
    public void log(InternalLogLevel level, String msg, Throwable t) {

    }

    @Override
    public void log(InternalLogLevel level, Throwable t) {

    }
}
