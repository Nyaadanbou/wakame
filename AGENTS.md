# Agent Instructions for Koish (Wakame)

本文档总结了 Koish 项目中关键的编码范式，供 AI Agent 在后续对话中直接参考，避免重复分析项目结构。

---

## 模块职责划分: wakame-mixin vs wakame-plugin

`wakame-mixin` 定义**接口和数据结构**，`wakame-plugin` 提供**实现和运行时逻辑**。

当 wakame-plugin 需要向 wakame-mixin 中的代码注入行为时，使用 **接口委托模式**：
- 在 `wakame-mixin` 中定义 `fun interface` 或普通 `interface`
- 在 `wakame-mixin` 的调用方持有一个 `var handler: MyHandler? = null`
- 在 `wakame-plugin` 中实现该接口，并在 `init {}` 块中注入: `CallerObject.handler = this`

**示例** (组合键序列):
```kotlin
// wakame-mixin: 定义接口
fun interface SequenceComboHandler {
    fun handleInput(player: Player, castableMap: Map<String, CastableProp>, input: GenericCastableTrigger)
}

// wakame-mixin: 调用方
object Castable : SimpleInteract {
    var sequenceComboHandler: SequenceComboHandler? = null
    // 在 handleSimpleUse/handleSimpleAttack 中调用:
    // sequenceComboHandler?.handleInput(player, castable, trigger)
}

// wakame-plugin: 实现并注入
@Init(InitStage.POST_WORLD)
object SequenceComboFeature : SequenceComboHandler {
    init { CastableBehavior.sequenceComboHandler = this }
    override fun handleInput(...) { /* 实现逻辑 */ }
}
```

---

## ItemProp 范式

`ItemProp` 是附加在物品类型上的静态数据，定义在 `wakame-mixin` 中。

### 定义新的 ItemProp 数据类型

在 `wakame-mixin/src/main/kotlin/cc/mewcraft/wakame/item/property/impl/` 下创建 `data class`，标记 `@ConfigSerializable`。

### 注册到 ItemPropTypes

在 `wakame-mixin/.../item/property/ItemPropTypes.kt` 中添加:
```kotlin
@JvmField
val MY_PROP: ItemPropType<MyPropData> = typeOf("my_prop") {
    serializers {
        // 如果需要自定义序列化器，在此注册
        register(MyCustomSerializer())
    }
}
```

### 读取 ItemProp

```kotlin
val propValue = itemStack.getProp(ItemPropTypes.MY_PROP) ?: return
```

---

## ItemBehavior 范式

`ItemBehavior` 描述物品与世界交互的逻辑，不含数据，定义在 `wakame-mixin` 中。

### 定义新的 ItemBehavior

- 在 `wakame-mixin/.../item/behavior/impl/` 下创建 `object`，实现 `SimpleInteract`（或 `ItemBehavior`）。
- `SimpleInteract` 提供 `handleSimpleUse` (右键) / `handleSimpleAttack` (左键) / `handleConsume` (消耗) 的统一入口。
- 在 `ItemBehaviorTypes` 中注册:
  ```kotlin
  @JvmField
  val MY_BEHAVIOR = typeOf("my_behavior", MyBehavior)
  ```

### ItemBehavior 与 ItemProp 的关系

一个 ItemBehavior 通常读取一个或多个 ItemProp 来获取配置数据:
```kotlin
object MyBehavior : SimpleInteract {
    override fun handleSimpleUse(context: UseContext): InteractionResult {
        val prop = context.itemstack.getProp(ItemPropTypes.MY_PROP) ?: return InteractionResult.PASS
        // 使用 prop 中的数据执行逻辑
        return InteractionResult.PASS
    }
}
```

---

## 配置文件 (@ConfigSerializable) 范式

本项目使用 **Configurate** 的 ObjectMapper 将 YAML 自动映射到 Kotlin data class。

### 核心规则

- 标记 `@ConfigSerializable`
- Kotlin 属性用 **camelCase**，YAML 键自动转 **snake_case**（由 `NamingSchemes.SNAKE_CASE` 处理）
- data class 的构造参数提供**默认值**，作为配置缺省值
- 复合配置应拆分为多个嵌套 data class

### 配置读取

