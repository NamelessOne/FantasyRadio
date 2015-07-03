package ru.sigil.log;

/**
 * User: Nikita
 * Date: 21.03.12
 */
public interface ICustomLogger {

    public static enum LogLevel {
        DEBUG, ERROR, WARNING
    }

    public void handle(final LogLevel logLevel, final String tag, final String component, final String message, final Exception e);

}
