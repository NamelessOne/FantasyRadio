package ru.sigil.log;

import java.util.Collection;
import java.util.LinkedList;

/**
 * User: Nikita
 * Date: 21.03.12
 */
public class LogManager {

    private static final String AppTAG = "FantasyRadio";
    private static final String TAG = LogManager.class.getSimpleName();
    private static final Collection<ICustomLogger> loggers = new LinkedList<>();

    public static boolean isDebug = true;


    private LogManager() {
    }

    private static void handle(final ICustomLogger.LogLevel logLevel, final String tag, final String component, final String message, final Exception e) {
        synchronized (loggers) {
            if (isDebug) {
                for (final ICustomLogger logger : loggers) {
                    logger.handle(logLevel, tag, component, message, e);
                }
            }
        }
    }

    public static void addLogger(final ICustomLogger logger) {
        synchronized (loggers) {
            if (null == logger) {
                e(TAG, "CmdLogHandler cannot be null");
            } else {
                loggers.add(logger);
            }
        }
    }

    public static void d(final String component, final String message) {
        handle(ICustomLogger.LogLevel.DEBUG, AppTAG, component, message, null);
    }

    public static void d(final String component, final String message, final Object... args) {
        handle(ICustomLogger.LogLevel.DEBUG, AppTAG, component, String.format(message, args), null);
    }

    public static void d(final String component, final String message, Exception e) {
        handle(ICustomLogger.LogLevel.DEBUG, AppTAG, component, message, e);
    }

    public static void e(final String component, final String message, final Exception e) {
        handle(ICustomLogger.LogLevel.ERROR, AppTAG, component, message, e);
    }

    public static void e(final String component, final String message) {
        handle(ICustomLogger.LogLevel.ERROR, AppTAG, component, message, null);
    }

    public static void w(final String component, final String message) {
        handle(ICustomLogger.LogLevel.WARNING, AppTAG, component, message, null);
    }

    public static void w(final String component, final String message, final Object... args) {
        handle(ICustomLogger.LogLevel.WARNING, AppTAG, component, String.format(message, args), null);
    }
}