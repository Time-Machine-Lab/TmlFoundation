<div align="center">

<img src="docs/logo.png" alt="TF4J Logo" width="80" height="80" />
<h1 style="margin-top: -10px">TF4J</h1>

<p>Tml-Foundation For Java · TML SDK</p>

[![Java](https://img.shields.io/badge/Java-11-007396?logo=openjdk&logoColor=white)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.timemachinelab/tml-sdk-java-bom?label=Maven%20Central)](https://search.maven.org/artifact/io.github.timemachinelab/tml-sdk-java-bom)
[![License](https://img.shields.io/github/license/Time-Machine-Lab/TmlFoundation)](https://www.apache.org/licenses/LICENSE-2.0)
[![Release to Maven Central](https://github.com/Time-Machine-Lab/TmlFoundation/actions/workflows/PushSDK.yml/badge.svg)](https://github.com/Time-Machine-Lab/TmlFoundation/actions/workflows/PushSDK.yml)
[![Stars](https://img.shields.io/github/stars/Time-Machine-Lab/TmlFoundation?style=social)](https://github.com/Time-Machine-Lab/TmlFoundation/stargazers)

</div>

## 🧱 项目定位

TML-Foundation（简称 TF4J）是团队的 TML SDK：一个面向企业级工程实践的“底层基建聚合层”。

它汇聚团队沉淀的高质量、高可用、高性能的通用能力（代码、工具、框架与设计），为不同业务线与组件提供可复用的最底层能力，让 Team + Agent 在开发时可以直接复用已有 CUFD（Code + Utility + Framework + Design），把精力集中在业务本身。

TF4J 聚焦 Java / JVM 生态，同时为 Web、Go、Python 等侧的基础设施提供可对齐的规范与实现接口，便于跨技术栈复用与统一演进。

## 🧭 导航

- [模块一览](#modules)
- [快速开始](#quickstart)
- [Star 走势](#stars)

## ✨ 你能用它做什么

- 统一依赖与版本对齐：通过 BOM 管理团队通用依赖与内部模块版本
- 提供高复用的基础能力：工具类、算法与数据结构、时间处理、并发与定时模型等
- 提供 Web 与微服务的统一响应模型：Result 与自动包装能力，让接口输出更稳定一致
- 以模块化方式沉淀：按需引入，不强绑定业务框架，逐步演进

## 🎯 适用场景

- 团队级基础设施：沉淀可复用通用能力，减少重复造轮子
- 统一研发体验：统一返回模型、错误表达、依赖版本与工程约定
- 微服务与 API 工程：更稳定的接口语义与更低的接入成本

<a name="modules"></a>

## 📦 模块一览

| 模块 | 说明 |
| --- | --- |
| `tml-sdk-java-bom` | 依赖版本对齐与模块版本管理（推荐优先引入） |
| `tml-sdk-java-core` | 通用工具与基础能力（字符串、断言、时间、算法/数据结构、时间轮等） |
| `tml-sdk-java-web` | Web 常量与基础协议抽象（如 HTTP 状态码等） |
| `tml-sdk-spring-boot-autoconfigure` | Spring Boot 自动装配基础（供 Starter 使用） |
| `tml-sdk-spring-boot-starter-web` | 面向 Web 的开箱即用能力（Result、@AutoResp 等） |
| `tml-sdk-java-cache-api` | Cache 能力接口层（面向扩展） |
| `tml-sdk-java-net-api` | 网络能力接口层（面向扩展） |
| `tml-sdk-java-fucking-bug` | 问题复现与回归验证模块（用于沉淀边界与反例） |

<a name="quickstart"></a>

## 🚀 快速开始

### 1) 引入 BOM（推荐）

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>io.github.timemachinelab</groupId>
      <artifactId>tml-sdk-java-bom</artifactId>
      <version>1.1.0-alpha.5</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

### 2) 按需引入模块

```xml
<dependencies>
  <dependency>
    <groupId>io.github.timemachinelab</groupId>
    <artifactId>tml-sdk-java-core</artifactId>
  </dependency>
</dependencies>
```


### 2) 延伸阅读

- Result 设计与用法：[`Result.md`](./tml-sdk-spring-boot-starter-web/src/readme/Result.md)
- Core 工具文档：[`tml-sdk-java-core/src/readme`](./tml-sdk-java-core/src/readme)

<a name="stars"></a>

## ⭐ Star 走势

[
  ![Star History Chart](https://api.star-history.com/svg?repos=Time-Machine-Lab/TmlFoundation&type=Date)
](https://star-history.com/#Time-Machine-Lab/TmlFoundation&Date)

## 🤝 贡献方式

- 提交 Issue：描述场景、预期行为与复现步骤（越可复现越容易被合入）
- 提交 PR：保持模块边界清晰、单点改动可验证，尽量附带测试

## 📄 License

Apache-2.0
