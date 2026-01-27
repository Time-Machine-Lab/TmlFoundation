# TmlLog 日志模块

## 概述

TmlLog 是一个开箱即用的日志配置模块，提供统一的日志格式、链路追踪（TraceId）和文件滚动策略。基于 Log4j2 实现，支持控制台彩色输出和 JSON 格式文件输出，便于 ELK 等日志系统采集。

**核心特性：**
- 🎯 基于 TTL 的 ThreadContextMap 实现多线程链路追踪
- 🚀 HTTP 请求自动生成并传递 traceId
- ⏰ 定时任务自动生成独立 traceId
- 🔄 支持多线程、异步任务、线程池场景下的 traceId 自动传递
- 🔌 可扩展的 TraceContext 接口，支持自定义实现
- 📝 控制台彩色输出 + JSON 文件输出
- 🗂️ 自动日志滚动和清理（按小时滚动，自动压缩）

## 快速开始

引入依赖后，模块会自动生效，无需额外配置。一般只需要配置 `fileName` 和 `path` 即可！

路径强烈建议使用绝对路径，并确保应用有写权限，不然docker映射文件可能会失败。
其他配置项如下参考。
```yaml
tml:
  log:
    file-name: my-app
    path: /app/logs/myapp
```

## 配置项

配置前缀：`tml.log`

| 配置项 | 说明 | 默认值         |
|--------|------|-------------|
| `enable` | 是否启用日志模块 | `true`      |
| `fileName` | 日志文件名（同时作为应用标识） | `unknown`   |
| `path` | 日志文件存储路径 | `/app/logs` |
| `level` | 日志级别（TRACE/DEBUG/INFO/WARN/ERROR） | `INFO`      |
| `fileMaxSize` | 单个日志文件最大大小 | `200M`      |
| `fileMaxDays` | 日志文件保留天数 | `7`         |
| `charset` | 日志字符编码 | `UTF-8`     |
| `traceId` | 是否启用链路追踪 | `true`      |
| `env` | 环境标识（DEV/TEST/PROD） | `PROD`      |
| `pattern` | 控制台日志输出格式 | 彩色格式（见下方说明） |

### 完整配置示例

```yaml
tml:
  log:
    enable: true
    file-name: my-app
    path: /app/logs/myapp
    level: INFO
    file-max-size: 200M
    file-max-days: 30
    charset: UTF-8
    trace-id: true
    env: PROD
```

**默认 pattern 格式：**
```
%d{yyyy-MM-dd HH:mm:ss} %highlight{[%X{traceId}]} [%thread] %highlight{%-5level}{FATAL=red, ERROR=red, WARN=yellow, INFO=green, DEBUG=cyan, TRACE=blue} %style{%logger{36}}{cyan} - %msg%n
```

## 核心功能

### 1. 链路追踪（TraceId）

基于 TTL（Transmittable ThreadLocal）实现的全链路追踪方案，通过自定义 Log4j2 的 `ThreadContextMap` 实现多线程场景下的 traceId 自动传递。

**核心组件：**

| 组件 | 说明 |
|------|------|
| `TmlLogTraceContext` | 链路上下文接口，定义 traceId 的存取、生成等操作，支持自定义实现 |
| `DefaultTraceContext` | 默认实现，基于 SLF4J MDC 实现 traceId 存储 |
| `TmlLogThreadContextMap` | 基于 TTL 的 Log4j2 ThreadContextMap 实现，核心组件 |
| `TmlLogWebTrace` | HTTP 请求过滤器，自动生成/获取 traceId 并注入到上下文 |
| `TmlLogScheduleTrace` | 定时任务切面，自动为 `@Scheduled` 方法注入 traceId |
| `TmlLogExecutorsTrace` | 线程池包装工具类（可选，用于不使用 TTL Agent 的场景） |

**工作原理：**
1. `TmlLogWebTrace` 作为最高优先级过滤器，在 HTTP 请求进入时生成或获取 traceId
2. traceId 通过 `TmlLogTraceContext` 存储到 MDC 中
3. `TmlLogThreadContextMap` 使用 TTL 的 `TransmittableThreadLocal` 替代 Log4j2 默认的 `ThreadLocal`
4. 配合 TTL Java Agent 使用时，所有线程池自动传递 traceId，无需手动包装
5. 响应头 `Tml-Trace-Id` 会返回当前请求的 traceId

**traceId 来源优先级：**
1. 请求头 `Tml-Trace-Id`（支持上游服务传递）
2. 自动生成 32 位 UUID（去除连字符）

### 2. 定时任务链路追踪

