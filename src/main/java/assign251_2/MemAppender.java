package assign251_2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Custom Log4j Appender that stores log events in memory
 * Uses a controlled singleton pattern for testing
 */
public class MemAppender extends AppenderSkeleton {

    private static MemAppender instance;
    private final List<LoggingEvent> events;
    private long discardedLogCount = 0;
    private int maxSize = 1000; // default max size

    /**
     * Constructor for dependency injection
     * @param eventsList the list to be injected for storing events
     */
    public MemAppender(List<LoggingEvent> eventsList) {
        if (eventsList == null) {
            throw new IllegalArgumentException("Events list cannot be null");
        }
        this.events = eventsList;
    }

    /**
     * Private constructor for Singleton pattern
     * @param eventsList the list to be injected for storing events
     */
    private MemAppender(List<LoggingEvent> eventsList, boolean singleton) {
        this(eventsList);
    }

    /**
     * Get the singleton instance with dependency injection
     * @param eventsList the list to store logging events
     * @return the singleton MemAppender instance
     */
    public static synchronized MemAppender getInstance(List<LoggingEvent> eventsList) {
        if (instance == null) {
            if (eventsList == null) {
                throw new IllegalArgumentException("Events list cannot be null for initial creation");
            }
            instance = new MemAppender(eventsList, true);
        } else {
            // Reset the internal state when getting instance with new list
            instance.events.clear();
            instance.discardedLogCount = 0;
        }
        return instance;
    }

    /**
     * Get the singleton instance (for cases where list is provided separately)
     * @return the singleton MemAppender instance
     * @throws IllegalStateException if instance not initialized
     */
    public static synchronized MemAppender getInstance() {
        if (instance == null) {
            throw new IllegalStateException("MemAppender not initialized. Call getInstance(List) first.");
        }
        return instance;
    }

    /**
     * Reset the singleton instance for testing purposes
     */
    public static synchronized void resetInstance() {
        instance = null;
    }

    /**
     * Get a new independent instance for testing (bypass singleton)
     * @param eventsList the list to store logging events
     * @return new MemAppender instance
     */
    public static synchronized MemAppender createNewInstance(List<LoggingEvent> eventsList) {
        return new MemAppender(eventsList);
    }

    /**
     * Append a logging event to memory storage
     * @param event the logging event to append
     */
    @Override
    protected void append(LoggingEvent event) {
        if (event == null) {
            return;
        }

        synchronized (events) {
            if (events.size() >= maxSize) {
                events.remove(0); // remove oldest event
                discardedLogCount++;
            }
            events.add(event);
        }
    }

    /**
     * Get current logs as unmodifiable list
     * @return unmodifiable list of current logging events
     */
    public List<LoggingEvent> getCurrentLogs() {
        synchronized (events) {
            return Collections.unmodifiableList(new ArrayList<>(events));
        }
    }

    /**
     * Get formatted event strings using the layout
     * @return unmodifiable list of formatted event strings
     * @throws IllegalStateException if layout is not set
     */
    public List<String> getEventStrings() {
        if (layout == null) {
            throw new IllegalStateException("Layout is not set. Cannot format events.");
        }

        synchronized (events) {
            List<String> formattedEvents = new ArrayList<>();
            for (LoggingEvent event : events) {
                String formatted = layout.format(event);
                if (formatted != null) {
                    formattedEvents.add(formatted);
                }
            }
            return Collections.unmodifiableList(formattedEvents);
        }
    }

    /**
     * Print all logs using layout and clear memory
     */
    public void printLogs() {
        synchronized (events) {
            if (layout != null) {
                for (LoggingEvent event : events) {
                    String formatted = layout.format(event);
                    if (formatted != null) {
                        System.out.print(formatted);
                    }
                }
            } else {
                for (LoggingEvent event : events) {
                    Object message = event.getMessage();
                    if (message != null) {
                        System.out.println(message.toString());
                    }
                }
            }
            events.clear();
        }
    }

    /**
     * Get count of discarded logs
     * @return number of logs discarded due to size limits
     */
    public long getDiscardedLogCount() {
        return discardedLogCount;
    }

    /**
     * Set maximum size for stored events
     * @param maxSize maximum number of events to store
     * @throws IllegalArgumentException if maxSize is not positive
     */
    public void setMaxSize(int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("Max size must be positive");
        }

        synchronized (events) {
            this.maxSize = maxSize;

            // Remove excess events if new maxSize is smaller than current size
            while (events.size() > maxSize) {
                events.remove(0);
                discardedLogCount++;
            }
        }
    }

    /**
     * Get current maximum size
     * @return current maximum size
     */
    public int getMaxSize() {
        return maxSize;
    }

    /**
     * Get current number of stored events
     * @return current event count
     */
    public int getCurrentSize() {
        synchronized (events) {
            return events.size();
        }
    }

    /**
     * Close the appender - required by AppenderSkeleton
     */
    @Override
    public void close() {
        // Clean up resources if needed
        synchronized (events) {
            events.clear();
            discardedLogCount = 0;
        }
    }

    /**
     * Check if appender requires layout - required by AppenderSkeleton
     * @return true if layout is required for some operations
     */
    @Override
    public boolean requiresLayout() {
        return false; // Layout is optional for basic operations
    }

    /**
     * Clear all events and reset counters (for testing)
     */
    public void clear() {
        synchronized (events) {
            events.clear();
            discardedLogCount = 0;
        }
    }

    /**
     * Get the list of events (for internal use and testing)
     * @return the internal events list
     */
    protected List<LoggingEvent> getEvents() {
        return events;
    }
}