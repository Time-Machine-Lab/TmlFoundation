# 📖 ReflectX 极速上手指南：从入门到大神

欢迎使用 **ReflectX**。
你是不是经常为了获取一个深层嵌套的数据，被迫写出如下繁琐的防御性代码？

```java
// 🤮 传统噩梦：为了防空指针，写了一堆废话
if (order != null && order.getUser() != null && order.getUser().getAddress() != null) {
        return order.getUser().getAddress().getCity();
}

```

**ReflectX 的出现，就是为了消灭这种代码。** 它可以像手术刀一样，精准、优雅地操作复杂的对象结构，让你的代码重新变得清爽。

---

## 🎮 第一关：新手村 (基础读写)

我们先从最简单的 POJO 对象开始。不需要显示调用 getter/setter 方法，直接通过属性名操作。

**目标**：修改用户名，并读取它。

```java
import io.github.reflectx.SystemMetaObject;
import io.github.reflectx.MetaObject;

// 1. 准备一个普通对象
User user = new User(); 
user.setName("Guest");

// ✨ 2. 召唤“上帝之手” (获取 MetaObject)
// 这是框架的唯一入口
MetaObject meta = SystemMetaObject.forObject(user);

// 3. 写值：不用调用 setter，直接通过属性名赋值
meta.setValue("name", "ReflectX Master");

// 4. 读值：不用调用 getter，直接拿
Object name = meta.getValue("name"); 

System.out.println("玩家名称: " + name);
// 输出: ReflectX Master

```

> **📝 小白笔记**：
> `SystemMetaObject.forObject(obj)` 是开启反射操作的钥匙。只要拿到了 `MetaObject`，你就掌控了这个对象的一切。

---

## 🎮 第二关：进阶之路 (深层穿透)

现实中的对象往往是“套娃”（嵌套）的：比如 `Order` 里有 `User`，`User` 里有 `Address`。
以前你要一层层 `get()`，现在只需要用 **点号 `.**` 连接路径。

**目标**：直接修改三层深度的城市名。

```java
Order order = new Order();
// 假设这里已经初始化好了中间对象
order.setUser(new User());
        order.getUser().setAddress(new Address());

MetaObject meta = SystemMetaObject.forObject(order);

// 🚀 穿透三层结构，直接操作！
// 路径解析：order -> user -> address -> city
meta.setValue("user.address.city", "Shanghai");

String city = (String) meta.getValue("user.address.city");
System.out.println("城市: " + city);
// 输出: Shanghai

```

> **🔍 原理揭秘**：
> 框架内部的“分词器”会自动把 `user.address.city` 切割，像贪吃蛇一样一层层钻进去找到目标属性。

---

## 🎮 第三关：高手试炼 (List 与 Map 混合双打)

如果是 `List`（列表）、数组或者 `Map`（键值对）怎么办？
别担心，**ReflectX** 能够自动识别！无需关心底层差异，语法完全统一：

* **List / 数组**：使用 `[0]`、`[1]` 下标访问。
* **Map**：直接使用 `key` 名字访问。

**场景**：一个超级复杂的混合结构。
`User` 有一个 `Map` 叫 `attributes`，里面存了个 `List` 叫 `scores`。

```java
// 结构：User -> Map<String, Object> attributes -> List<Integer> scores
User user = new User();
user.getAttributes().put("scores", Arrays.asList(90, 100, 85));

MetaObject meta = SystemMetaObject.forObject(user);

// 🎯 混合操作：既有点号，又有中括号
// 翻译：找 attributes 里的 scores，取第 1 个元素 (即 100)
Object score = meta.getValue("attributes[scores].[1]");

System.out.println("得分: " + score);
// 输出: 100

```

> **📝 小白笔记**：
> 无论底层是 JavaBean、Map 还是 List，ReflectX 都会自动给它穿上一层“宇航服”（Wrapper），对外暴露统一的操作接口。

---

## 🎮 第四关：上帝模式 (自动修路)

这是本框架 **最强** 的功能！
假设你有一个刚 `new` 出来的空对象，里面全是 `null`。你想直接给最深层的属性赋值。

* **普通代码**：直接报 `NullPointerException` (空指针异常) 💥。
* **ReflectX**：自动帮你把路修好！👷

```java
// 1. 一个完全空白的订单，user 是 null，address 更是 null
Order emptyOrder = new Order();
MetaObject meta = SystemMetaObject.forObject(emptyOrder);

// 2. 见证奇迹的时刻
// 框架发现 user 是 null，自动 new User()
// 发现 address 是 null，自动 new Address()
// 最后赋值 city
meta.setValue("user.address.city", "Beijing");

// 3. 验证一下，对象真的被创建了吗？
System.out.println(emptyOrder.getUser() != null); // true
        System.out.println(emptyOrder.getUser().getAddress().getCity()); // Beijing

```

- 相关的测试类路径: src/test/java/test/util/reflectx/ReflectXQuickStartTest.java

> **💡 核心机制**：
> 当 `setValue` 发现路径断了（遇到 `null`），它会利用元数据探测属性类型（比如 `Address` 类），自动调用工厂帮你 `new` 一个实例填坑。这在处理数据库查询结果映射或 JSON 解析时是神器。

---

## 🏆 总结：为什么选择 ReflectX？

| 你的痛点 😫 | ReflectX 的解法 😎 |
| --- | --- |
| 代码里全是 `if (obj != null)` | **自动修路**，一行代码直达深处，无需判空 |
| 分不清 Map 用 `get` 还是 List 用 `get` | **统一语法**，全都是 `a.b[0]`，屏蔽底层差异 |
| 反射代码全是 `try-catch` 异常 | **零废话**，一个 Runtime 异常处理所有底层报错 |
| 担心反射性能 | **高性能**，底层自动缓存类结构（Reflector），拒绝重复解析 |

---