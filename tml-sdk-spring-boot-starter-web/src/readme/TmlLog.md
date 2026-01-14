# TmlLog 日志模块

## 概述

TmlLog 是一个开箱即用的日志配置模块，提供统一的日志格式、链路追踪（TraceId）和文件滚动策略。基于 Log4j2 实现，支持控制台彩色输出和 JSON 格式文件输出，便于 ELK 等日志系统采集。

## 快速开始

引入依赖后，模块会自动生效，无需额外配置。

## 配置项

配置前缀：`tml.log`

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `enable` | 是否启用日志模块 | `true` |
| `fileName` | 日志文件名（同时作为应用标识） | `unknown` |
| `path` | 日志文件存储路径 | `/app/log` |
| `level` | 日志级别 | `INFO` |
| `fileMaxSize` | 单个日志文件最大大小 | `100M` |
| `fileMaxDays` | 日志文件保留天数 | `7` |
| `charset` | 日志字符编码 | `UTF-8` |
| `trace` | 是否启用链路追踪 | `true` |
| `env` | 环境标识（prod/dev/test） | `prod` |

### 配置示例
一般只需要针对fileName和path做一下修改，关于env这里会读取spring.profiles.active中的值，有值即可
```yaml
tml:
  log:
    enable: true
    fileName: my-app
    path: /var/log/myapp
    level: DEBUG
    fileMaxSize: 200M
    fileMaxDays: 30
    charset: UTF-8
    trace: true
    env: dev
```

## 核心功能

### 1. 链路追踪（TraceId）

每个 HTTP 请求会自动生成唯一的 `traceId`，贯穿整个请求链路，便于日志追踪和问题排查。

**工作原理：**
- `TraceIdWebFilter` 作为最高优先级过滤器，在请求进入时生成或获取 traceId
- traceId 存储在 SLF4J 的 MDC 中，日志输出时自动携带
- 响应头 `X-Trace-Id` 会返回当前请求的 traceId

**traceId 来源优先级：**
1. 请求头 `X-Trace-Id`（支持上游服务传递）
2. 自动生成 32 位 UUID

### 2. 定时任务链路追踪

`TraceIdScheduledAspect` 切面会自动为 `@Scheduled` 注解的定时任务方法注入 traceId，无需手动处理。

**工作原理：**
- 通过 AOP 环绕通知拦截所有 `@Scheduled` 方法
- 在方法执行前自动生成新的 traceId 并放入 MDC
- 方法执行完毕后自动清理 MDC，防止线程复用导致数据污染

**使用示例：**
```java
@Component
public class MyScheduledTask {
    
    private static final Logger log = LoggerFactory.getLogger(MyScheduledTask.class);
    
    @Scheduled(fixedRate = 60000)
    public void execute() {
        // traceId 已自动注入，直接打印日志即可
        log.info("定时任务执行中...");
    }
}
```

### 3. 日志输出格式

**控制台输出：** 彩色格式，便于开发调试
```
2025-01-14 10:30:00 [abc123def456] [main] INFO  com.example.Service - 业务日志
```

**文件输出：** JSON 格式，便于 ELK 采集
```json
{"app":"my-app","env":"prod","traceId":"abc123def456","level":"INFO","message":"业务日志",...}
```

### 4. 文件滚动策略

- 按小时滚动，文件路径：`{path}/{fileName}/{yyyy-MM-dd}/{yyyy-MM-dd_HH}.log.gz`
- 自动压缩历史日志（gzip）

## 架构说明

```
log/
├── config/
│   ├── TmlLog.java              # 配置枚举，定义所有配置项和常量
│   ├── TmlLogProperties.java    # Spring Boot 配置属性类
│   └── TmlLogAutoConfiguration.java  # 自动配置类
├── interceptor/
│   ├── TraceIdWebFilter.java    # HTTP 请求链路追踪过滤器
│   └── TraceIdScheduledAspect.java  # 定时任务链路追踪切面
└── resources/
    └── log4j2-spring.xml        # Log4j2 配置文件
```

## 注意事项

1. 确保项目中引入了 Log4j2 依赖
2. `fileName` 建议设置为应用名称，便于日志区分
3. 生产环境建议 `level` 设置为 `INFO` 或 `WARN`
4. 链路追踪默认开启，如需关闭设置 `tml.log.trace=false`
