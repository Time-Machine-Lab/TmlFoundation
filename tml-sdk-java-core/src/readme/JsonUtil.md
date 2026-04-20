# JsonUtil JSON工具类使用说明

## 概述

`JsonUtil` 是一个基于fastjson2封装的JSON工具类，提供对象与JSON字符串之间的相互转换功能。该工具类设计简洁，支持各种Java对象的序列化与反序列化。

## 特性

- ✅ **简单易用**：提供简洁的API接口
- ✅ **高性能**：基于fastjson2，性能优异
- ✅ **类型安全**：支持泛型，编译时类型检查
- ✅ **零依赖**：仅依赖fastjson2库
- ✅ **功能完整**：支持对象转JSON、JSON转对象
- ✅ **异常处理**：完善的异常处理机制

## 快速开始

### 基本用法

```java
import io.github.timemachinelab.util.JsonUtil;

// 对象转JSON字符串
User user = new User("张三", 25);
String jsonString = JsonUtil.toJson(user);
System.out.println("JSON字符串: " + jsonString);

// JSON字符串转对象
String json = "{\"name\":\"李四\",\"age\":30}";
User user2 = JsonUtil.fromJson(json, User.class);
System.out.println("用户名: " + user2.getName());
```

### 处理集合类型

```java
// List转JSON
List<String> list = Arrays.asList("apple", "banana", "orange");
String listJson = JsonUtil.toJson(list);
System.out.println("List JSON: " + listJson);

// JSON转List
List<String> fruits = JsonUtil.fromJson(listJson, new TypeReference<List<String>>(){});
System.out.println("水果列表: " + fruits);
```

## API列表

| 方法签名 | 描述 | 返回值 |
|---------|------|--------|
| `toJson(Object object)` | 将对象转换为JSON字符串 | `String` |
| `fromJson(String json, Class<T> clazz)` | 将JSON字符串转换为指定类型的对象 | `T` |
| `fromJson(String json, TypeReference<T> typeReference)` | 将JSON字符串转换为指定类型的对象（支持泛型） | `T` |

## API使用与介绍

### 对象转JSON

```java
// 简单对象
User user = new User("王五", 28);
String json = JsonUtil.toJson(user);

// null值处理
String nullJson = JsonUtil.toJson(null);  // 返回 "null"

// 数组和集合
int[] numbers = {1, 2, 3, 4, 5};
String arrayJson = JsonUtil.toJson(numbers);  // 返回 "[1,2,3,4,5]"
```

### JSON转对象

```java
// 基本类型转换
String userJson = "{\"name\":\"赵六\",\"age\":35}";
User user = JsonUtil.fromJson(userJson, User.class);

// 集合类型转换（使用TypeReference）
String listJson = "[\"Java\",\"Python\",\"JavaScript\"]";
List<String> languages = JsonUtil.fromJson(listJson, new TypeReference<List<String>>(){});

// Map类型转换
String mapJson = "{\"key1\":\"value1\",\"key2\":\"value2\"}";
Map<String, String> map = JsonUtil.fromJson(mapJson, new TypeReference<Map<String, String>>(){});
```

### 参数说明

- **object**: 要转换为JSON的对象，可以为null
- **json**: JSON格式的字符串
- **clazz**: 目标对象的Class类型
- **typeReference**: 用于处理泛型类型的TypeReference对象

### 使用示例

```java
// 完整示例
public class JsonExample {
    public static void main(String[] args) {
        // 创建测试对象
        Person person = new Person();
        person.setName("张三");
        person.setAge(30);
        person.setEmail("zhangsan@example.com");
        
        // 对象转JSON
        String json = JsonUtil.toJson(person);
        System.out.println("序列化结果: " + json);
        
        // JSON转对象
        Person restoredPerson = JsonUtil.fromJson(json, Person.class);
        System.out.println("反序列化结果: " + restoredPerson.getName());
        
        // 处理复杂对象
        Map<String, Object> complexData = new HashMap<>();
        complexData.put("users", Arrays.asList(person));
        complexData.put("count", 1);
        complexData.put("success", true);
        
        String complexJson = JsonUtil.toJson(complexData);
        System.out.println("复杂对象: " + complexJson);
    }
}
```