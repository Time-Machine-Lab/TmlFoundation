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

基于 TTL（Transmittable ThreadLocal）实现的全链路追踪方案，自动支持线程池场景下的 traceId 传递。

**核心组件：**

| 组件 | 说明 |
|------|------|
| `TraceIdHolder` | TraceId 持有者，基于 TTL（TransmittableThreadLocal）统一管理 traceId 的存取 |
| `TraceIdWebFilter` | HTTP 请求入口过滤器，自动生成/获取 traceId |
| `TraceIdScheduledAspect` | 定时任务切面，自动为 `@Scheduled` 方法注入 traceId |
| `TmlLogExecutors` | 线程池工具类，创建支持 traceId 传递的线程池，同时提供 TaskDecorator |

**工作原理：**
- `TraceIdWebFilter` 作为最高优先级过滤器，在请求进入时生成或获取 traceId
- traceId 存储在 TTL 中，自动支持线程池传递，同时同步到 MDC 供日志输出
- 响应头 `X-Trace-Id` 会返回当前请求的 traceId

**traceId 来源优先级：**
1. 请求头 `X-Trace-Id`（支持上游服务传递）
2. 自动生成 32 位 UUID

### 2. 定时任务链路追踪

`TraceIdScheduledAspect` 切面会自动为 `@Scheduled` 注解的定时任务方法注入 traceId，无需手动处理。

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
2026-01-15 10:30:00 [abc123def456] [main] INFO  com.example.Service - 业务日志
```

**文件输出：** JSON 格式，便于 ELK 采集
```json
{"app":"my-app","env":"prod","traceId":"abc123def456","level":"INFO","message":"业务日志",...}
```

### 4. 文件滚动策略

- 按小时滚动，文件路径：`{path}/{fileName}/{yyyy-MM-dd}/{yyyy-MM-dd_HH}.log.gz`
- 自动压缩历史日志（gzip）

## 多线程链路追踪使用指南

### 1. Spring @Async 异步方法

配置 `ThreadPoolTaskExecutor` 并设置 `TmlLogExecutors.wrap()` 作为 TaskDecorator：

```java
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean("asyncExecutor")
    public ThreadPoolTaskExecutor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("async-");
        // 关键：设置 TaskDecorator
        executor.setTaskDecorator(TmlLogExecutors.wrap());
        executor.initialize();
        return executor;
    }
}
```

使用：

```java
@Service
public class NotifyService {

    private static final Logger log = LoggerFactory.getLogger(NotifyService.class);

    @Async("asyncExecutor")
    public void sendEmail(String email) {
        // traceId 自动传递
        log.info("发送邮件到: {}", email);
    }
}
```

### 2. 使用 TmlLogExecutors 创建线程池
不推荐使用，具体原因自行探查八股文
```java
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    // 直接使用工具类创建线程池
    private final ExecutorService executor = TmlLogExecutors.newFixedThreadPool(10);

    public void processOrder(String orderId) {
        log.info("开始处理订单: {}", orderId);

        // 直接提交任务，traceId 自动传递
        executor.submit(() -> {
            log.info("异步处理订单: {}", orderId);
        });
    }
}
```

可用方法：

```java
// 固定大小线程池
ExecutorService executor = TmlLogExecutors.newFixedThreadPool(10);

// 缓存线程池
ExecutorService executor = TmlLogExecutors.newCachedThreadPool();

// 单线程线程池
ExecutorService executor = TmlLogExecutors.newSingleThreadExecutor();

// 定时任务线程池
ScheduledExecutorService executor = TmlLogExecutors.newScheduledThreadPool(5);
```

### 3. 包装已有线程池

```java
@Configuration
public class ThreadPoolConfig {

    @Bean
    public ExecutorService businessExecutor() {
        // 已有的线程池配置
        ThreadPoolExecutor original = new ThreadPoolExecutor(
            10, 20, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000),
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
        // 用 TmlLogExecutors.wrap 包装
        return TmlLogExecutors.wrap(original);
    }
}
```

### 4. CompletableFuture

```java
@Service
public class AggregateService {

    private static final Logger log = LoggerFactory.getLogger(AggregateService.class);

