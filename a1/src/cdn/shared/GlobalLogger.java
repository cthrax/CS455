package cdn.shared;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A central logging facility so that each class doesn't have to declare logging functionality.
 *
 * @author myles
 *
 */
public class GlobalLogger {
    public static GlobalLogger INSTANCE = null;

    /**
     * Log a message at the info level.
     *
     * @param t the object doing the logging.
     * @param msg the message to log.
     */
    public synchronized static void info(Object t, String msg) {
        log(t, msg, Level.INFO);
    }

    /**
     * Log a message at the warning level.
     *
     * @param t the object doing the logging.
     * @param msg the message to log.
     */
    public synchronized static void warning(Object t, String msg) {
        log(t, msg, Level.WARNING);
    }

    /**
     * Log a message at the severe level.
     *
     * @param t the object doing the logging.
     * @param msg the message to log.
     */
    public synchronized static void severe(Object t, String msg) {
        log(t, msg, Level.SEVERE);
    }

    /**
     * Log a message at the fine level (for debug).
     *
     * @param t the object doing the logging.
     * @param msg the message to log.
     */
    public synchronized static void debug(Object t, String msg) {
        log(t, msg, Level.FINE);
    }

    /**
     * Log a message at the fine level (for debug).
     *
     * @param t the object doing the logging.
     * @param msg the message to log.
     */
    public synchronized static void debug2(Object t, String msg) {
        log(t, msg, Level.FINER);
    }

    private synchronized static void log(Object t, String msg, Level level) {
        if (INSTANCE == null) {
            INSTANCE = new GlobalLogger();
        }

        INSTANCE.getLogger(t).logp(level, t.getClass().getName(), "", msg);

        if (level == Level.WARNING || level == Level.INFO) {
            System.out.println(msg);
        }

    }

    Level globalLevel = Level.SEVERE;

    private GlobalLogger() {
    }

    public Logger getLogger(Object t) {
        String className = t.getClass().getName();

        Logger logger = Logger.getLogger(className);
        logger.setLevel(globalLevel);

        return logger;
    }
}
