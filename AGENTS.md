# Agent Instructions for Koish (Wakame)

本文档总结了 Koish 项目中关键的编码范式，供 AI Agent 在后续对话中直接参考，避免重复分析项目结构。

---

## 模块职责划分: wakame-mixin vs wakame-plugin

`wakame-mixin` 是 **NMS Mixin 模块**，包含 Java Mixin 类 (NMS 补丁) 和 Kotlin bridge 接口 (NMS 级别的抽象)。使用 Horizon (`io.canvasmc.horizon`) + Weaver (`io.canvasmc.weaver.userdev`) 构建。**不包含任何游戏逻辑或物品系统代码。**

`wakame-plugin` 包含**所有游戏逻辑**：接口、数据结构、实现、配置读取、事件监听、tick 系统等。ItemBehavior、ItemProp、CastableTrigger、OnlineUserTicker、@Init 等核心抽象全部在此模块中。

### wakame-mixin Bridge 模式 (跨模块)

当 Mixin 类 (NMS 层) 需要调用插件逻辑时，使用 **Bridge 委托模式**：
- 在 `wakame-mixin/bridge/` 中定义 `interface`，带有 `companion object Impl` 委托到 `var implementation`
- Mixin 类直接调用 companion object
- `wakame-plugin` 通过 `setImplementation()` 注入实现

```kotlin
// wakame-mixin/bridge/DamageManagerBridge.kt
interface DamageManagerBridge {
    companion object Impl : DamageManagerBridge {
        private var implementation: DamageManagerBridge = object : DamageManagerBridge { /* 默认抛异常 */ }
        fun setImplementation(impl: DamageManagerBridge) { implementation = impl }
        override fun injectDamageLogic(...) = implementation.injectDamageLogic(...)
    }
    fun injectDamageLogic(event: EntityDamageEvent, originalLastHurt: Float, isDuringInvulnerable: Boolean): Float
}
```

关键 Bridge 接口: `KoishItemBridge` (物品系统)、`DamageManagerBridge` (伤害系统)、`MythicMobsBridge` 等。

### 模块内解耦模式 (wakame-plugin 内部)

当 `wakame-plugin` 内部需要解耦不同特性时，使用 **handler 注入模式**：
- 在调用方持有 `var handler: MyHandler? = null`
- 在实现方的 `init {}` 块中注入

```kotlin
// 定义接口 (wakame-plugin/item/behavior/impl/SequenceComboHandler.kt)
fun interface SequenceComboHandler {
    fun handleInput(player: Player, castableMap: Map<String, CastableProp>, input: GenericCastableTrigger)
}

// 调用方 (wakame-plugin/item/behavior/impl/Castable.kt)
object Castable : SimpleInteract {
    var sequenceComboHandler: SequenceComboHandler? = null
}

// 实现并注入 (wakame-plugin/item/feature/SequenceComboFeature.kt)
@Init(InitStage.POST_WORLD)
object SequenceComboFeature : SequenceComboHandler {
    init { CastableBehavior.sequenceComboHandler = this }
    override fun handleInput(...) { /* 实现逻辑 */ }
}
```

---

## ItemProp 范式

`ItemProp` 是附加在物品类型上的静态数据，定义在 `wakame-plugin` 中。

### 定义新的 ItemProp 数据类型

在 `wakame-plugin/src/main/kotlin/cc/mewcraft/wakame/item/property/impl/` 下创建 `data class`，标记 `@ConfigSerializable`。

### 注册到 ItemPropTypes

在 `wakame-plugin/.../item/property/ItemPropTypes.kt` 中添加:
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

`ItemBehavior` 描述物品与世界交互的逻辑，不含数据，定义在 `wakame-plugin` 中。

### 定义新的 ItemBehavior

- 在 `wakame-plugin/.../item/behavior/impl/` 下创建 `object`，实现 `SimpleInteract`（或 `ItemBehavior`）。
  - 子目录按类别组织: `weapon/` (武器行为), `external/` (依赖外部插件的行为), `test/` (测试用)
  - 对应的 property impl 也使用相同子目录: `item/property/impl/weapon/`
