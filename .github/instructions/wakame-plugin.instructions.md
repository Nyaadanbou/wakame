---
applyTo: "wakame-plugin/src/**/*.kt"
---

# wakame-plugin 编码指南

wakame-plugin 是 Koish 的运行时实现层，包含所有 Feature 逻辑、事件监听、配置读取和 tick 系统。

## Feature 类的标准结构

```kotlin
@Init(InitStage.POST_WORLD)
object MyFeature : Listener {
    // 配置读取 (使用 @ConfigSerializable data class)
    private val config: MyConfig by MAIN_CONFIG.entryOrElse(MyConfig(), "config_node_path")

    @InitFun
    fun init() { /* 注册逻辑 */ }

    // 事件处理...
}
```

## 配置文件规范

- 配置数据类放在功能对应的文件中 (与 Feature object 同文件)
- 标记 `@ConfigSerializable`，构造参数提供默认值
- Kotlin `camelCase` 属性 ↔ YAML `snake_case` 键 (自动映射)
- 重复字段封装为嵌套 data class
- 使用 `MAIN_CONFIG.entryOrElse(default, "path")` 一次性读取

## OnlineUserTicker 注册

在 `ServerOnlineUserTicker.kt` 的 `on(ServerTickStartEvent)` 方法中添加调用:
```kotlin
MyFeature.onTickUser(user, player)
```

## MetadataMap 使用

每个功能的 MetadataKey 应定义在状态类的 companion object 中:
```kotlin
class MyState {
    companion object {
        val METADATA_KEY: MetadataKey<MyState> = metadataKey("feature:my_state")
    }
}
```

## 导入别名约定

当类名冲突时必须使用 `import ... as`:
```kotlin
import cc.mewcraft.wakame.item.behavior.impl.Castable as CastableBehavior
import cc.mewcraft.wakame.item.property.impl.Castable as CastableProp
import org.bukkit.Sound as BukkitSound
```
