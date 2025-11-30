package assign251_2;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

/**
 * Stress tests for performance comparison
 */
class StressTest {

    private static final int LOG_COUNT = 10000;
    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

    @BeforeEach
    void setUp() {
        MemAppender.resetInstance(); // Reset singleton before each stress test
    }

    @AfterEach
    void tearDown() {
        MemAppender.resetInstance(); // Clean up after each test
    }

    /**
     * Measure memory usage in MB
     */
    private long getMemoryUsage() {
        System.gc(); // Suggest GC to get cleaner measurement
        try {
            Thread.sleep(100); // Give GC some time
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return memoryBean.getHeapMemoryUsage().getUsed() / (1024 * 1024);
    }

    /**
     * Test MemAppender with ArrayList vs LinkedList
     */
    @ParameterizedTest
    @ValueSource(ints = {1, 10, 100, 1000, 10000})
    void testMemAppenderListTypes(int maxSize) {
        System.out.println("\n=== Testing maxSize: " + maxSize + " ===");

        // Test with ArrayList - use fresh instance
        long startMemory = getMemoryUsage();
        long startTime = System.currentTimeMillis();

        List<LoggingEvent> arrayList = new ArrayList<>();
        MemAppender arrayListAppender = MemAppender.createNewInstance(arrayList); // Use non-singleton
        arrayListAppender.setMaxSize(maxSize);
        arrayListAppender.setLayout(new VelocityLayout("$m$n"));

        Logger arrayListLogger = Logger.getLogger("ArrayListTest_" + maxSize); // Unique logger name
        arrayListLogger.removeAllAppenders();
        arrayListLogger.addAppender(arrayListAppender);
        arrayListLogger.setLevel(Level.INFO);

        for (int i = 0; i < LOG_COUNT; i++) {
            arrayListLogger.info("Test message " + i);
        }

        long arrayListTime = System.currentTimeMillis() - startTime;
        long arrayListMemory = getMemoryUsage() - startMemory;

        // Clean up ArrayList logger
        arrayListLogger.removeAllAppenders();

        // Test with LinkedList - use fresh instance
        startMemory = getMemoryUsage();
        startTime = System.currentTimeMillis();

        List<LoggingEvent> linkedList = new LinkedList<>();
        MemAppender linkedListAppender = MemAppender.createNewInstance(linkedList); // Use non-singleton
        linkedListAppender.setMaxSize(maxSize);
        linkedListAppender.setLayout(new VelocityLayout("$m$n"));

        Logger linkedListLogger = Logger.getLogger("LinkedListTest_" + maxSize); // Unique logger name
        linkedListLogger.removeAllAppenders();
        linkedListLogger.addAppender(linkedListAppender);

        for (int i = 0; i < LOG_COUNT; i++) {
            linkedListLogger.info("Test message " + i);
        }

        long linkedListTime = System.currentTimeMillis() - startTime;
        long linkedListMemory = getMemoryUsage() - startMemory;

        System.out.println("ArrayList - Time: " + arrayListTime + "ms, Memory: " + arrayListMemory + "MB, Discarded: " + arrayListAppender.getDiscardedLogCount());
        System.out.println("LinkedList - Time: " + linkedListTime + "ms, Memory: " + linkedListMemory + "MB, Discarded: " + linkedListAppender.getDiscardedLogCount());

        // Clean up LinkedList logger
        linkedListLogger.removeAllAppenders();
    }

    @Test
    void testAppenderComparison() {
        System.out.println("\n=== Appender Comparison Test ===");

        // MemAppender with ArrayList - use fresh instance
        long startMemory = getMemoryUsage();
        long startTime = System.currentTimeMillis();

        List<LoggingEvent> events = new ArrayList<>();
        MemAppender memAppender = MemAppender.createNewInstance(events); // Use non-singleton
        memAppender.setMaxSize(5000);
        memAppender.setLayout(new VelocityLayout("$m$n"));

        Logger memLogger = Logger.getLogger("MemAppenderTest");
        memLogger.removeAllAppenders();
        memLogger.addAppender(memAppender);

        for (int i = 0; i < 5000; i++) {
            memLogger.info("Comparison message " + i);
        }

        long memAppenderTime = System.currentTimeMillis() - startTime;
        long memAppenderMemory = getMemoryUsage() - startMemory;

        // Clean up
        memLogger.removeAllAppenders();

        // ConsoleAppender
        startMemory = getMemoryUsage();
        startTime = System.currentTimeMillis();

        ConsoleAppender consoleAppender = new ConsoleAppender(new PatternLayout("%m%n"));
        consoleAppender.setTarget("System.out");
        consoleAppender.activateOptions();

        Logger consoleLogger = Logger.getLogger("ConsoleAppenderTest");
        consoleLogger.removeAllAppenders();
        consoleLogger.addAppender(consoleAppender);

        for (int i = 0; i < 5000; i++) {
            consoleLogger.info("Comparison message " + i);
        }

        long consoleAppenderTime = System.currentTimeMillis() - startTime;
        long consoleAppenderMemory = getMemoryUsage() - startMemory;

        System.out.println("MemAppender - Time: " + memAppenderTime + "ms, Memory: " + memAppenderMemory + "MB");
        System.out.println("ConsoleAppender - Time: " + consoleAppenderTime + "ms, Memory: " + consoleAppenderMemory + "MB");

        // Clean up
        consoleLogger.removeAllAppenders();
    }

    @Test
    void testLayoutComparison() {
        System.out.println("\n=== Layout Comparison Test ===");

        // VelocityLayout - use fresh instance
        long startTime = System.currentTimeMillis();

        List<LoggingEvent> velocityEvents = new ArrayList<>();
        MemAppender velocityAppender = MemAppender.createNewInstance(velocityEvents); // Use non-singleton
        velocityAppender.setLayout(new VelocityLayout("$p $c $d: $m$n"));

        Logger velocityLogger = Logger.getLogger("VelocityLayoutTest");
        velocityLogger.removeAllAppenders();
        velocityLogger.addAppender(velocityAppender);

        for (int i = 0; i < 10000; i++) {
            velocityLogger.info("Layout test message " + i);
        }

        long velocityTime = System.currentTimeMillis() - startTime;

        // Clean up
        velocityLogger.removeAllAppenders();

        // PatternLayout - use fresh instance
        startTime = System.currentTimeMillis();

        List<LoggingEvent> patternEvents = new ArrayList<>();
        MemAppender patternAppender = MemAppender.createNewInstance(patternEvents); // Use non-singleton
        patternAppender.setLayout(new PatternLayout("%p %c %d: %m%n"));

        Logger patternLogger = Logger.getLogger("PatternLayoutTest");
        patternLogger.removeAllAppenders();
        patternLogger.addAppender(patternAppender);

        for (int i = 0; i < 10000; i++) {
            patternLogger.info("Layout test message " + i);
        }

        long patternTime = System.currentTimeMillis() - startTime;

        System.out.println("VelocityLayout - Time: " + velocityTime + "ms");
        System.out.println("PatternLayout - Time: " + patternTime + "ms");

        // Clean up
        patternLogger.removeAllAppenders();
    }

    @Test
    void testFileAppenderComparison() {
        System.out.println("\n=== File Appender Comparison Test ===");

        try {
            // MemAppender - use fresh instance
            long startTime = System.currentTimeMillis();

            List<LoggingEvent> events = new ArrayList<>();
            MemAppender memAppender = MemAppender.createNewInstance(events); // Use non-singleton
            memAppender.setLayout(new VelocityLayout("$p: $m$n"));

            Logger memLogger = Logger.getLogger("FileComparisonMem");
            memLogger.removeAllAppenders();
            memLogger.addAppender(memAppender);

            for (int i = 0; i < 1000; i++) {
                memLogger.info("File comparison message " + i);
            }

            long memTime = System.currentTimeMillis() - startTime;

            // Clean up
            memLogger.removeAllAppenders();

            // FileAppender
            startTime = System.currentTimeMillis();

            FileAppender fileAppender = new FileAppender();
            fileAppender.setLayout(new PatternLayout("%p: %m%n"));
            fileAppender.setFile("target/test-stress.log");
            fileAppender.setAppend(false);
            fileAppender.activateOptions();

            Logger fileLogger = Logger.getLogger("FileComparisonFile");
            fileLogger.removeAllAppenders();
            fileLogger.addAppender(fileAppender);

            for (int i = 0; i < 1000; i++) {
                fileLogger.info("File comparison message " + i);
            }

            long fileTime = System.currentTimeMillis() - startTime;

            System.out.println("MemAppender - Time: " + memTime + "ms");
            System.out.println("FileAppender - Time: " + fileTime + "ms");

            // Clean up
            fileLogger.removeAllAppenders();
            fileAppender.close();

        } catch (Exception e) {
            System.err.println("File appender test failed: " + e.getMessage());
            // Don't fail the test, just continue
        }
    }
}