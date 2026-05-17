package fastterminal;

/**
 * Listener functional interface for receiving logging events.
 */
@FunctionalInterface
public interface LogListener {
    void onLog(String line);
}
