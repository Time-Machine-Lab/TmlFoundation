# HttpUtils HTTP工具类使用说明

## 概述

`HttpUtils` 是一个基于OkHttp封装的HTTP工具类，提供同步的HTTP请求发送功能。该工具类支持GET、POST、PUT、DELETE等常用HTTP方法，同时提供代理配置和自定义请求头功能。

## 特性

- ✅ **同步调用**：所有方法都是同步的，使用简单直观
- ✅ **多方法支持**：支持GET、POST、PUT、DELETE等HTTP方法
- ✅ **代理支持**：内置代理配置功能，支持HTTP代理
- ✅ **超时配置**：默认60秒超时，可自定义配置
- ✅ **重定向支持**：自动处理HTTP重定向
- ✅ **异常处理**：完善的异常处理机制
- ✅ **高性能**：基于OkHttp，性能优异

## 快速开始

### 基本用法

```java
import io.github.timemachinelab.util.HttpUtils;

// 简单GET请求
String response = HttpUtils.get("https://api.example.com/data");
System.out.println("响应内容: " + response);

// POST请求发送JSON数据
String jsonBody = "{\"name\":\"张三\",\"age\":25}";
String postResponse = HttpUtils.post("https://api.example.com/users", jsonBody);
System.out.println("POST响应: " + postResponse);
```

### 使用代理

```java
// 配置代理
HttpUtils.setProxyConfig(new HttpUtils.ProxyConfig("127.0.0.1", 8080));

// 发送带代理的请求
String response = HttpUtils.get("https://api.example.com/data");

// 清除代理配置
HttpUtils.clearProxyConfig();
```

### 自定义请求头

```java
// 创建自定义请求头
Map<String, String> headers = new HashMap<>();
headers.put("Authorization", "Bearer your-token");
headers.put("Content-Type", "application/json");

// 发送带自定义头的请求
String response = HttpUtils.get("https://api.example.com/protected", headers);
```

## API列表

| 方法签名 | 描述 | 返回值 |
|---------|------|--------|
| `get(String url)` | 发送GET请求 | `String` |
| `get(String url, Map<String, String> headers)` | 发送带自定义头的GET请求 | `String` |
| `post(String url, String body)` | 发送POST请求 | `String` |
| `post(String url, String body, Map<String, String> headers)` | 发送带自定义头的POST请求 | `String` |
| `put(String url, String body)` | 发送PUT请求 | `String` |
| `put(String url, String body, Map<String, String> headers)` | 发送带自定义头的PUT请求 | `String` |
| `delete(String url)` | 发送DELETE请求 | `String` |
| `delete(String url, Map<String, String> headers)` | 发送带自定义头的DELETE请求 | `String` |
| `setProxyConfig(ProxyConfig config)` | 设置代理配置 | `void` |
| `clearProxyConfig()` | 清除代理配置 | `void` |

## API使用与介绍

### 代理配置

```java
// 代理配置类
public static class ProxyConfig {
    private String host;    // 代理主机地址
    private int port;       // 代理端口号
    private String username;  // 代理用户名（可选）
    private String password;  // 代理密码（可选）
    
    // 构造方法
    public ProxyConfig(String host, int port)
    public ProxyConfig(String host, int port, String username, String password)
}

// 设置代理
HttpUtils.ProxyConfig proxyConfig = new HttpUtils.ProxyConfig("proxy.example.com", 8080);
HttpUtils.setProxyConfig(proxyConfig);

// 设置带认证的代理
HttpUtils.ProxyConfig authProxy = new HttpUtils.ProxyConfig("proxy.example.com", 8080, "user", "pass");
HttpUtils.setProxyConfig(authProxy);
```

### 参数说明

- **url**: 请求的URL地址
- **body**: 请求体内容，通常为JSON格式
- **headers**: 自定义请求头，Map类型
- **config**: 代理配置对象

### 使用示例

```java
// 完整示例
public class HttpExample {
    public static void main(String[] args) {
        try {
            // 1. 简单GET请求
            String getResponse = HttpUtils.get("https://jsonplaceholder.typicode.com/posts/1");
            System.out.println("GET响应: " + getResponse);
            
            // 2. POST请求发送JSON数据
            String jsonBody = "{\"title\":\"foo\",\"body\":\"bar\",\"userId\":1}";
            String postResponse = HttpUtils.post("https://jsonplaceholder.typicode.com/posts", jsonBody);
            System.out.println("POST响应: " + postResponse);
            
            // 3. 带自定义头的请求
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer your-jwt-token");
            headers.put("X-Custom-Header", "custom-value");
            
            String authResponse = HttpUtils.get("https://api.example.com/protected", headers);
            System.out.println("认证响应: " + authResponse);
            
            // 4. 使用代理
            HttpUtils.ProxyConfig proxyConfig = new HttpUtils.ProxyConfig("127.0.0.1", 8080);
            HttpUtils.setProxyConfig(proxyConfig);
            
            String proxyResponse = HttpUtils.get("https://api.example.com/data");
            System.out.println("代理响应: " + proxyResponse);
            
            // 清除代理
            HttpUtils.clearProxyConfig();
            
            // 5. PUT请求
            String putBody = "{\"id\":1,\"title\":\"updated title\"}";
            String putResponse = HttpUtils.put("https://jsonplaceholder.typicode.com/posts/1", putBody);
            System.out.println("PUT响应: " + putResponse);
            
            // 6. DELETE请求
            String deleteResponse = HttpUtils.delete("https://jsonplaceholder.typicode.com/posts/1");
            System.out.println("DELETE响应: " + deleteResponse);
            
        } catch (IOException e) {
            System.err.println("HTTP请求失败: " + e.getMessage());
        }
    }
}
```

### 异常处理

```java
try {
    String response = HttpUtils.get("https://api.example.com/data");
    System.out.println("请求成功: " + response);
} catch (IOException e) {
    // 网络连接失败、超时、服务器错误等情况
    System.err.println("请求失败: " + e.getMessage());
}
```