```kotlin
import cc.mewcraft.lazyconfig.MAIN_CONFIG
import cc.mewcraft.lazyconfig.access.entryOrElse
import cc.mewcraft.lazyconfig.access.optionalEntry

// 读取一个带默认值的配置项 (推荐):
private val config: MyConfig by MAIN_CONFIG.entryOrElse(MyConfig(), "yaml_node_path")

// 读取一个可选配置项:
private val optConfig: MyConfig? by MAIN_CONFIG.optionalEntry("yaml_node_path")
```

### 示例: 嵌套 @ConfigSerializable 配置

```kotlin
@ConfigSerializable
data class SequenceComboConfig(
    val comboTimeoutTicks: Int = 20,       // → combo_timeout_ticks
    val leftClick: ClickDisplayConfig = ClickDisplayConfig(literalSymbol = "L"),  // → left_click
    val rightClick: ClickDisplayConfig = ClickDisplayConfig(literalSymbol = "R"), // → right_click
    val sequence: SequenceResultConfig = SequenceResultConfig(),                  // → sequence
)

@ConfigSerializable
data class ClickDisplayConfig(
    val literalSymbol: String = "?",       // → literal_symbol
    val successStyle: String = "<green>",  // → success_style
)
```

对应 YAML (`config.yml`):
```yaml
sequence_castable_trigger_display:
  combo_timeout_ticks: 20
  left_click:
    literal_symbol: "L"
    success_style: "<green>"
```

> **要点**: 将重复字段封装为嵌套 data class，避免零散的配置项。一个功能对应一个顶层 data class，用 `entryOrElse` 一次性读取。

---

## @Init 生命周期注解

使用 `@Init(InitStage.XXX)` + `@InitFun` 控制初始化时机:
- `InitStage.BOOTSTRAP` — 最早期，注册表等基础设施
- `InitStage.PRE_WORLD` — 世界加载前
- `InitStage.POST_WORLD` — 世界加载后 (大多数 Feature / Listener 使用此阶段)

```kotlin
@Init(InitStage.POST_WORLD)
object MyFeature : Listener {
    @InitFun
    fun init() { /* 注册事件监听器等 */ }
}
```

> `object` 标注 `@Init` 后会被反射自动初始化，无需手动调用。IDE 可能报 "Object is never used" 警告，可忽略。

---

## OnlineUserTicker 范式

需要每 tick 对每个在线玩家执行逻辑时:
- 实现 `OnlineUserTicker` 接口 (`wakame-mixin/.../entity/player/ticker.kt`)
- 在 `ServerOnlineUserTicker` (`wakame-plugin`) 中注册调用

```kotlin
// 定义
object MyTickSystem : OnlineUserTicker {
    override fun onTickUser(user: User, player: Player) { /* per-tick logic */ }
}

// 注册 (ServerOnlineUserTicker.kt)
MyTickSystem.onTickUser(user, player)
```

---

## MetadataMap 范式

用于存储与玩家关联的临时运行时状态:

```kotlin
// 定义 key
val MY_KEY: MetadataKey<MyState> = metadataKey("my_feature:state")

// 读写
val metadata = player.metadata()
val state = metadata.getOrPut(MY_KEY) { MyState() }
metadata.getOrNull(MY_KEY)
metadata.remove(MY_KEY)
```

---

## CastableTrigger 体系

`CastableTrigger` 是一个 sealed interface，有 4 种实现:

| 类型 | 说明 | 示例 |
|---|---|---|
| `GenericCastableTrigger` | 单击左/右键 | `LEFT_CLICK`, `RIGHT_CLICK` |
| `SequenceCastableTrigger` | 3 次左/右键组合序列 | `LLR`, `RRL`, `RRR` 等 8 种 |
| `SpecialCastableTrigger` | 特殊动作 | `ON_EQUIP`, `ON_UNEQUIP`, `ON_CONSUME` |
| `InputCastableTrigger` | WASD/跳跃/潜行/冲刺 | `FORWARD`, `JUMP`, `SNEAK` 等 |

---

## 编码风格

- 注释语言: **中文**
- KDoc: 公共 API 必须有 KDoc (中文)
- 代码折叠: 使用 `//<editor-fold desc="...">` ... `//</editor-fold>` 组织长文件
- 导入别名: 当类名冲突时使用 `import ... as`，例如:
  ```kotlin
  import cc.mewcraft.wakame.item.behavior.impl.Castable as CastableBehavior
  import cc.mewcraft.wakame.item.property.impl.Castable as CastableProp
  import org.bukkit.Sound as BukkitSound
  ```