`TmlLogScheduleTrace` 切面会自动为 `@Scheduled` 注解的定时任务方法注入 traceId，无需手动处理。

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
- 自动删除超过保留天数的日志

## 多线程链路追踪

### 核心机制

TmlLog 通过 `TmlLogThreadContextMap` 实现了基于 TTL 的 Log4j2 ThreadContextMap，配置在 `log4j2.component.properties` 中：

```properties
log4j2.threadContextMap=io.github.timemachinelab.log.interceptor.TmlLogThreadContextMap
```

**核心原理：**
- `TmlLogThreadContextMap` 使用 TTL 的 `TransmittableThreadLocal` 替代普通 `ThreadLocal`
- `TransmittableThreadLocal` 的 `childValue()` 方法在创建子线程时自动复制父线程的值
- Log4j2 通过 `%X{traceId}` 获取 ThreadContextMap 中的值

### 使用方式对比

| 使用方式 | 配置要求 | 适用场景 | 优点 | 缺点 |
|---------|---------|---------|------|------|
| **方式一：默认方式** | 无需配置 | `new Thread()`、简单线程池 | ✅ 开箱即用<br>✅ 零配置 | ⚠️ 线程池复用场景可能需要额外处理 |
| **方式二：TTL Agent** | JVM 启动参数 | 所有场景（推荐生产环境） | ✅ 最彻底<br>✅ 零代码侵入<br>✅ 覆盖所有场景 | ⚠️ 需要修改启动参数 |
| **方式三：手动包装** | 代码包装 | 无法使用 Agent 的场景 | ✅ 不需要修改启动参数 | ❌ 需要手动包装<br>❌ 容易遗漏 |

### 方式一：默认方式（开箱即用）

**无需任何配置，引入依赖即可使用。**

`TransmittableThreadLocal` 的 `childValue()` 方法会在创建子线程时自动复制父线程的值，因此对于直接创建的子线程，traceId 会自动传递。

**自动支持的场景：**

```java
// ✅ 直接创建线程
new Thread(() -> {
    log.info("traceId 自动传递");
}).start();

// ✅ 简单的线程池场景
ExecutorService executor = Executors.newFixedThreadPool(10);
executor.submit(() -> {
    log.info("traceId 自动传递");
});

// ✅ CompletableFuture
CompletableFuture.runAsync(() -> {
    log.info("traceId 自动传递");
});

// ✅ Spring @Async（默认配置）
@Async
public void asyncMethod() {
    log.info("traceId 自动传递");
}
```

**说明：**
- 对于大多数场景，默认方式已经足够
- 如果遇到 traceId 未传递的情况，可以考虑使用 TTL Agent 或手动包装

### 方式二：使用 TTL Java Agent

在 JVM 启动参数中添加：

```bash
java -javaagent:transmittable-thread-local-2.x.x.jar -jar your-app.jar
```

**优点：**
- ✅ 零代码侵入，所有线程池自动支持
- ✅ 包括 JDK 原生线程池、Spring @Async、CompletableFuture、定时任务线程池等
- ✅ 最简单、最彻底的解决方案
- ✅ 性能开销极小

**原理：** Agent 会在类加载时修改 JDK 线程池相关类的字节码，自动包装所有线程池，确保线程池复用时 traceId 正确传递。

**使用示例：**

配置 Agent 后，所有场景都自动支持，无需额外代码：

```java
// 1. 普通线程池 - 自动支持
ExecutorService executor = Executors.newFixedThreadPool(10);
executor.submit(() -> {
    log.info("traceId 自动传递");
});

// 2. Spring @Async - 自动支持
@Async
public void asyncMethod() {
    log.info("traceId 自动传递");
}

// 3. CompletableFuture - 自动支持
CompletableFuture.runAsync(() -> {
    log.info("traceId 自动传递");
});

// 4. 定时任务 - 自动支持（已有切面）
@Scheduled(fixedRate = 60000)
public void scheduledTask() {
    log.info("traceId 自动生成并传递");
}
```

### 方式三：显式包装线程池或任务（不推荐）

如果不使用 Agent，且默认方式无法满足需求，可以使用 `TmlLogExecutorsTrace` 工具类显式包装：

**优点：**
- ✅ 不需要修改 JVM 启动参数
- ✅ 代码级控制，更灵活

**缺点：**
- ❌ 需要手动包装所有自定义线程池
- ❌ 容易遗漏

#### 1. Spring @Async 异步方法

配置 `ThreadPoolTaskExecutor` 并设置 `TaskDecorator`：

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
        executor.setTaskDecorator(TmlLogExecutorsTrace.wrap());
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

#### 2. 包装已有线程池

使用 `TmlLogExecutorsTrace.wrap()` 包装已有线程池：

