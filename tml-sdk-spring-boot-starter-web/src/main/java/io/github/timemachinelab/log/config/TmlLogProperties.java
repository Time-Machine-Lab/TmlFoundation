package io.github.timemachinelab.log.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 供外部项目提供yml配置进行加载
 * 配置项前缀：tml.log
 */
@ConfigurationProperties(TmlLog.PREFIX)
public class TmlLogProperties {

    private boolean enable = Boolean.parseBoolean(TmlLog.ENABLE.value());

    private String fileName = TmlLog.FILE_NAME.value();

    private String path = TmlLog.PATH.value();

    private String level = TmlLog.LEVEL.value();

    private String fileMaxSize = TmlLog.FILE_MAX_SIZE.value();

    private String fileMaxDays = TmlLog.FILE_MAX_DAYS.value();

    private String charset = TmlLog.CHARSET.value();

    private boolean trace = Boolean.parseBoolean(TmlLog.TRACE.value());

    private String env = TmlLog.ENV.value();

    public void apply() {
        setProperty(TmlLog.ENABLE, String.valueOf(enable));
        setProperty(TmlLog.FILE_NAME, fileName);
        setProperty(TmlLog.PATH, path);
        setProperty(TmlLog.LEVEL, level);
        setProperty(TmlLog.FILE_MAX_SIZE, fileMaxSize);
        setProperty(TmlLog.FILE_MAX_DAYS, fileMaxDays);
        setProperty(TmlLog.CHARSET, charset);
        setProperty(TmlLog.TRACE, String.valueOf(trace));
        setProperty(TmlLog.ENV, env);
    }

    private void setProperty(TmlLog tmlLog, String value) {
        if (value == null) {
            return;
        }
        System.setProperty(TmlLog.PREFIX + "." + tmlLog.key(), value);
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public void setFileMaxSize(String fileMaxSize) {
        this.fileMaxSize = fileMaxSize;
    }

    public void setFileMaxDays(String fileMaxDays) {
        this.fileMaxDays = fileMaxDays;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public void setTrace(boolean trace) {
        this.trace = trace;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public boolean isEnable() {
        return enable;
    }

    public String getFileName() {
        return fileName;
    }

    public String getPath() {
        return path;
    }

    public String getLevel() {
        return level;
    }

    public String getFileMaxSize() {
        return fileMaxSize;
    }

    public String getFileMaxDays() {
        return fileMaxDays;
    }

    public String getCharset() {
        return charset;
    }

    public boolean isTrace() {
        return trace;
    }

    public String getEnv() {
        return env;
    }
}
