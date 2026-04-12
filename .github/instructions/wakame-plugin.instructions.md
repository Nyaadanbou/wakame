---
applyTo: "wakame-plugin/src/**/*.kt"
---

# wakame-plugin 编码指南

wakame-plugin 是 Koish 的运行时实现层，包含所有游戏逻辑：ItemBehavior、ItemProp、CastableTrigger、配置读取、事件监听、tick 系统和 Feature 实现。

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

---

## @Init 生命周期注解

使用 `@Init(InitStage.XXX)` + `@InitFun` 控制初始化时机:

|阶段|时机|
|---|---|
|`BOOTSTRAP`|最早期，注册表等基础设施|
|`PRE_WORLD`|世界加载前|
|`POST_WORLD`|世界加载后 (大多数 Feature / Listener)|

```kotlin
@Init(InitStage.POST_WORLD)
object MyFeature : Listener {
    @InitFun
    fun init() { /* 注册事件监听器等 */ }

    @DisableFun
    fun disable() { /* 清理逻辑 */ }
}
```

- `runAfter` / `runBefore` 参数声明初始化顺序依赖
- `object` 标注 `@Init` 后会被反射自动初始化，无需手动调用

---

## 配置文件 (@ConfigSerializable) 范式

使用 Configurate ObjectMapper 将 YAML 自动映射到 Kotlin data class。

### 核心规则

- 标记 `@ConfigSerializable`，构造参数提供**默认值**
- Kotlin `camelCase` 属性 ↔ YAML `snake_case` 键 (自动映射)
- 重复字段封装为嵌套 data class
- 配置数据类与 Feature object 放在同文件

### 配置读取

```kotlin
import cc.mewcraft.lazyconfig.MAIN_CONFIG
import cc.mewcraft.lazyconfig.access.entryOrElse
import cc.mewcraft.lazyconfig.access.optionalEntry

// 带默认值 (推荐):
private val config: MyConfig by MAIN_CONFIG.entryOrElse(MyConfig(), "yaml_node_path")

// 可选:
private val optConfig: MyConfig? by MAIN_CONFIG.optionalEntry("yaml_node_path")

// 嵌套路径 (varargs): debug.logging.damage
private val LOGGING by MAIN_CONFIG.optionalEntry<Boolean>("debug", "logging", "damage").orElse(false)
```

### 嵌套配置示例

```kotlin
@ConfigSerializable
data class SequenceComboConfig(
    val comboTimeoutTicks: Int = 20,       // → combo_timeout_ticks
    val leftClick: ClickDisplayConfig = ClickDisplayConfig(literalSymbol = "L"),
    val rightClick: ClickDisplayConfig = ClickDisplayConfig(literalSymbol = "R"),
)

@ConfigSerializable
data class ClickDisplayConfig(
    val literalSymbol: String = "?",       // → literal_symbol
    val successStyle: String = "<green>",  // → success_style
)
```

---

## ItemProp 范式

`ItemProp` 是附加在物品类型上的静态数据。

- 在 `item/property/impl/` 下创建 `@ConfigSerializable data class`
- 在 `ItemPropTypes` 中注册:
  ```kotlin
  @JvmField
  val MY_PROP: ItemPropType<MyPropData> = typeOf("my_prop") {
      serializers { register(MyCustomSerializer()) }
  }
  ```
- 读取: `itemStack.getProp(ItemPropTypes.MY_PROP)`

---

## ItemBehavior 范式

`ItemBehavior` 描述物品与世界交互的逻辑，不含数据。

### 定义

- 在 `item/behavior/impl/` 下创建 `object`，实现 `SimpleInteract` 或 `ItemBehavior`
- 子目录按类别: `weapon/`, `external/`, `test/`
- `SimpleInteract` 将交互统一为 `handleSimpleUse` (右键) 和 `handleSimpleAttack` (左键)
  - 方块/实体有交互时自动让出 (`FAIL`)
  - 若物品优先级需高于方块/实体交互，直接实现 `ItemBehavior`

### ItemBehavior 接口的所有 handler

|handler|触发场景|
|---|---|
|`handleUse` / `handleUseOn` / `handleUseEntity`|右键 (空气/方块/实体)|
|`handleAttack` / `handleAttackOn` / `handleAttackEntity`|左键 (空气/方块/实体)|
|`handleCauseDamage` / `handleReceiveDamage`|激活状态时造成/受到伤害|
|`handleDurabilityDecrease`|物品失去耐久度|
|`handleStopUse`|停止使用 (不可取消)|
|`handleConsume`|消耗物品 (`minecraft:consumable`)|

### 注册与读取

```kotlin
// 注册 (ItemBehaviorTypes.kt)
@JvmField val MY_BEHAVIOR = typeOf("my_behavior", MyBehavior)

// Behavior 读取 Prop
object MyBehavior : SimpleInteract {
    override fun handleSimpleUse(context: UseContext): InteractionResult {
        val prop = context.itemstack.getProp(ItemPropTypes.MY_PROP) ?: return InteractionResult.PASS
        return InteractionResult.PASS
    }
}
```

---

## CastableTrigger 体系

`CastableTrigger` 是 sealed interface，有 4 种实现:

|类型|说明|示例|
|---|---|---|
|`GenericCastableTrigger`|单击左/右键|`LEFT_CLICK`, `RIGHT_CLICK`|
|`SequenceCastableTrigger`|3 次左/右键组合|`LLR`, `RRL`, `RRR` 等 8 种|
|`SpecialCastableTrigger`|特殊动作|`ON_EQUIP`, `ON_UNEQUIP`, `ON_CONSUME`|
|`InputCastableTrigger`|WASD/跳跃/潜行/冲刺|`FORWARD`, `JUMP`, `SNEAK`|

---

## 模块内解耦: handler 注入模式

当 wakame-plugin 内部需要解耦不同特性时:

```kotlin
// 1. 定义接口
fun interface SequenceComboHandler {
    fun handleInput(player: Player, castableMap: Map<String, CastableProp>, input: GenericCastableTrigger)
}

// 2. 调用方持有可空 handler
object Castable : SimpleInteract {
    var sequenceComboHandler: SequenceComboHandler? = null
}

// 3. 实现方在 init 块注入
@Init(InitStage.POST_WORLD)
object SequenceComboFeature : SequenceComboHandler {
    init { CastableBehavior.sequenceComboHandler = this }
    override fun handleInput(...) { /* 实现 */ }
}
```

---

## OnlineUserTicker 范式

每 tick 对每个在线玩家执行逻辑:

```kotlin
object MyTickSystem : OnlineUserTicker {
    override fun onTickUser(user: User, player: Player) { /* per-tick logic */ }
}
```

在 `ServerOnlineUserTicker.kt` 的 `on(ServerTickStartEvent)` 方法中添加调用:
```kotlin
MyTickSystem.onTickUser(user, player)
```

---

## MetadataMap 范式

存储与游戏对象关联的临时运行时状态 (Player/Entity/Block/World):

```kotlin
class MyState {
    companion object {
        val METADATA_KEY: MetadataKey<MyState> = metadataKey("feature:my_state")
    }
}

// 读写
val metadata = player.metadata()
val state = metadata.getOrPut(MyState.METADATA_KEY) { MyState() }
metadata.remove(MyState.METADATA_KEY)
```

---

## 导入别名约定

当类名冲突时必须使用 `import ... as`:
```kotlin
import cc.mewcraft.wakame.item.behavior.impl.Castable as CastableBehavior
import cc.mewcraft.wakame.item.property.impl.Castable as CastableProp
import org.bukkit.Sound as BukkitSound
```
