package assign251_2;

import java.io.StringWriter;
import java.util.Date;
import java.util.Properties;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
/**
 * Custom Log4j Layout using Velocity template engine
 * Supports variables: $c, $d, $m, $p, $t, $n
 */
public class VelocityLayout extends Layout {

    private String pattern;
    private boolean velocityInitialized = false;

    /**
     * Default constructor with default pattern
     */
    public VelocityLayout() {
        this("[$p] $c $d: $m$n");
    }

    /**
     * Constructor with custom pattern
     * @param pattern the velocity template pattern
     */
    public VelocityLayout(String pattern) {
        this.pattern = pattern;
        initializeVelocity();
    }

    /**
     * Initialize Velocity engine
     */
    private void initializeVelocity() {
        if (!velocityInitialized) {
            try {
                Properties props = new Properties();
                props.setProperty("resource.loader", "class");
                props.setProperty("class.resource.loader.class",
                        "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
                Velocity.init(props);
                velocityInitialized = true;
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize Velocity engine", e);
            }
        }
    }

    /**
     * Format logging event using Velocity template
     * @param event the logging event to format
     * @return formatted string
     */
    @Override
    public String format(LoggingEvent event) {
        VelocityContext context = new VelocityContext();

        // Populate context with supported variables
        context.put("c", event.getLoggerName());  // logger name
        context.put("d", new Date(event.timeStamp));  // date
        context.put("m", event.getRenderedMessage());  // message
        context.put("p", event.getLevel().toString());  // priority/level
        context.put("t", event.getThreadName());  // thread name
        context.put("n", System.lineSeparator());  // line separator

        try {
            StringWriter writer = new StringWriter();
            Velocity.evaluate(context, writer, "VelocityLayout", pattern);
            return writer.toString();
        } catch (Exception e) {
            // Fallback to simple format if velocity fails
            return "[" + event.getLevel() + "] " + event.getLoggerName() +
                    " " + new Date(event.timeStamp) + ": " +
                    event.getRenderedMessage() + System.lineSeparator();
        }
    }

    /**
     * Set the velocity pattern
     * @param pattern the velocity template pattern
     */
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    /**
     * Get the current pattern
     * @return current velocity pattern
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * Ignore throwable - not implemented in this layout
     * @return empty string
     */
    @Override
    public boolean ignoresThrowable() {
        return true;
    }

    /**
     * Activate options - required by Layout
     */
    @Override
    public void activateOptions() {
        // No special activation needed
    }
}