    private final ExecutorService executor = TmlLogExecutors.newFixedThreadPool(10);

    public OrderDetail getOrderDetail(String orderId) {
        log.info("聚合查询订单详情");

        // 并行查询，traceId 自动传递
        CompletableFuture<Order> orderFuture = CompletableFuture.supplyAsync(
            () -> {
                log.info("查询订单基本信息");
                return orderDao.findById(orderId);
            },
            executor
        );

        CompletableFuture<List<Item>> itemsFuture = CompletableFuture.supplyAsync(
            () -> {
                log.info("查询订单商品");
                return itemDao.findByOrderId(orderId);
            },
            executor
        );

        return CompletableFuture.allOf(orderFuture, itemsFuture)
            .thenApply(v -> new OrderDetail(orderFuture.join(), itemsFuture.join()))
            .join();
    }
}
```

### 5. 嵌套异步场景

```java
@Service
public class ComplexService {

    private static final Logger log = LoggerFactory.getLogger(ComplexService.class);

    private final ExecutorService executor = TmlLogExecutors.newFixedThreadPool(10);
    private final ExecutorService innerExecutor = TmlLogExecutors.newFixedThreadPool(5);

    public void complexProcess() {
        log.info("主线程开始");

        executor.submit(() -> {
            log.info("第一层异步");

            // 嵌套提交，traceId 继续传递
            innerExecutor.submit(() -> {
                log.info("第二层异步");

                // 再嵌套也没问题
                CompletableFuture.runAsync(() -> {
                    log.info("第三层异步");
                }, executor);
            });
        });
    }
}
```

### 6. 定时任务中的异步调用

定时任务会自动生成独立的 traceId，子线程会继承该 traceId：

```java
@Component
public class MyScheduledTask {

    private static final Logger log = LoggerFactory.getLogger(MyScheduledTask.class);

    private final ExecutorService executor = TmlLogExecutors.newFixedThreadPool(5);

    @Scheduled(cron = "0 * * * * ?")
    public void execute() {
        log.info("定时任务开始");

        // 子线程继承定时任务的 traceId
        executor.submit(() -> {
            log.info("定时任务的异步子任务");
        });
    }
}
```

### 7. 手动包装单个任务

如果使用的是未包装的线程池，可以手动包装单个任务：

```java
// 未包装的原始线程池
ExecutorService rawExecutor = Executors.newFixedThreadPool(10);

// 手动包装任务
rawExecutor.submit(TmlLogExecutors.wrap(() -> {
    log.info("手动包装的任务");
}));

// Callable 也支持
Future<String> future = rawExecutor.submit(TmlLogExecutors.wrap(() -> {
    log.info("手动包装的 Callable");
    return "result";
}));
```

### 8. 手动获取/设置 traceId

```java
// 获取当前 traceId
String traceId = TraceIdHolder.get();

// 手动设置 traceId（一般不需要）
TraceIdHolder.set("custom-trace-id");

// 清理 traceId
TraceIdHolder.clear();
```

### 日志输出示例

```
2026-01-15 10:30:00 [http-nio-8080-exec-1] [abc123def456] INFO  OrderService - 开始处理订单: ORD001
2026-01-15 10:30:00 [async-1]              [abc123def456] INFO  OrderService - 异步处理订单: ORD001
2026-01-15 10:30:00 [async-2]              [abc123def456] INFO  AggregateService - 查询订单基本信息
2026-01-15 10:30:00 [async-3]              [abc123def456] INFO  AggregateService - 查询订单商品
```

所有线程的 traceId 都是 `abc123def456`，完整链路可追踪。

## 架构说明

```
log/
├── config/
│   ├── TmlLog.java                  # 配置枚举，定义所有配置项、常量和 traceId 生成方法
│   ├── TmlLogProperties.java        # Spring Boot 配置属性类
│   ├── TmlLogAutoConfiguration.java # 自动配置类，注册 Filter 和 Aspect
│   └── TmlLogEnvPostProcessor.java  # 环境后置处理器，加载配置到系统属性
├── interceptor/
│   ├── TraceIdHolder.java           # TraceId 持有者（TTL + MDC 双写）
│   ├── TraceIdWebFilter.java        # HTTP 请求链路追踪过滤器
│   ├── TraceIdScheduledAspect.java  # 定时任务链路追踪切面
│   └── TmlLogExecutors.java         # 线程池工具类（含 TaskDecorator）
└── resources/
    ├── log4j2-spring.xml            # Log4j2 配置文件（正常模式）
    └── log4j2-noop.xml              # Log4j2 配置文件（禁用模式）
