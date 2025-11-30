package assign251_2;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for VelocityLayout class
 */
class VelocityLayoutTest {

    @Test
    void testDefaultPattern() {
        VelocityLayout layout = new VelocityLayout();
        LoggingEvent event = createTestEvent();

        String result = layout.format(event);
        assertTrue(result.contains("[INFO]"));
        assertTrue(result.contains("TestLogger"));
        assertTrue(result.contains("Test message"));
    }

    @Test
    void testCustomPattern() {
        VelocityLayout layout = new VelocityLayout("$p - $m$n");
        LoggingEvent event = createTestEvent();

        String result = layout.format(event);
        assertEquals("INFO - Test message" + System.lineSeparator(), result);
    }

    @Test
    void testAllVariables() {
        VelocityLayout layout = new VelocityLayout("$c|$d|$m|$p|$t$n");
        LoggingEvent event = createTestEvent();

        String result = layout.format(event);
        assertTrue(result.contains("TestLogger"));
        assertTrue(result.contains("Test message"));
        assertTrue(result.contains("INFO"));
        assertTrue(result.contains("main"));
    }

    @Test
    void testPatternSetter() {
        VelocityLayout layout = new VelocityLayout();
        layout.setPattern("$m ($p)$n");

        LoggingEvent event = createTestEvent();
        String result = layout.format(event);
        assertEquals("Test message (INFO)" + System.lineSeparator(), result);
    }

    @Test
    void testWithMemAppender() {
        VelocityLayout layout = new VelocityLayout("[$p] $m$n");

        // Test integration with MemAppender - use fresh instances
        java.util.ArrayList<org.apache.log4j.spi.LoggingEvent> events = new java.util.ArrayList<>();
        MemAppender appender = MemAppender.createNewInstance(events); // Use non-singleton for testing
        appender.setLayout(layout);

        Logger logger = Logger.getLogger("IntegrationTest");
        logger.removeAllAppenders(); // Clean any existing appenders
        logger.addAppender(appender);
        logger.setLevel(Level.INFO);

        // Log exactly ONE message
        logger.info("Integration test message");

        List<String> eventStrings = appender.getEventStrings();
        assertEquals(1, eventStrings.size(), "Should have exactly 1 formatted message");
        assertEquals("[INFO] Integration test message" + System.lineSeparator(),
                eventStrings.get(0));

        logger.removeAllAppenders(); // Clean up
    }

    private LoggingEvent createTestEvent() {
        Logger logger = Logger.getLogger("TestLogger");
        return new LoggingEvent("TestLogger", logger,
                System.currentTimeMillis(), Level.INFO, "Test message", null);
    }
}