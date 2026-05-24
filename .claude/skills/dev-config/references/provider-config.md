# 范式 A: Provider 响应式配置

适用于单例配置项——某个功能只需从配置文件中读取少量值。配置重载时值自动更新，无需手动处理。

## 选择配置源

项目预定义了几个常用的配置源:

```kotlin
import cc.mewcraft.lazyconfig.MAIN_CONFIG           // configs/config.yml
import cc.mewcraft.wakame.feature.FEATURE_CONFIG     // configs/features.yml
```

也可以为独立的配置文件创建自己的源:

```kotlin
import cc.mewcraft.lazyconfig.access.ConfigAccess

private val MY_CONFIG = ConfigAccess["my_module"]    // configs/my_module.yml
```

## 读取配置项

核心 API 是 `Provider` 上的扩展函数，配合 Kotlin `by` 委托使用:

```kotlin
import cc.mewcraft.lazyconfig.access.entryOrElse
import cc.mewcraft.lazyconfig.access.optionalEntry
import xyz.xenondevs.commons.provider.orElse
import xyz.xenondevs.commons.provider.map
```

**`entryOrElse` — 带默认值的必选项（最常用）:**

```kotlin
// 基本类型
private val enabled: Boolean by MY_CONFIG.entryOrElse(false, "enabled")

// 枚举类型
private val type: DatabaseType by MY_CONFIG.entryOrElse<DatabaseType>(DatabaseType.SQLITE, "type")

// @ConfigSerializable data class
private val config: MyConfig by MY_CONFIG.entryOrElse(MyConfig(), "my_feature")

// 集合类型
private val worlds: Set<Key> by MY_CONFIG.entryOrElse<Set<Key>>(setOf(), "disabled_worlds")
```

**`optionalEntry` + `orElse` — 可选项（缺失时用默认值，不报错）:**

```kotlin
// 适合 debug 开关等可选配置
private val LOGGING by MAIN_CONFIG.optionalEntry<Boolean>("debug", "logging", "damage").orElse(false)
```

**`entry` — 必选项（缺失时抛异常）:**

```kotlin
// 配置文件中必须存在该项，否则启动报错
private val formula: String by MY_CONFIG.entry("formula")
```

## 嵌套路径

每级路径用一个 vararg 参数表示:

```kotlin
// 对应 YAML: debug.logging.damage
private val LOGGING by MAIN_CONFIG.optionalEntry<Boolean>("debug", "logging", "damage").orElse(false)

// 对应 YAML: equipment.amount_per_damage
private val EQUIPMENT_CONFIG = DAMAGE_CONFIG.node("equipment")
private val AMOUNT by EQUIPMENT_CONFIG.optionalEntry<Double>("amount_per_damage").orElse(0.25)
```

## Provider 链式变换

`Provider` 支持 `map` 等操作，可以对读取到的原始值进行变换:

```kotlin
// 读取 tick 数并转换为毫秒
private val COOLDOWN_MS by MY_CONFIG.optionalEntry<Long>("cooldown_ticks").orElse(5L).map { it * 50L }
```

## 子节点 Provider

使用 `node()` 从一个 Provider 中派生子节点 Provider:

```kotlin
import cc.mewcraft.lazyconfig.access.node

private val DAMAGE_CONFIG = ConfigAccess["damage/config"]
private val RULES_CONFIG = DAMAGE_CONFIG.node("rules")        // 指向 rules 子节点
private val EQUIPMENT_CONFIG = DAMAGE_CONFIG.node("equipment") // 指向 equipment 子节点

// 然后在子节点上继续读取
private val LEAST_DAMAGE: Double by RULES_CONFIG.entry("least_damage")
```

## strong vs weak Provider

- `entry` / `optionalEntry` / `entryOrElse` / `node` — 返回 **weak** Provider（弱引用，无强引用持有时可被 GC）
- `strongEntry` / `strongOptionalEntry` / `strongEntryOrElse` / `strongNode` — 返回 **strong** Provider（始终持有）

**经验法则**: 大多数场景使用默认的 weak 版本即可。只有当 Provider 没有被任何字段持有、但仍需长期存活时，才用 strong 版本。