```

## 注意事项

1. 确保项目中引入了 Log4j2 和阿里 TTL（transmittable-thread-local）依赖
2. `fileName` 建议设置为应用名称，便于日志区分和 ELK 采集
3. 生产环境建议 `level` 设置为 `INFO` 或 `WARN`
4. 链路追踪默认开启，如需关闭设置 `tml.log.trace=false`
5. 使用 `TmlLogExecutors` 创建的线程池或用 `TmlLogExecutors.wrap()` 装饰的线程池，traceId 会自动传递
6. 使用原始线程池时，需要用 `TmlLogExecutors.wrap()` 包装任务
7. `CompletableFuture` 需要指定包装后的线程池，否则默认使用 `ForkJoinPool` 无法传递 traceId
8. 定时任务会自动生成新的 traceId，与 HTTP 请求的 traceId 相互独立
9. `env` 配置项会自动从 `spring.profiles.active` 读取，无需手动配置
10. 设置 `tml.log.enable=false` 会完全禁用日志模块，使用 `log4j2-noop.xml` 配置


## 依赖说明

本模块依赖以下核心组件：

```xml
<!-- Log4j2 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-log4j2</artifactId>
</dependency>

<!-- 阿里 TTL（Transmittable ThreadLocal） -->
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>transmittable-thread-local</artifactId>
</dependency>

<!-- AOP（用于定时任务切面） -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

## 实现原理

### TraceId 存储机制

采用 TTL + MDC 双写策略：
- **TTL（TransmittableThreadLocal）**：作为主存储，自动支持线程池场景下的 traceId 传递
- **MDC（Mapped Diagnostic Context）**：供 Log4j2 日志输出使用

```java
// TraceIdHolder 核心实现
private static final TransmittableThreadLocal<String> TRACE_ID = new TransmittableThreadLocal<>();

public static void set(String traceId) {
    TRACE_ID.set(traceId);      // 写入 TTL
    MDC.put("traceId", traceId); // 同步到 MDC
}
```

### 线程池包装原理

`TmlLogExecutors` 通过以下方式实现 traceId 传递：

1. **TTL 包装**：使用 `TtlExecutors.getTtlExecutorService()` 包装线程池，自动传递 TTL 中的值
2. **MDC 同步**：在任务执行前调用 `TraceIdHolder.syncToMdc()` 将 TTL 值同步到 MDC
3. **清理机制**：任务执行后清理 MDC，防止线程复用时数据污染

### 配置加载流程

1. `TmlLogEnvPostProcessor` 在 Spring 环境准备阶段执行
2. 从 `application.yml` 读取 `tml.log.*` 配置
3. 将配置写入系统属性（`System.setProperty`）
4. Log4j2 通过 `${sys:tml.log.*}` 读取系统属性

## 常见问题

### Q: 为什么子线程的日志没有 traceId？

A: 检查以下几点：
1. 是否使用了 `TmlLogExecutors` 创建或包装的线程池
2. `CompletableFuture` 是否指定了包装后的线程池
3. 如果使用原始线程池，是否用 `TmlLogExecutors.wrap()` 包装了任务

### Q: 定时任务的 traceId 和 HTTP 请求的 traceId 会冲突吗？

A: 不会。定时任务会生成独立的 traceId，与 HTTP 请求相互隔离。

### Q: 如何在微服务间传递 traceId？

A: 调用下游服务时，在请求头中添加 `X-Trace-Id`：
```java
String traceId = TraceIdHolder.get();
httpHeaders.set("X-Trace-Id", traceId);
```
下游服务的 `TraceIdWebFilter` 会自动从请求头获取 traceId。

### Q: 日志文件在哪里？

A: 默认路径为 `/app/log/{fileName}/{fileName}.log`，可通过 `tml.log.path` 和 `tml.log.fileName` 配置。
