// [file name]: JMXTestApp.java
package assign251_2;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Test application for JMX monitoring
 */
public class JMXTestApp {

    public static void main(String[] args) throws Exception {
        System.out.println("🚀 Starting JMX Test Application...");
        System.out.println("📊 Connect with JConsole or VisualVM to see JMX monitoring");
        System.out.println("🔍 Look for: assign251_2:type=MemAppenderMonitor,name=TestMonitor");

        try {
            // Create appender
            List<LoggingEvent> events = new ArrayList<>();
            MemAppender appender = MemAppender.createNewInstance(events);
            appender.setLayout(new VelocityLayout("[$p] $m$n"));
            appender.setMaxSize(50);

            // Create JMX Monitor
            MemAppenderMonitor monitor = MemAppenderMonitor.createMonitor(appender, "TestMonitor");

            // Configure logger
            Logger logger = Logger.getLogger("JMXTest");
            logger.removeAllAppenders();
            logger.addAppender(appender);
            logger.setLevel(Level.INFO);

            System.out.println("📝 Starting to generate log messages...");

            // Generate some log messages
            for (int i = 0; i < 100; i++) {
                logger.info("Test message " + i);

                if (i % 10 == 0) {
                    System.out.println("✅ Logged " + (i + 1) + " messages");
                    System.out.println("📈 Current stats - Messages: " + appender.getCurrentSize() +
                            ", Discarded: " + appender.getDiscardedLogCount());
                    System.out.println("🔧 Check JConsole for JMX monitoring");
                }

                Thread.sleep(500); // Wait 0.5 second between messages
            }

            System.out.println("\n🎉 Test completed successfully!");
            System.out.println("📊 JMX Monitor provides the following attributes:");
            System.out.println("   - LogMessages: Array of current log messages");
            System.out.println("   - EstimatedSize: Total characters in cached logs");
            System.out.println("   - DiscardedLogCount: Number of discarded logs");
            System.out.println("   - CurrentSize: Current number of stored logs");
            System.out.println("   - MaxSize: Maximum size limit");
            System.out.println("   - MonitorName: Name of this monitor");
            System.out.println("   - clearLogs operation: Method to clear all logs");

            System.out.println("\n⏳ Keeping application running for 3 minutes for JMX monitoring...");
            System.out.println("💡 Use JConsole or VisualVM to connect and view the MBean");

            // Keep running for monitoring
            Thread.sleep(180000);

            System.out.println("👋 JMX monitoring test completed. Exiting...");

        } catch (Exception e) {
            System.err.println("❌ Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}