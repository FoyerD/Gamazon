package Application.utils;

import java.io.IOException;
import java.util.logging.*;

public class TradingLogger {
    private static final Logger logger =
      Logger.getLogger(TradingLogger.class.getName());
    private static boolean init = false;
    private TradingLogger() {};

    private static void init() {
        try {
            logger.setLevel(Level.ALL);
            Handler fh = new FileHandler("./log.txt", true);
            fh.setLevel(Level.ALL);
            Formatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
            logger.addHandler(fh);
            init = true;
        } catch (IOException e) {
            return;
        }
    }
    public static void logEvent(String className, String functionName, String msg) {
        if (!init)
            init();

        LogRecord record = new LogRecord(Level.INFO, msg);
        record.setLoggerName("Trading Event Logger");
        record.setSourceClassName(className);
        record.setSourceMethodName(functionName);
        logger.log(record);
    }

    public static void logError(String className, String functionName, String errorMsg, Object... params) {
        if (!init)
            init();
            
        String msg = String.format(errorMsg, (Object[])params);
        LogRecord record = new LogRecord(Level.SEVERE, msg);
        record.setLoggerName("Trading Error Logger");
        record.setSourceClassName(className);
        record.setSourceMethodName(functionName);
        logger.log(record);
    }
}
