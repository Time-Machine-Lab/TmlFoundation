package io.github.timemachinelab.log.config;

import io.github.timemachinelab.constant.TmlConstant;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * yml提供配置
 *
 * @author glser
 * @since 2026/01/16
 */
@ConfigurationProperties(TmlConstant.LOG)
public class TmlLogProperties {

    private boolean enable = true;

    private String fileName = TmlLogConstant.FILE_NAME_VALUE;

    private String path = TmlLogConstant.PATH_VALUE;

    private TmlLogConstant.Level level = TmlLogConstant.Level.INFO;

    private String fileMaxSize = TmlLogConstant.FILE_MAX_SIZE_VALUE;

    private String fileMaxDays = TmlLogConstant.FILE_MAX_DAYS_VALUE;

    private String charset = TmlLogConstant.CHARSET_VALUE;

    private boolean traceId = true;

    private TmlLogConstant.Env env = TmlLogConstant.Env.PROD;

    private String pattern = TmlLogConstant.PATTERN_VALUE;

    public void apply() {
        System.setProperty(key(TmlLogConstant.ENABLE), String.valueOf(enable));
        System.setProperty(key(TmlLogConstant.FILE_NAME), fileName);
        System.setProperty(key(TmlLogConstant.PATH), path);
        System.setProperty(key(TmlLogConstant.LEVEL), String.valueOf(level));
        System.setProperty(key(TmlLogConstant.FILE_MAX_SIZE), fileMaxSize);
        System.setProperty(key(TmlLogConstant.FILE_MAX_DAYS), fileMaxDays);
        System.setProperty(key(TmlLogConstant.CHARSET), charset);
        System.setProperty(key(TmlLogConstant.TRACE_ID), String.valueOf(traceId));
        System.setProperty(key(TmlLogConstant.ENV), String.valueOf(env));
        System.setProperty(key(TmlLogConstant.PATTERN), pattern);
    }

    private String key(String value) {
        return TmlConstant.LOG + "." + value;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public TmlLogConstant.Level getLevel() {
        return level;
    }

    public void setLevel(TmlLogConstant.Level level) {
        this.level = level;
    }

    public String getFileMaxSize() {
        return fileMaxSize;
    }

    public void setFileMaxSize(String fileMaxSize) {
        this.fileMaxSize = fileMaxSize;
    }

    public String getFileMaxDays() {
        return fileMaxDays;
    }

    public void setFileMaxDays(String fileMaxDays) {
        this.fileMaxDays = fileMaxDays;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public boolean isTraceId() {
        return traceId;
    }

    public void setTraceId(boolean traceId) {
        this.traceId = traceId;
    }

    public TmlLogConstant.Env getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = TmlLogConstant.Env.valueOf(env);
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
}