```java
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    // 包装已有线程池
    private final ExecutorService executor = TmlLogExecutorsTrace.wrap(
        Executors.newFixedThreadPool(10)
    );

    public void processOrder(String orderId) {
        log.info("开始处理订单: {}", orderId);

        // 直接提交任务，traceId 自动传递
        executor.submit(() -> {
            log.info("异步处理订单: {}", orderId);
        });
    }
}
```

可用包装方法：

```java
// 包装普通线程池
ExecutorService wrapped = TmlLogExecutorsTrace.wrap(executor);

// 包装定时任务线程池
ScheduledExecutorService wrapped = TmlLogExecutorsTrace.wrap(scheduledExecutor);

// 包装单个 Runnable 任务
Runnable wrapped = TmlLogExecutorsTrace.wrap(runnable);

// 包装单个 Callable 任务
Callable<T> wrapped = TmlLogExecutorsTrace.wrap(callable);
```

#### 3. CompletableFuture

```java
@Service
public class AggregateService {

    private static final Logger log = LoggerFactory.getLogger(AggregateService.class);

    private final ExecutorService executor = TmlLogExecutorsTrace.wrap(
        Executors.newFixedThreadPool(10)
    );

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

#### 4. 定时任务中的异步调用

定时任务会自动生成独立的 traceId，子线程会继承该 traceId：

```java
@Component
public class MyScheduledTask {

    private static final Logger log = LoggerFactory.getLogger(MyScheduledTask.class);