- `SimpleInteract` 将 6 种交互统一为 `handleSimpleUse` (右键) 和 `handleSimpleAttack` (左键) 两个入口。
  - 当方块/实体本身有交互时，`SimpleInteract` 会自动让出 (`FAIL`)，不执行物品交互。
  - 若物品交互优先级需高于方块/实体交互，应直接实现 `ItemBehavior` 而非 `SimpleInteract`。
- `ItemBehavior` 顶级接口提供的所有 handler:
  - `handleUse` / `handleUseOn` / `handleUseEntity` — 右键 (空气/方块/实体)
  - `handleAttack` / `handleAttackOn` / `handleAttackEntity` — 左键 (空气/方块/实体)
  - `handleCauseDamage` / `handleReceiveDamage` — 物品处于激活状态时造成/受到伤害
  - `handleDurabilityDecrease` — 物品失去耐久度
  - `handleStopUse` — 停止使用 (不可取消)
  - `handleConsume` — 消耗物品 (`minecraft:consumable` 组件)
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

嵌套 YAML 路径使用 varargs (每级路径一个参数):
```kotlin
// 对应 YAML: debug.logging.damage
private val LOGGING by MAIN_CONFIG.optionalEntry<Boolean>("debug", "logging", "damage").orElse(false)
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

### 排序与依赖

`@Init` 和 `@InitFun` 均支持 `runAfter` / `runBefore` 参数，用于声明初始化顺序依赖:
```kotlin
@Init(InitStage.POST_WORLD, runAfter = [SomeDependency::class])
object MyFeature {
    @InitFun
    fun init() { /* 在 SomeDependency 初始化完成后执行 */ }
}
```

### 关闭逻辑

使用 `@DisableFun` 标注需要在插件禁用时执行的清理方法:
```kotlin
@Init(InitStage.POST_WORLD)
object MyFeature {
    @InitFun
    fun init() { /* 启动逻辑 */ }

    @DisableFun
    fun disable() { /* 清理逻辑 */ }
}
```

> `object` 标注 `@Init` 后会被反射自动初始化，无需手动调用。IDE 可能报 "Object is never used" 警告，可忽略。

---

## OnlineUserTicker 范式

需要每 tick 对每个在线玩家执行逻辑时:
- 实现 `OnlineUserTicker` 接口 (`wakame-plugin/.../entity/player/ticker.kt`)
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

用于存储与游戏对象关联的临时运行时状态 (支持 `Player`/`Entity`/`Block`/`World`):

```kotlin
// 定义 key
val MY_KEY: MetadataKey<MyState> = metadataKey("my_feature:state")

// 读写 (Player/Entity/Block/World 均可调用 .metadata())
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

## MCP 工具使用偏好

执行特定任务时，优先使用以下 MCP 工具:

|任务|首选工具|说明|
|---|---|---|
|检索项目代码 (符号、引用、结构)|Serena |语言感知的代码分析，优于纯文本搜索|
|查询第三方库/API 文档|Context7 |获取最新的库文档、代码示例和配置步骤，无需用户显式要求|
|爬取/读取网页|Firecrawl 或 `fetch`|Firecrawl 功能更强（支持 JS 渲染、结构化提取），`fetch` 适合轻量级单页抓取|

> 如果某个 MCP 工具不可用，退而求其次使用备选工具（`grep_search`、`read_file` 等），核心目标不变。

---

## 编码风格

- 注释语言: **中文**
- KDoc: 公共 API 必须有 KDoc (中文)
- 文件末尾: 必须以恰好一个换行符 (`\n`) 结尾，不留多余空行
- 代码折叠: 使用 `//<editor-fold desc="...">` ... `//</editor-fold>` 组织长文件
- Markdown 表格: 使用最小格式，不要加多余的空格或 `-` 来对齐列。IDE 用户有 Markdown 可视化，源代码的人类可读性不重要。例如:
  ```markdown
  |列A|列B|
  |---|---|
  |值1|值2|
  ```
- 导入别名: 当类名冲突时使用 `import ... as`，例如:
  ```kotlin
  import cc.mewcraft.wakame.item.behavior.impl.Castable as CastableBehavior
  import cc.mewcraft.wakame.item.property.impl.Castable as CastableProp
  import org.bukkit.Sound as BukkitSound
  ```
