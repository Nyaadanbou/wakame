---
applyTo: "wakame-mixin/src/**/*.kt"
---

# wakame-mixin 编码指南

wakame-mixin 包含 NMS Mixin 补丁 (Java) 和 Bridge 接口 (Kotlin)，通过 Horizon + Weaver 在服务器启动时加载。**不包含插件运行时逻辑。**

## 模块结构

|目录|内容|
|---|---|
|`bridge/`|Bridge 接口与工具类 — 封装 NMS 交互，暴露给 wakame-plugin 使用|
|`mixin/`|Java Mixin 类 — 修改 NMS 行为 (Invoker、Mixin 注入)|
|`util/`|通用工具|

## 可以做

- 定义 Bridge `interface` / `object` (如 `KoishItemBridge`, `DamageManagerBridge`)
- 编写 Java Mixin 类 (`@Mixin`, `@Inject`, `@Invoker`)
- 定义 `data class` / `sealed interface` / `enum class` 作为 Bridge 数据结构
- 定义 NMS 相关的 typealias 和扩展函数
- 使用 `@ConfigSerializable` 标记数据类
- 定义 Codec / 注册 DataComponent

## 不能做

- 读取配置文件 (`MAIN_CONFIG`)
- 注册事件监听器 (`Listener`)
- 使用 `@Init` 注解
- 直接依赖 wakame-plugin 的类
- 实现游戏逻辑 (物品行为、技能、伤害计算等)

## 委托到 wakame-plugin 的模式

如果 wakame-mixin 的代码需要调用 wakame-plugin 的逻辑:
1. 在 wakame-mixin 中定义 `fun interface`
2. 在调用方持有 `var handler: MyHandler? = null`
3. wakame-plugin 在初始化时注入实现