    private final ExecutorService executor = TmlLogExecutorsTrace.wrap(
        Executors.newFixedThreadPool(5)
    );

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

### 手动操作 traceId

```java
// 获取 TmlLogTraceContext 实例
TmlLogTraceContext context = TmlLogTraceContext.Holder.get();

// 获取当前 traceId
String traceId = context.get(context.getTraceIdKey());

// 手动设置 traceId（一般不需要）
context.set(context.getTraceIdKey(), "custom-trace-id");

// 清理 traceId
context.clear();

// 生成新的 traceId
String newTraceId = context.generateTraceId();
```

### 日志输出示例

使用 TTL Agent 或正确包装后，所有线程的日志都会有相同的 traceId：

```
2026-01-15 10:30:00 [abc123def456] [http-nio-8080-exec-1] INFO  OrderController - 开始处理订单
2026-01-15 10:30:00 [abc123def456] [pool-1-thread-1] INFO  OrderController - 异步处理订单
2026-01-15 10:30:00 [abc123def456] [pool-1-thread-2] INFO  AggregateService - 查询订单基本信息
2026-01-15 10:30:00 [abc123def456] [pool-1-thread-3] INFO  AggregateService - 查询订单商品
```

所有线程的 traceId 都是 `abc123def456`，完整链路可追踪。

## 架构说明

```
log/
├── config/
│   ├── TmlLogConstant.java          # 日志常量类，定义所有配置项和默认值
│   └── TmlLogProperties.java        # Spring Boot 配置属性类
├── context/
│   ├── TmlLogTraceContext.java      # 链路上下文接口，支持自定义实现
│   └── DefaultTraceContext.java     # 默认实现，基于 SLF4J MDC
├── interceptor/
│   ├── TmlLogThreadContextMap.java  # 基于 TTL 的 Log4j2 ThreadContextMap（核心）
│   ├── TmlLogWebTrace.java          # HTTP 请求链路追踪过滤器
│   ├── TmlLogScheduleTrace.java     # 定时任务链路追踪切面
│   └── TmlLogExecutorsTrace.java    # 线程池包装工具类（可选）
├── TmlLogAutoConfiguration.java     # 自动配置类，注册 Filter 和 Aspect
├── TmlLogEnvPostProcessor.java      # 环境后置处理器，加载配置到系统属性
└── resources/
    ├── log4j2-spring.xml            # Log4j2 配置文件（正常模式）
    ├── log4j2-noop.xml              # Log4j2 配置文件（禁用模式）
    └── log4j2.component.properties  # Log4j2 组件配置（指定 ThreadContextMap）
```

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

### TraceId 存储与传递机制

**核心实现：TmlLogThreadContextMap**

```java
public class TmlLogThreadContextMap implements ThreadContextMap {
    
    // 使用 TTL 的 TransmittableThreadLocal 替代普通 ThreadLocal
    private final TransmittableThreadLocal<Map<String, String>> ttlThreadLocal =
        new TransmittableThreadLocal<Map<String, String>>() {
            @Override
            protected Map<String, String> childValue(Map<String, String> parentValue) {
                // 子线程自动继承父线程的上下文
                return parentValue != null ? new HashMap<>(parentValue) : new HashMap<>();
            }
        };
    
    @Override
    public void put(String key, String value) {
        ttlThreadLocal.get().put(key, value);
    }
    
    @Override
    public String get(String key) {
        return ttlThreadLocal.get().get(key);
    }
    
    // ... 其他方法
}
```

**配置 Log4j2 使用自定义 ThreadContextMap：**

在 `log4j2.component.properties` 中配置：
```properties
log4j2.threadContextMap=io.github.timemachinelab.log.interceptor.TmlLogThreadContextMap
```
如果需要使用其他的链路追踪工具，自定义实现ThreadContextMap，然后再上述配置替换成自己的

### TTL 的工作模式

#### 模式一：TTL Java Agent（自动模式）

启动时添加 Agent：
```bash
-javaagent:transmittable-thread-local-2.x.x.jar
```

**工作原理：**
- Agent 会在类加载时修改 JDK 线程池相关类的字节码
- 自动包装所有 `Executor`、`ExecutorService`、`ThreadPoolExecutor` 等
- 无需修改业务代码，所有线程池自动支持 TTL 传递

#### 模式二：显式包装（手动模式）

不使用 Agent 时，需要手动包装：

```java
// 包装线程池
ExecutorService wrapped = TtlExecutors.getTtlExecutorService(executor);

// 包装任务
Runnable wrapped = TtlRunnable.get(runnable);
```

`TmlLogExecutorsTrace` 就是对这些 API 的封装，简化使用。

### 配置加载流程

1. `TmlLogEnvPostProcessor` 在 Spring 环境准备阶段执行（优先级：HIGHEST_PRECEDENCE + 11）
2. 从 `application.yml` 读取 `tml.log.*` 配置
3. 将配置写入系统属性（`System.setProperty`）
4. Log4j2 通过 `${sys:tml.log.*}` 读取系统属性
5. 如果 `enable=false`，则加载 `log4j2-noop.xml` 完全禁用日志

### 自定义 TraceContext

可以通过实现 `TmlLogTraceContext` 接口并注册为 Bean 来自定义 traceId 的存储和生成方式：

```java
@Component
public class CustomTraceContext implements TmlLogTraceContext {
    
    @Override
    public void set(String key, String value) {
        // 自定义存储逻辑
    }
    
    @Override
    public String get(String key) {
        // 自定义获取逻辑
        return null;
    }
    
    @Override
    public String generateTraceId() {
        // 自定义 traceId 生成逻辑
        return "custom-" + System.currentTimeMillis();
    }
    
    @Override
    public String getTraceIdHeader() {
        // 自定义请求头名称
        return "X-Custom-Trace-Id";
    }
    
    // ... 其他方法实现
}
```

`TmlLogAutoConfiguration` 会自动检测并使用自定义实现。

## 注意事项

1. 确保项目中引入了 Log4j2 和阿里 TTL（transmittable-thread-local）依赖
2. `fileName` 建议设置为应用名称，便于日志区分和 ELK 采集
3. 生产环境建议 `level` 设置为 `INFO` 或 `WARN`
4. 链路追踪默认开启，如需关闭设置 `tml.log.traceId=false`
5. **默认方式已支持大多数场景**，无需额外配置
6. 如果不使用 Agent 且默认方式无法满足需求，可使用 `TmlLogExecutorsTrace.wrap()` 包装线程池或任务
7. 定时任务会自动生成新的 traceId，与 HTTP 请求的 traceId 相互独立
8. 设置 `tml.log.enable=false` 会完全禁用日志模块，使用 `log4j2-noop.xml` 配置
9. 可以通过实现 `TmlLogTraceContext` 接口并注册为 Bean 来自定义 traceId 的存储和生成方式

## 常见问题

### Q: 定时任务的 traceId 和 HTTP 请求的 traceId 会冲突吗？

A: 不会。定时任务会生成独立的 traceId，与 HTTP 请求相互隔离。

### Q: 如何在微服务间传递 traceId？

A: 调用下游服务时，在请求头中添加 `Tml-Trace-Id`：
```java
TmlLogTraceContext context = TmlLogTraceContext.Holder.get();
String traceId = context.get(context.getTraceIdKey());
httpHeaders.set(context.getTraceIdHeader(), traceId);
```
下游服务的 `TmlLogWebTrace` 会自动从请求头获取 traceId。

### Q: 日志文件在哪里？

A: 默认路径为 `/app/logs/{fileName}/{yyyy-MM-dd}/{yyyy-MM-dd_HH}.log.gz`，可通过 `tml.log.path` 和 `tml.log.fileName` 配置。

### Q: TmlLogExecutorsTrace 是必须的吗？
