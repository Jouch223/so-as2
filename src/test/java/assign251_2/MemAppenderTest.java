package assign251_2;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MemAppender class
 */
class MemAppenderTest {

    private MemAppender memAppender;
    private List<LoggingEvent> eventsList;

    @BeforeEach
    void setUp() {
        eventsList = new ArrayList<>();
        MemAppender.resetInstance(); // Reset singleton before each test
        memAppender = MemAppender.getInstance(eventsList);
        memAppender.setMaxSize(10);
        memAppender.clear(); // Clear any existing events
    }

    @AfterEach
    void tearDown() {
        MemAppender.resetInstance(); // Clean up after each test
    }

    @Test
    void testSingletonPattern() {
        MemAppender instance1 = MemAppender.getInstance();
        MemAppender instance2 = MemAppender.getInstance();
        assertSame(instance1, instance2, "Singleton instances should be the same");
    }

    @Test
    void testDependencyInjection() {
        List<LoggingEvent> newList = new ArrayList<>();
        // Should get the same instance even with different list
        MemAppender sameInstance = MemAppender.getInstance(newList);
        assertSame(memAppender, sameInstance);
    }

    @Test
    void testLogStorage() {
        // Create a fresh logger for this test only
        Logger testLogger = Logger.getLogger("TestLogStorage");
        testLogger.removeAllAppenders();
        testLogger.addAppender(memAppender);
        testLogger.setLevel(Level.DEBUG);

        testLogger.info("Test message 1");
        testLogger.warn("Test message 2");

        assertEquals(2, memAppender.getCurrentLogs().size(), "Should have 2 logged messages");
        assertEquals("Test message 1",
                memAppender.getCurrentLogs().get(0).getRenderedMessage());

        testLogger.removeAllAppenders(); // Clean up
    }

    @Test
    void testMaxSizeEnforcement() {
        // Create a fresh logger for this test only
        Logger testLogger = Logger.getLogger("TestMaxSize");
        testLogger.removeAllAppenders();
        testLogger.addAppender(memAppender);
        testLogger.setLevel(Level.DEBUG);

        memAppender.setMaxSize(3);

        // Log 5 messages
        for (int i = 0; i < 5; i++) {
            testLogger.info("Message " + i);
        }

        assertEquals(3, memAppender.getCurrentSize(), "Should only have 3 messages due to maxSize");
        assertEquals(2, memAppender.getDiscardedLogCount(), "Should have discarded 2 messages");

        // The remaining messages should be the last 3 (2, 3, 4)
        List<LoggingEvent> logs = memAppender.getCurrentLogs();
        assertEquals("Message 2", logs.get(0).getRenderedMessage());
        assertEquals("Message 3", logs.get(1).getRenderedMessage());
        assertEquals("Message 4", logs.get(2).getRenderedMessage());

        testLogger.removeAllAppenders(); // Clean up
    }

    @Test
    void testGetEventStringsWithoutLayout() {
        // Create a fresh logger for this test only
        Logger testLogger = Logger.getLogger("TestNoLayout");
        testLogger.removeAllAppenders();
        testLogger.addAppender(memAppender);
        testLogger.setLevel(Level.DEBUG);

        testLogger.info("Test message");

        assertThrows(IllegalStateException.class, () -> {
            memAppender.getEventStrings();
        }, "Should throw exception when layout is not set");

        testLogger.removeAllAppenders(); // Clean up
    }

    @Test
    void testGetEventStringsWithLayout() {
        // Create a fresh logger for this test only
        Logger testLogger = Logger.getLogger("TestWithLayout");
        testLogger.removeAllAppenders();
        testLogger.addAppender(memAppender);
        testLogger.setLevel(Level.DEBUG);

        VelocityLayout layout = new VelocityLayout("$m$n");
        memAppender.setLayout(layout);

        testLogger.info("Test message");

        List<String> eventStrings = memAppender.getEventStrings();
        assertEquals(1, eventStrings.size(), "Should have 1 formatted message");
        assertEquals("Test message" + System.lineSeparator(), eventStrings.get(0));

        testLogger.removeAllAppenders(); // Clean up
    }

    @Test
    void testPrintLogs() {
        // Create a fresh logger for this test only
        Logger testLogger = Logger.getLogger("TestPrintLogs");
        testLogger.removeAllAppenders();
        testLogger.addAppender(memAppender);
        testLogger.setLevel(Level.DEBUG);

        VelocityLayout layout = new VelocityLayout("$m$n");
        memAppender.setLayout(layout);

        testLogger.info("Message 1");
        testLogger.info("Message 2");

        assertEquals(2, memAppender.getCurrentSize(), "Should have 2 messages before printing");
        memAppender.printLogs();
        assertEquals(0, memAppender.getCurrentSize(), "Should have 0 messages after printing");

        testLogger.removeAllAppenders(); // Clean up
    }

    @Test
    void testUnmodifiableLists() {
        // Create a fresh logger for this test only
        Logger testLogger = Logger.getLogger("TestUnmodifiable");
        testLogger.removeAllAppenders();
        testLogger.addAppender(memAppender);
        testLogger.setLevel(Level.DEBUG);

        testLogger.info("Test message");

        List<LoggingEvent> logs = memAppender.getCurrentLogs();
        assertThrows(UnsupportedOperationException.class, () -> {
            logs.add(null);
        }, "Should not be able to modify unmodifiable list");

        memAppender.setLayout(new VelocityLayout("$m"));
        List<String> strings = memAppender.getEventStrings();
        assertThrows(UnsupportedOperationException.class, () -> {
            strings.add("test");
        }, "Should not be able to modify unmodifiable list");

        testLogger.removeAllAppenders(); // Clean up
    }
}