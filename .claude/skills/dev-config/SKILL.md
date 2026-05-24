---
name: dev-config
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

## 核心概念

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

## 三种配置范式

### 范式 A: Provider 响应式配置（推荐，适用于单例配置项）

当配置项像"单例"一样存在——某个功能只需从配置文件中读取少量值——使用 Provider 响应式范式。配置重载时值自动更新。

核心 API: `entryOrElse` (带默认值), `optionalEntry` + `orElse` (可选), `entry` (必须)。配合 Kotlin `by` 委托使用。支持嵌套路径 (`"debug", "logging", "damage"`)、Provider 链式变换 (`.map { }`)、子节点 Provider (`.node()`)。

**详细用法见 `references/provider-config.md`。**

### 范式 B: @ConfigSerializable 数据类（推荐，适用于结构化配置）

当多个零散的配置项服务于同一个逻辑时，将它们封装为一个 `@ConfigSerializable` data class。属性用 camelCase（自动映射到 snake_case YAML 键），所有构造参数提供默认值。

**详细用法见 `references/serializable-config.md`。**

### 范式 C: 手动加载配置文件夹（适用于注册表批量加载）

当需要遍历一个文件夹加载多个配置文件时，使用 `yamlLoader` DSL 和 Configurate 底层 API。适合物品注册表、战利品表等场景。

**详细用法见 `references/manual-loader.md`。**

### 自定义 TypeSerializer

当 `@ConfigSerializable` 不够用（多态反序列化、自定义格式解析）时，编写自定义序列化器（`SimpleSerializer`, `DispatchingSerializer`）。

**详细用法见 `references/type-serializers.md`。**

---

## API 快速参考

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

## 注意事项

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

---

## 子文件索引

- `references/provider-config.md` — 范式 A: Provider 响应式配置（选择配置源、读取配置项、嵌套路径、Provider 链式变换、子节点 Provider、strong vs weak）
- `references/serializable-config.md` — 范式 B: @ConfigSerializable 数据类（定义 data class、嵌套 data class、读取、关键规则）
- `references/manual-loader.md` — 范式 C: 手动加载配置文件夹（yamlLoader DSL、遍历加载、require 扩展）
- `references/type-serializers.md` — 自定义 TypeSerializer（SimpleSerializer、DispatchingSerializer、注册序列化器）
- `assets/database-module-example.kt` — 完整示例: 功能模块配置（data class 定义 + Provider 读取）
- `assets/feature-toggle-example.kt` — 完整示例: 简单功能开关
