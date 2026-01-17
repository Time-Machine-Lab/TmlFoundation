package io.github.timemachinelab.log.config;

/**
 * 日志常量类
 *
 * @author glser
 * @since 2026/01/16
 */
public class TmlLogConstant {

    public static final String ENABLE = "enable";

    public static final String FILE_NAME = "fileName";

    public static final String PATH = "path";

    public static final String LEVEL = "level";

    public static final String FILE_MAX_SIZE = "fileMaxSize";

    public static final String FILE_MAX_DAYS = "fileMaxDays";

    public static final String CHARSET = "charset";

    public static final String TRACE_ID = "traceId";

    public static final String ENV = "env";

    public static final String PATTERN = "pattern";

    public static String FILE_NAME_VALUE = "unknown";

    public static String PATH_VALUE = "/app/log";

    public enum Level {
        TRACE, DEBUG, INFO, WARN, ERROR
    }

    public static String FILE_MAX_SIZE_VALUE = "200M";

    public static String FILE_MAX_DAYS_VALUE = "7";

    public static String CHARSET_VALUE = "UTF-8";

    public enum Env {
        DEV, TEST, PROD
    }

    public static final String TRACE_ID_HEADER = "Tml-Trace-Id";

    public static final String MDC_TTL = "TTL";
}
