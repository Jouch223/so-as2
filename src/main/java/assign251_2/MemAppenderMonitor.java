package assign251_2;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.List;
import org.apache.log4j.spi.LoggingEvent;

/**
 * JMX Monitor for MemAppender using DynamicMBean
 * This provides JMX monitoring capabilities for any MemAppender instance
 */
public class MemAppenderMonitor implements DynamicMBean {

    private final MemAppender appender;
    private final String monitorName;

    public MemAppenderMonitor(MemAppender appender, String monitorName) {
        this.appender = appender;
        this.monitorName = monitorName;
        registerMBean();
    }

    private void registerMBean() {
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName name = new ObjectName("assign251_2:type=MemAppenderMonitor,name=" + monitorName);
            if (mbs.isRegistered(name)) {
                mbs.unregisterMBean(name);
            }
            mbs.registerMBean(this, name);
            System.out.println("✅ JMX Monitor registered successfully: " + name);
        } catch (Exception e) {
            System.err.println("❌ Failed to register JMX Monitor: " + e.getMessage());
        }
    }

    @Override
    public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
        switch (attribute) {
            case "LogMessages":
                return getLogMessages();
            case "EstimatedSize":
                return getEstimatedSize();
            case "DiscardedLogCount":
                return appender.getDiscardedLogCount();
            case "CurrentSize":
                return appender.getCurrentSize();
            case "MaxSize":
                return appender.getMaxSize();
            case "MonitorName":
                return monitorName;
            default:
                throw new AttributeNotFoundException("Attribute not found: " + attribute);
        }
    }

    @Override
    public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        throw new UnsupportedOperationException("Setting attributes is not supported");
    }

    @Override
    public AttributeList getAttributes(String[] attributes) {
        AttributeList list = new AttributeList();
        for (String attribute : attributes) {
            try {
                list.add(new Attribute(attribute, getAttribute(attribute)));
            } catch (Exception e) {
                // Skip attributes that cause errors
            }
        }
        return list;
    }

    @Override
    public AttributeList setAttributes(AttributeList attributes) {
        throw new UnsupportedOperationException("Setting attributes is not supported");
    }

    @Override
    public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException, ReflectionException {
        if ("clearLogs".equals(actionName)) {
            appender.clear();
            return "Logs cleared successfully";
        }
        throw new UnsupportedOperationException("Method not supported: " + actionName);
    }

    @Override
    public MBeanInfo getMBeanInfo() {
        MBeanAttributeInfo[] attributes = new MBeanAttributeInfo[] {
                new MBeanAttributeInfo("LogMessages", "[Ljava.lang.String;", "Array of log messages", true, false, false),
                new MBeanAttributeInfo("EstimatedSize", "long", "Estimated size of cached logs in characters", true, false, false),
                new MBeanAttributeInfo("DiscardedLogCount", "long", "Number of discarded logs", true, false, false),
                new MBeanAttributeInfo("CurrentSize", "int", "Current number of stored logs", true, false, false),
                new MBeanAttributeInfo("MaxSize", "int", "Maximum size limit", true, false, false),
                new MBeanAttributeInfo("MonitorName", "java.lang.String", "Name of the monitor", true, false, false)
        };

        MBeanOperationInfo[] operations = new MBeanOperationInfo[] {
                new MBeanOperationInfo("clearLogs", "Clear all logs from the appender", null, "java.lang.String", MBeanOperationInfo.ACTION)
        };

        return new MBeanInfo(
                this.getClass().getName(),
                "MemAppender JMX Monitor",
                attributes,
                null, // constructors
                operations, // operations
                null  // notifications
        );
    }

    private String[] getLogMessages() {
        List<LoggingEvent> logs = appender.getCurrentLogs();
        String[] messages = new String[logs.size()];
        for (int i = 0; i < logs.size(); i++) {
            String message = logs.get(i).getRenderedMessage();
            messages[i] = message != null ? message : "null";
        }
        return messages;
    }

    private long getEstimatedSize() {
        List<LoggingEvent> logs = appender.getCurrentLogs();
        long totalSize = 0;
        for (LoggingEvent event : logs) {
            String message = event.getRenderedMessage();
            if (message != null) {
                totalSize += message.length();
            }
        }
        return totalSize;
    }

    /**
     * Create a new JMX Monitor instance
     */
    public static MemAppenderMonitor createMonitor(MemAppender appender, String monitorName) {
        return new MemAppenderMonitor(appender, monitorName);
    }
}