package assign251_2;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests showing compatibility with built-in Log4j classes
 */
class IntegrationTest {

    @AfterEach
    void tearDown() {
        MemAppender.resetInstance(); // Clean up after each test
    }

    @Test
    void testWithConsoleAppender() {
        // Create VelocityLayout
        VelocityLayout velocityLayout = new VelocityLayout("VELOCITY: $p - $m$n");

        // Create ConsoleAppender with VelocityLayout and set target
        ConsoleAppender consoleAppender = new ConsoleAppender();
        consoleAppender.setLayout(velocityLayout);
        consoleAppender.setTarget("System.out"); // Set output target
        consoleAppender.activateOptions(); // Activate the appender

        Logger logger = Logger.getLogger("ConsoleTest");
        logger.removeAllAppenders(); // Clean any existing appenders
        logger.addAppender(consoleAppender);
        logger.setLevel(Level.INFO);

        // This should print to console using our VelocityLayout
        logger.info("Console test message");

        // Test passes if no exception is thrown
        assertTrue(true);

        logger.removeAllAppenders(); // Clean up
    }

    @Test
    void testWithPatternLayoutComparison() {
        // Create both layouts with similar patterns
        VelocityLayout velocityLayout = new VelocityLayout("$p: $m$n");
        PatternLayout patternLayout = new PatternLayout("%p: %m%n");

        // Test VelocityLayout with fresh instance
        List<LoggingEvent> velocityEvents = new ArrayList<>();
        MemAppender velocityAppender = MemAppender.createNewInstance(velocityEvents);
        velocityAppender.setLayout(velocityLayout);

        // Test PatternLayout with fresh instance
        List<LoggingEvent> patternEvents = new ArrayList<>();
        MemAppender patternAppender = MemAppender.createNewInstance(patternEvents);
        patternAppender.setLayout(patternLayout);

        // Use separate loggers to avoid interference
        Logger velocityLogger = Logger.getLogger("VelocityTest");
        velocityLogger.removeAllAppenders();
        velocityLogger.addAppender(velocityAppender);

        Logger patternLogger = Logger.getLogger("PatternTest");
        patternLogger.removeAllAppenders();
        patternLogger.addAppender(patternAppender);

        String testMessage = "Layout comparison test";
        velocityLogger.info(testMessage);
        patternLogger.info(testMessage);

        // Both should produce similar output
        List<String> velocityStrings = velocityAppender.getEventStrings();
        List<String> patternStrings = patternAppender.getEventStrings();

        assertEquals(1, velocityStrings.size());
        assertEquals(1, patternStrings.size());
        // Output should be functionally equivalent though syntax differs
        assertTrue(velocityStrings.get(0).contains(testMessage));
        assertTrue(patternStrings.get(0).contains(testMessage));

        velocityLogger.removeAllAppenders(); // Clean up
        patternLogger.removeAllAppenders(); // Clean up
    }

    @Test
    void testMultipleAppenders() {
        // Test that our MemAppender works alongside built-in appenders
        VelocityLayout layout = new VelocityLayout("MEM: $m$n");
        List<LoggingEvent> events = new ArrayList<>();
        MemAppender memAppender = MemAppender.createNewInstance(events); // Use non-singleton
        memAppender.setLayout(layout);

        ConsoleAppender consoleAppender = new ConsoleAppender();
        consoleAppender.setLayout(new PatternLayout("CONSOLE: %m%n"));
        consoleAppender.setTarget("System.out");
        consoleAppender.activateOptions();

        Logger logger = Logger.getLogger("MultipleAppenderTest");
        logger.removeAllAppenders(); // Clean any existing appenders
        logger.addAppender(memAppender);
        logger.addAppender(consoleAppender);
        logger.setLevel(Level.INFO);

        logger.info("Multiple appender test message");

        // MemAppender should have captured exactly ONE event
        assertEquals(1, memAppender.getCurrentSize(), "MemAppender should have exactly 1 message");
        assertEquals("Multiple appender test message",
                memAppender.getCurrentLogs().get(0).getRenderedMessage());

        logger.removeAllAppenders(); // Clean up
    }
}