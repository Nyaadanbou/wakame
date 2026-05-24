# 范式 B: @ConfigSerializable 数据类

当多个零散的配置项服务于同一个逻辑时，将它们封装为一个 `@ConfigSerializable` data class，然后用 `entryOrElse` 一次性读取。

## 定义 data class

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

## 嵌套 data class

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

## 在代码中读取

```kotlin
// 一行搞定: 读取整个结构，配置缺失时使用 data class 默认值
private val config: SequenceComboConfig by MAIN_CONFIG.entryOrElse(
    SequenceComboConfig(), "sequence_castable_trigger_display"
)

// 使用
val timeout = config.comboTimeoutTicks
val leftSymbol = config.leftClick.literalSymbol
```

## 关键规则

1. **必须标注 `@ConfigSerializable`** — 否则 ObjectMapper 不会处理
2. **属性用 camelCase** — `NamingSchemes.SNAKE_CASE` 自动转换
3. **所有构造参数提供默认值** — 作为 YAML 缺省值
4. **data class 放在哪个模块**:
   - 纯数据定义 (不依赖运行时逻辑) -> `wakame-mixin`
   - 依赖插件运行时的类 -> `wakame-plugin`
