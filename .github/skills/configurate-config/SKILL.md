---
name: configurate-config
description: >
  Guide for reading, writing, and organizing configuration files in the Koish (wakame) project
  using SpongePowered Configurate and the project's lazyconfig wrapper.
  Use this skill when asked to create, modify, or read configuration files,
  add new config entries, define @ConfigSerializable data classes,
  use ConfigAccess / MAIN_CONFIG / FEATURE_CONFIG / entryOrElse / optionalEntry,
  write custom TypeSerializers, or load config folders with yamlLoader.
---

# 配置文件使用指南

本技能指导你在 Koish (wakame) 项目中正确使用配置文件系统。

底层库为 [SpongePowered Configurate](https://github.com/SpongePowered/Configurate)，项目在其之上构建了 `lazyconfig` 封装层，提供基于 `Provider` 的响应式配置读取。

---

## 1. 核心概念

| 概念 | 说明 |
|---|---|
| **Configurate** | SpongePowered 的配置库，提供 `ConfigurationNode` 树、`TypeSerializer`、`ObjectMapper` 等基础设施 |
| **ConfigAccess** | 项目封装的配置文件访问入口，通过 `ConfigAccess["id"]` 获取配置文件的 `Provider<CommentedConfigurationNode>` |
| **Provider** | `xyz.xenondevs.commons.provider.Provider`，一个响应式容器，配置重载时自动更新下游值 |
| **entryOrElse / optionalEntry** | `Provider` 上的扩展函数，声明式地读取配置项并提供默认值 |
| **@ConfigSerializable** | Configurate 的 ObjectMapper 注解，将 YAML 自动映射到 Kotlin data class |
| **KoishObjectMapper** | 项目定制的 ObjectMapper，默认使用 `NamingSchemes.SNAKE_CASE`（camelCase 属性自动对应 snake_case YAML 键） |
| **yamlLoader** | `wakame-plugin` 中的 DSL 工具，用于手动构建 `YamlConfigurationLoader` 加载任意 YAML 文件 |

### 配置文件存放位置

所有默认配置文件位于 `wakame-plugin/src/main/resources/configs/`，运行时被提取到插件数据目录。

`ConfigAccess["id"]` 的 id 映射规则:
- `"config"` -> `configs/config.yml` (即 `MAIN_CONFIG`)
- `"database"` -> `configs/database.yml`
- `"features"` -> `configs/features.yml` (即 `FEATURE_CONFIG`)
- `"damage/config"` -> `configs/damage/config.yml`
- `"hook/towny/config"` -> `configs/hook/towny/config.yml`

默认命名空间为 `koish`，省略时自动补全。

---

## 2. 范式 A: Provider 响应式配置（推荐，适用于单例配置项）

当配置项像"单例"一样存在——某个功能只需从配置文件中读取少量值——使用 Provider 响应式范式。配置重载时值自动更新，无需手动处理。

### 2.1 选择配置源

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

### 2.2 读取配置项

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

### 2.3 嵌套路径

每级路径用一个 vararg 参数表示:

```kotlin
// 对应 YAML: debug.logging.damage
private val LOGGING by MAIN_CONFIG.optionalEntry<Boolean>("debug", "logging", "damage").orElse(false)

// 对应 YAML: equipment.amount_per_damage
private val EQUIPMENT_CONFIG = DAMAGE_CONFIG.node("equipment")
private val AMOUNT by EQUIPMENT_CONFIG.optionalEntry<Double>("amount_per_damage").orElse(0.25)
```

### 2.4 Provider 链式变换

`Provider` 支持 `map` 等操作，可以对读取到的原始值进行变换:

```kotlin
// 读取 tick 数并转换为毫秒
private val COOLDOWN_MS by MY_CONFIG.optionalEntry<Long>("cooldown_ticks").orElse(5L).map { it * 50L }
```

### 2.5 子节点 Provider

使用 `node()` 从一个 Provider 中派生子节点 Provider:

```kotlin
import cc.mewcraft.lazyconfig.access.node

private val DAMAGE_CONFIG = ConfigAccess["damage/config"]
private val RULES_CONFIG = DAMAGE_CONFIG.node("rules")        // 指向 rules 子节点
private val EQUIPMENT_CONFIG = DAMAGE_CONFIG.node("equipment") // 指向 equipment 子节点

// 然后在子节点上继续读取
private val LEAST_DAMAGE: Double by RULES_CONFIG.entry("least_damage")
```

### 2.6 strong vs weak Provider

- `entry` / `optionalEntry` / `entryOrElse` / `node` — 返回 **weak** Provider（弱引用，无强引用持有时可被 GC）
- `strongEntry` / `strongOptionalEntry` / `strongEntryOrElse` / `strongNode` — 返回 **strong** Provider（始终持有）

**经验法则**: 大多数场景使用默认的 weak 版本即可。只有当 Provider 没有被任何字段持有、但仍需长期存活时，才用 strong 版本。

---

## 3. 范式 B: @ConfigSerializable 数据类（推荐，适用于结构化配置）

当多个零散的配置项服务于同一个逻辑时，将它们封装为一个 `@ConfigSerializable` data class，然后用 `entryOrElse` 一次性读取。

### 3.1 定义 data class

```kotlin
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class MyFeatureConfig(
    val enabled: Boolean = true,               // -> enabled
    val maxRetries: Int = 3,                   // -> max_retries (自动 snake_case)
    val timeoutSeconds: Long = 30L,            // -> timeout_seconds
    val allowedWorlds: List<String> = listOf() // -> allowed_worlds
)
```

### 3.2 嵌套 data class

重复的配置结构应提取为独立的嵌套 data class:

```kotlin
@ConfigSerializable
data class SequenceComboConfig(
    val comboTimeoutTicks: Int = 20,                                              // -> combo_timeout_ticks
    val leftClick: ClickDisplayConfig = ClickDisplayConfig(literalSymbol = "L"),   // -> left_click
    val rightClick: ClickDisplayConfig = ClickDisplayConfig(literalSymbol = "R"),  // -> right_click
    val sequence: SequenceResultConfig = SequenceResultConfig(),                   // -> sequence
)

@ConfigSerializable
data class ClickDisplayConfig(
    val literalSymbol: String = "?",       // -> literal_symbol
    val successStyle: String = "<green>",  // -> success_style
    val failureStyle: String = "<red>",    // -> failure_style
    val progressStyle: String = "<yellow>" // -> progress_style
)

@ConfigSerializable
data class SequenceResultConfig(
    val connector: String = "<gray> -> </gray>", // -> connector
    val success: String = "<green>OK",            // -> success
    val failure: String = "<red>BAD",             // -> failure
    val timeout: String = "<gray>TIMEOUT",        // -> timeout
)
```

对应的 YAML:

```yaml
sequence_castable_trigger_display:
  combo_timeout_ticks: 20
  left_click:
    literal_symbol: "L"
    success_style: "<green>"
    failure_style: "<red>"
    progress_style: "<yellow>"
  right_click:
    literal_symbol: "R"
  sequence:
    connector: "<gray> -> </gray>"
    success: "<green>OK"
```

### 3.3 在代码中读取

```kotlin
// 一行搞定: 读取整个结构，配置缺失时使用 data class 默认值
private val config: SequenceComboConfig by MAIN_CONFIG.entryOrElse(
    SequenceComboConfig(), "sequence_castable_trigger_display"
)

// 使用
val timeout = config.comboTimeoutTicks
val leftSymbol = config.leftClick.literalSymbol
```

### 3.4 关键规则

1. **必须标注 `@ConfigSerializable`** — 否则 ObjectMapper 不会处理
2. **属性用 camelCase** — `NamingSchemes.SNAKE_CASE` 自动转换
3. **所有构造参数提供默认值** — 作为 YAML 缺省值
4. **data class 放在哪个模块**:
   - 纯数据定义 (不依赖运行时逻辑) -> `wakame-mixin`
   - 依赖插件运行时的类 -> `wakame-plugin`

---

## 4. 范式 C: 手动加载配置文件夹（适用于注册表批量加载）

当需要遍历一个文件夹加载多个配置文件、每个文件对应一个注册表条目时，不适合用 Provider 响应式范式，而是直接使用底层 Configurate API。

### 4.1 使用 yamlLoader DSL

```kotlin
import cc.mewcraft.lazyconfig.configurate.register
import cc.mewcraft.lazyconfig.configurate.require
import cc.mewcraft.wakame.util.configurate.yamlLoader

val loader = yamlLoader {
    withDefaults()                          // 应用项目默认设置 (缩进、序列化器等)
    serializers {
        registerAll(MY_CUSTOM_SERIALIZERS)  // 注册自定义序列化器 (如需要)
    }
}
```

### 4.2 遍历文件夹加载

```kotlin
// 获取存放配置文件的目录
val dataDir = getFileInConfigDirectory("item/")

dataDir.walk().drop(1)                      // drop(1) 跳过根目录自身
    .filter { it.isFile && it.extension == "yml" }
    .forEach { file ->
        try {
            // 从文件内容构建节点树
            val rootNode = loader.buildAndLoadString(file.readText())

            // 从文件路径推导 ID
            val id = file.relativeTo(dataDir)
                .invariantSeparatorsPath
                .substringBeforeLast('.')

            // 反序列化各部分
            val properties = rootNode.require<ItemPropContainer>()
            val behaviors = rootNode.require<ItemBehaviorContainer>()

            // 注册到注册表
            registry.add(id, MyItem(id, properties, behaviors))
        } catch (e: Exception) {
            LOGGER.error("Failed to load config from file: {}", file.path)
        }
    }
```

### 4.3 `require` 扩展函数

```kotlin
import cc.mewcraft.lazyconfig.configurate.require

// 从节点反序列化，缺失时抛 NoSuchElementException
val value: MyType = node.require<MyType>()
```

### 4.4 典型场景

- 物品注册表加载 (`CustomItemRegistryLoader`)
- 战利品表加载
- 技能配置加载
- 任何 "一个文件夹 = 一组注册表条目" 的场景

---

## 5. 自定义 TypeSerializer

当 `@ConfigSerializable` 不够用（如需要多态反序列化、自定义格式解析）时，编写自定义序列化器。

### 5.1 SimpleSerializer (只需反序列化)

```kotlin
import cc.mewcraft.lazyconfig.configurate.SimpleSerializer
import org.spongepowered.configurate.ConfigurationNode

// 大多数场景只需要反序列化，不需要序列化回 YAML
val MySerializer = SimpleSerializer<MyType> { type, node ->
    val name = node.node("name").string ?: return@SimpleSerializer null
    val value = node.node("value").getInt(0)
    MyType(name, value)
}
```

### 5.2 DispatchingSerializer (多态类型)

```kotlin
import cc.mewcraft.lazyconfig.configurate.serializer.DispatchingSerializer

// 根据 "type" 字段分发到不同的子类型
val MyPolymorphicSerializer = DispatchingSerializer.createPartial<String, BaseType>(
    mapOf(
        "type_a" to TypeA::class,
        "type_b" to TypeB::class,
    )
)
```

对应 YAML:

```yaml
my_entry:
  type: type_a
  # TypeA 的其他字段...
```

### 5.3 注册序列化器

**方式一: 通过 ConfigAccess 注册（对整个命名空间生效）:**

```kotlin
import cc.mewcraft.lazyconfig.access.ConfigAccess
import cc.mewcraft.lazyconfig.access.registerSerializer

ConfigAccess.registerSerializer("koish", MySerializer)
```

**方式二: 在 yamlLoader 中注册（仅对该 loader 生效）:**

```kotlin
val loader = yamlLoader {
    withDefaults()
    serializers {
        register(MySerializer)
        register<BaseType>(MyPolymorphicSerializer)
    }
}
```

**方式三: 在 `ItemPropTypes` / `ItemBehaviorTypes` 中注册:**

```kotlin
@JvmField
val MY_PROP: ItemPropType<MyData> = typeOf("my_prop") {
    serializers {
        register(MyCustomSerializer())
    }
}
```

---

## 6. 完整示例: 功能模块配置

展示一个完整的功能模块如何使用配置，从 data class 定义到实际读取:

```kotlin
// ========== 配置数据类 (可放在 wakame-mixin 或 wakame-plugin) ==========

@ConfigSerializable
data class DatabaseCredentials(
    val host: String = "localhost",
    val port: Int = 3306,
    val database: String = "koish",
    val username: String = "minecraft",
    val password: String = "",
    val parameters: String = "",
    val filePath: String = "",             // -> file_path
)

@ConfigSerializable
data class DatabasePoolConfig(
    val maximumPoolSize: Int = 10,         // -> maximum_pool_size
    val minimumIdle: Int = 10,             // -> minimum_idle
    val maximumLifetime: Long = 1800000L,  // -> maximum_lifetime
    val keepAliveInterval: Long = 0L,      // -> keep_alive_interval
    val connectionTimeout: Long = 5000L,   // -> connection_timeout
)

// ========== 功能实现 (wakame-plugin) ==========

private val DB_CONFIG = ConfigAccess["database"]  // -> configs/database.yml

@Init(InitStage.PRE_WORLD)
object DatabaseManager {

    private val type by DB_CONFIG.entryOrElse<DatabaseType>(DatabaseType.SQLITE, "type")
    private val credentials by DB_CONFIG.entryOrElse<DatabaseCredentials>(DatabaseCredentials(), "credentials")
    private val poolConfig by DB_CONFIG.entryOrElse<DatabasePoolConfig>(DatabasePoolConfig(), "connection_pool")

    @InitFun
    fun init() {
        // 直接使用 type, credentials, poolConfig
        // 配置重载时这些值会自动更新
    }
}
```

对应 `configs/database.yml`:

```yaml
type: sqlite

credentials:
  host: localhost
  port: 3306
  database: koish
  username: minecraft
  password: ''
  parameters: '?autoReconnect=true&useSSL=false'
  file_path: ''

connection_pool:
  maximum_pool_size: 10
  minimum_idle: 10
  maximum_lifetime: 1800000
  keep_alive_interval: 0
  connection_timeout: 5000
```

---

## 7. 完整示例: 简单功能开关

展示最简单的配置读取模式——单个布尔开关:

```kotlin
import cc.mewcraft.wakame.feature.FEATURE_CONFIG
import cc.mewcraft.lazyconfig.access.entryOrElse

@Init(InitStage.POST_WORLD)
object ForceCommandLowercase : Listener {

    // 对应 configs/features.yml 中的 force_command_lowercase
    private val enabled by FEATURE_CONFIG.entryOrElse(false, "force_command_lowercase")

    @InitFun
    fun init() {
        if (enabled) registerEvents()
    }
}
```

---

## 8. API 快速参考

### ConfigAccess

```kotlin
ConfigAccess["config"]                    // 获取 configs/config.yml 的 Provider
ConfigAccess["database"]                  // 获取 configs/database.yml 的 Provider
ConfigAccess["damage/config"]             // 获取 configs/damage/config.yml 的 Provider
ConfigAccess.registerSerializer("koish", serializer)  // 注册全局序列化器
```

### Provider 扩展函数 (cc.mewcraft.lazyconfig.access)

| 函数 | 返回 | 说明 |
|---|---|---|
| `entry<T>(vararg path)` | `Provider<T>` | 必选项，缺失抛异常 |
| `optionalEntry<T>(vararg path)` | `Provider<T?>` | 可选项，缺失返回 null |
| `entryOrElse(default, vararg path)` | `Provider<T>` | 带默认值，缺失用默认值 |
| `node(vararg path)` | `Provider<Node>` | 获取子节点 Provider |

### Provider 操作符 (xyz.xenondevs.commons.provider)

| 函数 | 说明 |
|---|---|
| `.orElse(default)` | 为 `Provider<T?>` 提供非空默认值，返回 `Provider<T>` |
| `.map { ... }` | 变换 Provider 的值 |

### Configurate 扩展函数 (cc.mewcraft.lazyconfig.configurate)

| 函数 | 说明 |
|---|---|
| `node.require<T>()` | 从节点反序列化，缺失抛异常 |
| `builder.register(serializer)` | 注册 TypeSerializer (reified 版本) |
| `builder.registerExact(serializer)` | 注册精确匹配的 TypeSerializer |

### 预定义配置源

| 变量 | 文件 | 导入 |
|---|---|---|
| `MAIN_CONFIG` | `configs/config.yml` | `cc.mewcraft.lazyconfig.MAIN_CONFIG` |
| `FEATURE_CONFIG` | `configs/features.yml` | `cc.mewcraft.wakame.feature.FEATURE_CONFIG` |

---

## 9. 注意事项

1. **camelCase 属性 = snake_case YAML 键**: `KoishObjectMapper` 使用 `NamingSchemes.SNAKE_CASE`，属性名 `comboTimeoutTicks` 对应 YAML 键 `combo_timeout_ticks`。不要手动写 snake_case 属性名。
2. **所有构造参数必须有默认值**: `@ConfigSerializable` data class 的每个参数都需要默认值，否则 YAML 中缺少该字段时会抛异常。
3. **`by` 委托是惰性的**: `by CONFIG.entryOrElse(...)` 在首次访问属性时才读取配置，而非声明时。确保访问时配置已加载。
4. **配置自动重载**: Provider 链在配置文件被修改并触发 reload 后自动更新。不需要手动 re-read。
5. **模块边界**:
   - `wakame-mixin` 中可以使用 `ConfigAccess` 和 `MAIN_CONFIG`（它们在 `common/lazyconfig` 模块中）
   - `wakame-mixin` 中**不能**使用 `yamlLoader` DSL（它在 `wakame-plugin` 中）
   - `@ConfigSerializable` data class 可以放在任一模块
6. **不要在 `@ConfigSerializable` data class 中放逻辑**: data class 只放数据和默认值，业务逻辑放在使用方。
7. **注释语言**: 配置文件的 YAML 注释和代码中的 KDoc 注释均使用**中文**。
8. **配置文件路径**: 新增配置文件需要在 `wakame-plugin/src/main/resources/configs/` 下创建对应的默认文件，否则运行时文件不存在。
