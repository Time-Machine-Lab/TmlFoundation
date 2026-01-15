package io.github.timemachinelab.log.config;

import java.util.UUID;

/**
 * @Author glser
 * @Date 2026/01/15
 * @description: 日志配置键统一管理
 */
public enum TmlLog {

    ENABLE("enable", "true"),

    FILE_NAME("fileName", "unknown"),

    PATH("path", "/app/log"),

    LEVEL("level", "INFO"),

    FILE_MAX_SIZE("fileMaxSize", "100M"),

    FILE_MAX_DAYS("fileMaxDays", "7"),

    CHARSET("charset", "UTF-8"),

    TRACE("trace", "true"),

    ENV("env", "prod"),

    ;
    private final String key;

    private final String value;

    public static final String PREFIX = "tml.log";

    TmlLog(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String key() {
        return key;
    }

    public String value() {
        return value;
    }


    public static final String TRACE_ID = "traceId";

    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    public static String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
