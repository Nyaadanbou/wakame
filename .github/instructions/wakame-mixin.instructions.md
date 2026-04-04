---
applyTo: "wakame-mixin/src/**/*.kt"
---

# wakame-mixin 编码指南

wakame-mixin 是 Koish 的核心抽象层，定义接口和数据结构，**不包含插件运行时逻辑**。

## 可以做

- 定义 `sealed interface` / `enum class` / `data class` (如 `CastableTrigger`, `Castable`)
- 定义 `interface` (如 `ItemBehavior`, `SimpleInteract`, `OnlineUserTicker`, `SequenceComboHandler`)
- 定义 `object` 实现 `ItemBehavior` (如 `Castable : SimpleInteract`)
- 在 `ItemPropTypes` / `ItemBehaviorTypes` 中注册新类型
- 使用 `@ConfigSerializable` 标记数据类

## 不能做

- 读取配置文件 (`MAIN_CONFIG`)
- 注册事件监听器 (`Listener`)
- 使用 `@Init` 注解
- 直接依赖 wakame-plugin 的类

## 委托到 wakame-plugin 的模式

如果 wakame-mixin 的代码需要调用 wakame-plugin 的逻辑:
1. 在 wakame-mixin 中定义 `fun interface`
2. 在调用方持有 `var handler: MyHandler? = null`
3. wakame-plugin 在初始化时注入实现
