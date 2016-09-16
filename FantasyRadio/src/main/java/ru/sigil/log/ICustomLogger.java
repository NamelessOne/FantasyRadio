package ru.sigil.log;

/**
 * User: Nikita
 * Date: 21.03.12
 */
public interface ICustomLogger {

    enum LogLevel {
        DEBUG, ERROR, WARNING
    }

    void handle(final LogLevel logLevel, final String tag, final String component, final String message, final Exception e);
}