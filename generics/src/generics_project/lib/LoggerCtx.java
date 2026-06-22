package generics_project.lib;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggerCtx {

    public static void createLogger(Level level, String sourceClass, String sourceMethod, String message) {
        Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME.getClass().getName());
        logger.logp(level, sourceClass, sourceMethod, message);
    }
}
