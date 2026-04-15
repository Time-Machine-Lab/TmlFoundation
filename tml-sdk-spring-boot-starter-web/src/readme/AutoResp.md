# AutoResp 注解使用指南

## 概述

AutoResp 是一个自动响应包装注解，标记在 Controller 类或方法上后，框架会自动将方法的返回值包装成标准的 `Result` 对象返回给客户端。

## 核心特性

- **自动包装**: 无需手动创建 Result 对象，框架自动完成包装
- **灵活配置**: 支持类级别和方法级别注解
- **异常处理**: 配合异常解析器统一处理异常响应
- **无缝集成**: Spring Boot 自动配置，开箱即用

## 快速开始

### 基本用法

```java
// 在Controller方法上使用
@RestController
public class UserController {

    @GetMapping("/users/{id}")
    @AutoResp  // 自动包装返回结果为Result
    public User getUser(@PathVariable Long id) {
        return userService.findById(id);
    }

    @PostMapping("/users")
    @AutoResp
    public User createUser(@RequestBody UserDTO userDTO) {
        return userService.create(userDTO);
    }
}
```

### 类级别注解

```java
// 整个Controller的所有方法都自动包装
@RestController
@AutoResp
public class OrderController {

    @GetMapping("/orders/{id}")
    public Order getOrder(@PathVariable Long id) {
        return orderService.findById(id);
    }

    @PostMapping("/orders")
    public Order createOrder(@RequestBody OrderDTO orderDTO) {
        return orderService.create(orderDTO);
    }
}
```

### 混合使用

```java
@RestController
@AutoResp  // 类级别注解：默认所有方法都自动包装
public class ProductController {

    @GetMapping("/products/{id}")
    public Product getProduct(@PathVariable Long id) {
        return productService.findById(id);
    }

    @GetMapping("/products/search")
    public List<Product> searchProducts(@RequestParam String keyword) {
        // 方法级别注解会覆盖类级别设置
        return productService.search(keyword);
    }
}
```

## 工作原理

1. `AutoRespAdvice` 拦截所有标记了 `@AutoResp` 的 Controller 方法
2. 在响应序列化前，将原始返回值自动包装成 `Result` 对象
3. 特殊处理 String 类型返回值，确保正确序列化为 JSON
4. 异常通过 `AutoRespExceptionResolver` 统一处理为 Result 格式

## 配置说明

### 引入依赖

确保项目中已引入 `tml-sdk-spring-boot-starter-web` 模块：

```xml
<dependency>
    <groupId>io.github.timemachinelab</groupId>
    <artifactId>tml-sdk-spring-boot-starter-web</artifactId>
    <version>${tml-sdk.version}</version>
</dependency>
```

### application.yml 配置

```yaml
tml:
  web:
    result:
      # 是否启用链路追踪
      trace-enabled: true
      # 时间戳精度（毫秒/秒）
      timestamp-precision: MILLIS
      # 时区
      timezone: Asia/Shanghai
```

### 配置属性说明

| 属性 | 说明 | 默认值 |
|------|------|--------|
| `trace-enabled` | 是否启用TraceId生成 | true |
| `timestamp-precision` | 时间戳精度（MILLIS/SECONDS） | MILLIS |
| `timezone` | 时区设置 | 系统默认 |

## 注意事项

1. **返回类型**: `@AutoResp` 作用于任何返回类型，框架会自动包装
2. **异常处理**: 被 `@AutoResp` 标记的方法抛出的异常会自动转换为错误 Result
3. **优先级**: 方法级别的注解优先级高于类级别
4. **String类型**: 特殊处理确保 String 返回值正确序列化为 JSON
5. **Result对象**: 如果方法已经返回 Result 对象，不会进行二次包装

## 常见场��

### 场景1：标准CRUD接口

```java
@RestController
@AutoResp
public class UserController {

    @GetMapping("/users/{id}")
    public User get(@PathVariable Long id) {
        return userService.getById(id);
    }

    @PostMapping("/users")
    public User create(@RequestBody UserDTO dto) {
        return userService.create(dto);
    }

    @PutMapping("/users/{id}")
    public User update(@PathVariable Long id, @RequestBody UserDTO dto) {
        return userService.update(id, dto);
    }

    @DeleteMapping("/users/{id}")
    public void delete(@PathVariable Long id) {
        userService.delete(id);
        // 返回void时，自动包装为成功响应
    }
}
```

### 场景2：部分接口需要特殊处理

```java
@RestController
@AutoResp
public class ReportController {

    @GetMapping("/reports/{id}")
    public Report getReport(@PathVariable Long id) {
        return reportService.getById(id);
    }

    @GetMapping("/reports/{id}/download")
    public void downloadReport(@PathVariable Long id, HttpServletResponse response) {
        // 不需要自动包装，直接输出文件流
        reportService.download(id, response);
    }
}
```

## 与其他注解的关系

- `@RestController`: 配合使用，`@AutoResp` 只对 `@RestController` 或 `@Controller` 生效
- `@ResponseBody`: `@AutoResp` 内部使用 `ResponseBodyAdvice` 实现，自动处理响应体
- `@ExceptionHandler`: 可以自定义异常处理，优先级高于 `AutoRespExceptionResolver`
