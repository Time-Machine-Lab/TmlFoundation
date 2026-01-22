# AssertUtils 断言工具类使用说明

## 概述

`AssertUtils` 用于在运行时做快速校验：当条件不满足时立即抛出异常，避免错误状态继续传播。

- 参数校验：推荐抛出 `IllegalArgumentException`
- 状态校验：推荐抛出 `IllegalStateException`
- 支持 `Supplier<String>` 形式的延迟消息构建（性能友好）

## 快速开始

```java
import io.github.timemachinelab.util.AssertUtils;

public void createUser(String name, Integer age) {
    AssertUtils.hasText(name, "name must not be blank");
    AssertUtils.notNull(age, "age must not be null");
    AssertUtils.isTrue(age > 0, () -> "age must be positive, but was " + age);
}
```

## API 列表

| 方法签名 | 断言含义（失败即抛异常） | 异常类型 |
|---|---|---|
| `isTrue(boolean expression, String message)` | 表达式必须为 `true` | `IllegalArgumentException` |
| `isTrue(boolean expression, Supplier<String> messageSupplier)` | 同上，消息延迟构建 | `IllegalArgumentException` |
| `state(boolean expression, String message)` | 状态表达式必须为 `true` | `IllegalStateException` |
| `state(boolean expression, Supplier<String> messageSupplier)` | 同上，消息延迟构建 | `IllegalStateException` |
| `isNull(Object object, String message)` | 对象必须为 `null` | `IllegalArgumentException` |
| `isNull(Object object, Supplier<String> messageSupplier)` | 同上，消息延迟构建 | `IllegalArgumentException` |
| `notNull(Object object, String message)` | 对象必须非 `null` | `IllegalArgumentException` |
| `notNull(Object object, Supplier<String> messageSupplier)` | 同上，消息延迟构建 | `IllegalArgumentException` |
| `hasLength(String text, String message)` | 字符串必须非 `null` 且长度 > 0 | `IllegalArgumentException` |
| `hasLength(String text, Supplier<String> messageSupplier)` | 同上，消息延迟构建 | `IllegalArgumentException` |
| `hasText(String text, String message)` | 字符串必须包含至少 1 个非空白字符 | `IllegalArgumentException` |
| `hasText(String text, Supplier<String> messageSupplier)` | 同上，消息延迟构建 | `IllegalArgumentException` |
| `notEmpty(Object[] array, String message)` | 对象数组必须非 `null` 且长度 > 0 | `IllegalArgumentException` |
| `notEmpty(Object[] array, Supplier<String> messageSupplier)` | 同上，消息延迟构建 | `IllegalArgumentException` |
| `isInstanceOf(Class<?> type, Object obj, String message)` | `obj` 必须是 `type` 的实例 | `IllegalArgumentException` |
| `isInstanceOf(Class<?> type, Object obj, Supplier<String> messageSupplier)` | 同上，消息延迟构建 | `IllegalArgumentException` |

## 使用说明

### 1) 参数校验（IllegalArgumentException）

```java
public void setTimeoutMs(Long timeoutMs) {
    AssertUtils.notNull(timeoutMs, "timeoutMs must not be null");
    AssertUtils.isTrue(timeoutMs >= 0, "timeoutMs must be >= 0");
}
```

### 2) 状态校验（IllegalStateException）

```java
public void initOnce() {
    AssertUtils.state(!this.initialized, "already initialized");
    this.initialized = true;
}
```

### 3) isNull / notNull

```java
AssertUtils.isNull(entity.getId(), "id must be null before persist");
AssertUtils.notNull(entity.getId(), "id must not be null after persist");
```

### 4) hasLength vs hasText

- `hasLength`：不允许 `null`/空字符串，但允许纯空格
- `hasText`：不允许 `null`/空字符串/纯空白

```java
AssertUtils.hasLength(" ", "length ok, text may be blank");
AssertUtils.hasText("a", "must contain non-whitespace");
```

### 5) notEmpty(Object[])

```java
AssertUtils.notEmpty(new String[]{"a"}, "array must not be empty");
```

### 6) isInstanceOf

- `type` 不允许为 `null`（会先做 `notNull` 校验）

```java
Object payload = "hello";
AssertUtils.isInstanceOf(String.class, payload, "payload type mismatch");
```

### 7) Supplier 形式的消息（推荐用于复杂字符串拼接）

```java
AssertUtils.isTrue(result > 0, () -> "result must be > 0, but was " + result);
```
