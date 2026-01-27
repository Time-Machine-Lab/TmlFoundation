# StringUtils 字符串工具类使用说明

## 概述

`StringUtils` 提供常用的字符串/字符序列判断与处理能力，行为与 Apache Commons Lang 同名方法保持一致（本项目内自实现）。

适用于输入校验、日志字段规范化、协议解析等场景。

## 快速开始

```java
import io.github.timemachinelab.util.StringUtils;

boolean blank = StringUtils.isBlank("  ");      // true
boolean empty = StringUtils.isEmpty("");        // true
boolean notBlank = StringUtils.isNotBlank("a"); // true

String lower = StringUtils.lowerCase("AbC");    // "abc"
```

## 核心语义

- `isEmpty(cs)`：`cs == null` 或长度为 0
- `isBlank(cs)`：`cs == null`、长度为 0、或全部为空白字符（`Character.isWhitespace`）

## API 列表

| 方法签名 | 说明 | 返回值 |
|---|---|---|
| `int length(CharSequence cs)` | 获取长度；`null` 返回 0 | `int` |
| `String lowerCase(String str)` | 转小写；`null` 返回 `null` | `String` |
| `boolean isBlank(CharSequence cs)` | `null`/空/全空白判断 | `boolean` |
| `boolean isEmpty(CharSequence cs)` | `null`/空字符串判断 | `boolean` |
| `boolean isAnyBlank(CharSequence... css)` | 任意一个为 blank 则 `true` | `boolean` |
| `boolean isAnyEmpty(CharSequence... css)` | 任意一个为 empty 则 `true` | `boolean` |
| `boolean isAllBlank(CharSequence... css)` | 全部为 blank 则 `true` | `boolean` |
| `boolean isAllEmpty(CharSequence... css)` | 全部为 empty 则 `true` | `boolean` |
| `boolean isNotBlank(CharSequence cs)` | `!isBlank(cs)` | `boolean` |
| `boolean isNotEmpty(CharSequence cs)` | `!isEmpty(cs)` | `boolean` |

## 方法使用说明

### length(CharSequence cs)

```java
int n1 = StringUtils.length(null);     // 0
int n2 = StringUtils.length("abc");   // 3
```

### lowerCase(String str)

```java
String a = StringUtils.lowerCase("AbC"); // "abc"
String b = StringUtils.lowerCase(null);  // null
```

### isBlank / isEmpty

```java
StringUtils.isEmpty(" "); // false
StringUtils.isBlank(" "); // true
```

### isAnyBlank / isAnyEmpty

- 入参为 `null` 或不传参数时：返回 `false`

```java
StringUtils.isAnyBlank("a", " "); // true
StringUtils.isAnyEmpty("a", "");  // true
```

### isAllBlank / isAllEmpty

- 入参为 `null` 或不传参数时：返回 `true`

```java
StringUtils.isAllBlank(" ", "\t"); // true
StringUtils.isAllEmpty("", null);   // true
```

### isNotBlank / isNotEmpty

```java
StringUtils.isNotBlank("bob"); // true
StringUtils.isNotEmpty(" ");   // true
```
