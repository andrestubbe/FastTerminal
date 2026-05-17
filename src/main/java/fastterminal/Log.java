package fastterminal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * High-performance thread-safe Logging utility for FastTerminal.
 */
public class Log {

    private static final int MAX_LOGS = 1000;
    private static final List<String> logLines = Collections.synchronizedList(new ArrayList<>());
    private static final List<LogListener> listeners = new CopyOnWriteArrayList<>();

    public static void addListener(LogListener listener) {
        listeners.add(listener);
    }

    public static void removeListener(LogListener listener) {
        listeners.remove(listener);
    }

    public static void info(String message) {
        log(message);
    }

    public static void log(String message) {
        if (logLines.size() >= MAX_LOGS) {
            logLines.remove(0);
        }
        logLines.add(message);
        for (LogListener listener : listeners) {
            listener.onLog(message);
        }
    }

    public static List<String> getLastLines(int count) {
        synchronized (logLines) {
            int size = logLines.size();
            if (size == 0) {
                return new ArrayList<>();
            }
            int start = Math.max(0, size - count);
            return new ArrayList<>(logLines.subList(start, size));
        }
    }
}
