# CollectionUtils 集合工具类使用说明

## 概述

`CollectionUtils` 是一个功能丰富的集合工具类，提供集合操作的常用工具方法。该工具类专注于集合的空值安全操作、数组转换、深度比较等功能，简化了Java集合的常规操作。

## 特性

- ✅ **空值安全**：所有方法都进行了空值检查，避免NullPointerException
- ✅ **数组支持**：深度数组比较和数组类型识别
- ✅ **类型安全**：使用泛型确保类型安全
- ✅ **性能优化**：高效的集合操作实现
- ✅ **零依赖**：仅依赖JDK标准库
- ✅ **线程安全**：所有方法都是线程安全的

## 快速开始

### 基本用法

```java
import io.github.timemachinelab.util.CollectionUtils;

// 检查集合是否为空
List<String> list = new ArrayList<>();
boolean isEmpty = CollectionUtils.isEmpty(list); // true

// 数组转列表
String[] array = {"a", "b", "c"};
List<String> list = CollectionUtils.arrayToList(array); // ["a", "b", "c"]

// 合并数组到集合
Integer[] numbers = {1, 2, 3};
Set<Integer> set = new HashSet<>();
CollectionUtils.mergeArrayIntoCollection(numbers, set); // set now contains [1, 2, 3]
```

## API列表

| 方法签名 | 描述 | 返回值 |
|---------|------|--------|
| `isEmpty(Collection<?>)` | 检查集合是否为空或null | `boolean` |
| `isEmpty(Map<?, ?>)` | 检查Map是否为空或null | `boolean` |
| `arrayToList(T[])` | 将数组转换为列表 | `List<T>` |
| `mergeArrayIntoCollection(E[], Collection<E>)` | 将数组元素合并到集合中 | `void` |
| `arrayEquals(Object, Object)` | 深度比较两个数组是否相等 | `boolean` |
| `contains(Iterator<E>, E)` | 检查迭代器中是否包含指定元素 | `boolean` |
| `contains(Collection<E>, E)` | 检查集合中是否包含指定元素 | `boolean` |
| `containsInstance(Collection<?>, Object)` | 检查集合中是否包含指定实例 | `boolean` |

## API使用与介绍

### 空值检查

```java
// 检查集合
List<String> list = null;
if (CollectionUtils.isEmpty(list)) {
    System.out.println("集合为空或null");
}

// 检查Map
Map<String, Object> map = new HashMap<>();
if (CollectionUtils.isEmpty(map)) {
    System.out.println("Map为空或null");
}
```

### 数组与集合转换

```java
// 数组转列表（null安全）
String[] array = {"hello", "world"};
List<String> list = CollectionUtils.arrayToList(array); // ["hello", "world"]

// null数组处理
String[] nullArray = null;
List<String> emptyList = CollectionUtils.arrayToList(nullArray); // []

// 合并数组到现有集合
Integer[] numbers = {1, 2, 3};
List<Integer> numberList = new ArrayList<>();
CollectionUtils.mergeArrayIntoCollection(numbers, numberList);
// numberList now contains [1, 2, 3]
```

### 深度数组比较

```java
// 基本类型数组比较
int[] arr1 = {1, 2, 3};
int[] arr2 = {1, 2, 3};
int[] arr3 = {1, 2, 4};

boolean result1 = CollectionUtils.arrayEquals(arr1, arr2); // true
boolean result2 = CollectionUtils.arrayEquals(arr1, arr3); // false

// 对象数组比较
String[] strArr1 = {"a", "b"};
String[] strArr2 = {"a", "b"};
boolean result3 = CollectionUtils.arrayEquals(strArr1, strArr2); // true

// 非数组对象比较（返回false）
boolean result4 = CollectionUtils.arrayEquals("string", "string"); // false
```

### 元素查找

```java
// 在集合中查找元素
List<String> names = Arrays.asList("Alice", "Bob", "Charlie");
boolean hasBob = CollectionUtils.contains(names, "Bob"); // true

// 在迭代器中查找元素
Iterator<Integer> iterator = Arrays.asList(1, 2, 3).iterator();
boolean hasTwo = CollectionUtils.contains(iterator, 2); // true

// 查找数组元素（特殊处理）
List<Object> mixedList = Arrays.asList(new int[]{1, 2}, "string");
boolean hasArray = CollectionUtils.contains(mixedList, new int[]{1, 2}); // true

// 实例查找（使用==比较）
Object obj = new Object();
List<Object> objects = Arrays.asList(obj, new Object());
boolean hasInstance = CollectionUtils.containsInstance(objects, obj); // true
```

## 使用注意事项

1. **空值处理**：所有方法都对null输入进行了安全处理
2. **数组比较**：`arrayEquals`方法只比较数组类型，非数组对象总是返回false
3. **元素比较**：`contains`方法对数组元素有特殊处理，使用深度比较
4. **实例比较**：`containsInstance`使用==进行引用比较，而不是equals方法

## 性能说明

- 所有方法都是O(n)时间复杂度，其中n是集合或数组的大小
- `arrayEquals`方法对基本类型数组使用专门的比较方法，性能优化
- `mergeArrayIntoCollection`方法避免了不必要的中间对象创建