package ru.sigil.log;

        import android.util.Log;

/**
 * User: Nikita
 * Date: 21.03.12
 */
public class DefaultLogger implements ICustomLogger {

    @Override
    public synchronized void handle(LogLevel logLevel, String tag, String component, String message, Exception e) {
        switch (logLevel) {
            case DEBUG:
                Log.d(tag, "(" + component + ") " + message, e);
                break;
            case WARNING:
                Log.w(tag, "(" + component + ") " + message, e);
                break;
            case ERROR:
                Log.e(tag, "(" + component + ") " + message, e);
                break;
        }
    }
}