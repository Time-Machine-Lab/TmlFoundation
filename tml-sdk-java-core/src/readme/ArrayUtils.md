# ArrayUtils 数组工具类使用说明

## 概述

`ArrayUtils` 提供轻量的数组通用能力：

- 获取任意 Java 数组的长度（支持基本类型数组与对象数组）
- 判断对象数组是否为空

适用于参数校验、通用组件开发与框架层基础能力。

## 依赖与约束

- 仅依赖 JDK 标准库（`java.lang.reflect.Array`）
- `getLength(Object array)` 仅接受“数组对象”；非数组会抛出 `IllegalArgumentException`
- 类上标注 `@Beta`，表示 API 可能在后续版本调整

## 快速开始

```java
import io.github.timemachinelab.util.ArrayUtils;

int len1 = ArrayUtils.getLength(new int[]{1, 2, 3});
int len2 = ArrayUtils.getLength(new String[]{"a", "b"});
int len3 = ArrayUtils.getLength(null); // 0

boolean empty = ArrayUtils.isEmpty(new Object[]{}); // true
boolean notEmpty = ArrayUtils.notEmpty(new Object[]{"x"}); // true
```

## API 列表

| 方法签名 | 说明 | 返回值 | 异常 |
|---|---|---|---|
| `int getLength(Object array)` | 获取数组长度；`null` 返回 0 | `int` | `IllegalArgumentException`：入参不是数组 |
| `boolean isEmpty(Object[] array)` | 判断对象数组是否为 `null` 或长度为 0 | `boolean` | - |
| `boolean notEmpty(Object[] array)` | `isEmpty` 的取反 | `boolean` | - |

## 方法说明

### getLength(Object array)

- 典型场景：需要同时兼容 `int[]`、`long[]`、`String[]` 等不同数组类型时
- 调用方式：直接传入“数组对象”；可为 `null`

```java
Object arr = new long[]{10L, 20L};
int len = ArrayUtils.getLength(arr);
```

### isEmpty(Object[] array) / notEmpty(Object[] array)

- 仅适用于“对象数组”（`Object[]`），例如 `String[]`、`Integer[]`

```java
String[] names = null;
if (ArrayUtils.isEmpty(names)) {
    // 处理空数组
}
```
