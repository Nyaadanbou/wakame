---
name: mixin-to-plugin-migration
description: >
  Guide for migrating code from wakame-mixin to wakame-plugin in the Koish (wakame) project.
  Use this skill when asked to move, migrate, or relocate source files or directories
  from the wakame-mixin module to the wakame-plugin module, when the task involves
  creating bridge interfaces for mixin code, resolving cross-module dependencies
  between mixin and plugin, or refactoring classloader-boundary-crossing code.
---

# 从 wakame-mixin 迁移代码到 wakame-plugin

本技能指导你将 `wakame-mixin` 中的代码（不含 `mixin/` 子目录和根目录 `.kt` 文件）迁移到 `wakame-plugin`，同时确保 `mixin/` 下的 Mixin 代码仍然能正确编译和运行。

> **⚠️ 最重要的规则: 严格遵循用户提示词的迁移范围。**
> 在一次对话中，**只迁移用户提示词中明确提到的文件/目录到提示词中明确提到的目标位置**。
> 不要主动扩大迁移范围，不要"顺便"迁移用户未提及的文件，即使它们看起来相关。
> 每次对话只做提示词要求的那一批，做完就停。

---

## 1. 背景与约束

### 1.1 ClassLoader 隔离

- `wakame-mixin` 由 JVM 的 **system classloader** 加载
- `wakame-plugin` 由 Paper 的 **PluginClassLoader** 加载
- **PluginClassLoader → system classloader**: ✅ 可访问
- **system classloader → PluginClassLoader**: ❌ 不可访问

因此，`mixin/` 中的代码（Mixin Java 类和 support 类）**不能**直接引用 `wakame-plugin` 中的类型。

### 1.2 迁移范围

| 位置 | 动作 |
|---|---|
| `wakame-mixin/.../wakame/mixin/` (core + support) | **保留** 在 wakame-mixin |
| `wakame-mixin/.../wakame/*.kt` (根目录文件) | **保留** 在 wakame-mixin |
| `wakame-mixin/.../wakame/<其他目录>/` | **迁移** 到 wakame-plugin |

待迁移的目录包括: `item/`, `damage/`, `registry/`, `util/`, `entity/`, `element/`, `enchantment/`, `kizami/`, `rarity/`, `loot/`, `feature/`, `context/`, `world/`, `adventure/`, `particle/`, `molang/`, `database/`, `datafix/`, `integration/`, `brewery/`, `serialization/`, `shadow/`, `messaging/`, `event/`

### 1.3 Gradle 依赖

- `wakame-plugin` 依赖 `wakame-mixin` — **plugin 可以自由访问 mixin 的所有代码**
- `wakame-hooks/*` 大多依赖 `wakame-plugin` — 迁移后仍可正常访问
- `wakame-api` 不依赖 mixin 或 plugin — 不受影响

---

## 2. 核心规则

### 2.1 包名不变

迁移只是将文件从一个 Gradle 模块移动到另一个。包名 `cc.mewcraft.wakame.<pkg>` **保持不变**。

例如 `wakame-mixin/.../wakame/item/Foo.kt` 移动到 `wakame-plugin/.../wakame/item/Foo.kt`，包名仍然是 `cc.mewcraft.wakame.item`。

### 2.2 目录合并

`wakame-plugin` 中可能已存在同名目录。迁移时将文件**合并**到已有目录中，不覆盖已有文件。如果两边存在同名文件，需要手动合并内容。

### 2.3 Bridge 接口

当 `mixin/core` 或 `mixin/support` 中的代码直接引用了将要迁移的类型时，必须创建 **Bridge 接口** 来替代直接引用。

---

## 3. 迁移流程（每批次）

每次迁移以**一个目录**（或一组紧密关联的文件）为单位。

> **范围控制:** 用户的提示词会明确指定本次要迁移的文件/目录和目标位置。你必须且仅需迁移这些指定的内容。如果迁移过程中发现其他目录也"应该"一起迁移（如被依赖的类型），**不要擅自迁移**——而是为它创建 Bridge 接口作为临时隔离手段，留待后续对话处理。

### 3.1 分析跨模块引用

**找出 `mixin/` 中引用了待迁移代码的所有位置。**

使用 grep 搜索 `mixin/core` 和 `mixin/support` 中对待迁移包的 import:

```
grep_search(query="import cc.mewcraft.wakame.<迁移目标包>", includePattern="wakame-mixin/**/mixin/**")
```

对搜索结果分类:

| 引用类型 | 处理方式 |
|---|---|
| `mixin/core/*.java` → 待迁移类型 | 必须创建 Bridge 接口 |
| `mixin/support/*.java` → 待迁移类型 | 必须创建 Bridge 接口或重构 |
| `mixin/support/*.kt` → 待迁移类型 | 必须创建 Bridge 接口或重构 |
| 待迁移代码 → 其他待迁移代码 | 不需要 Bridge，一起迁移即可 |
| 待迁移代码 → `mixin/` 中的代码 | 无需改动，plugin 可访问 mixin |
| 待迁移代码 → 根目录 `.kt` | 无需改动，这些文件留在 mixin |

### 3.2 分析待迁移代码中已有的 Bridge 模式

有些待迁移的代码**已经**采用了 Bridge/委托模式（接口 + companion Impl + `setImplementation`）。例如 `DamageManagerApi`。

对于这类代码:
1. 将 **接口定义 + companion Impl** 部分提取到 `mixin/support/` 中（如果 mixin 代码需要引用的话）
2. 将 **实际业务逻辑** 迁移到 `wakame-plugin`
3. 如果 mixin 代码不引用该类型，则整体迁移，无需拆分

### 3.3 创建 Bridge 接口

对于每个需要桥接的类型，在 `wakame-mixin/.../mixin/support/` 下创建 Bridge 接口。

#### Bridge 命名约定

| 原始类型 | Bridge 名称 | 说明 |
|---|---|---|
| 具体类/object | `Koish<功能>Bridge` | 例: `KoishItemBridge` |
| 已有接口 | 可保持原名移到 support/ | 如果接口本身就是纯抽象 |
| 顶层函数/扩展 | 封装为 `fun interface` | 单方法用 `fun interface` |

#### Bridge 标准写法

```kotlin
package cc.mewcraft.wakame.mixin.support

/**
 * <描述此 Bridge 的用途>.
 */
interface MyBridge {

    fun doSomething(param: ParamType): ReturnType

    companion object Impl : MyBridge {
        private var implementation: MyBridge = object : MyBridge {
            override fun doSomething(param: ParamType): ReturnType {
                // 默认实现: 空操作/抛异常/返回默认值，取决于业务需求
                throw IllegalStateException("MyBridge has not been initialized")
            }
        }

        fun setImplementation(impl: MyBridge) {
            implementation = impl
        }

        override fun doSomething(param: ParamType): ReturnType {
            return implementation.doSomething(param)
        }
    }
}
```

**关键点:**
- Bridge 接口的**参数和返回类型**必须是 `mixin/` 可见的类型（NMS 类型、Bukkit API、`mixin/support` 中的类型、根目录的类型）
- 如果参数类型也是待迁移的类型，需要先为该类型创建 Bridge，或用更基础的类型（如 `String`、`Key`、NMS 类型）替代
- companion `Impl` 委托到可变的 `implementation`，默认实现为空操作或抛异常

### 3.4 更新 mixin/ 中的引用

将 `mixin/core` 和 `mixin/support` 中对待迁移类型的直接引用替换为 Bridge 接口:

**Before:**
```java
import cc.mewcraft.wakame.item.HotfixItemName;
// ...
Component itemName = HotfixItemName.INSTANCE.getItemName(itemStack);
```

**After:**
```java
import cc.mewcraft.wakame.mixin.support.KoishItemNameBridge;
// ...
Component itemName = KoishItemNameBridge.Impl.INSTANCE.getItemName(itemStack);
```

### 3.5 移动文件

将文件从 `wakame-mixin` 移动到 `wakame-plugin` 对应位置:

```
wakame-mixin/src/main/kotlin/cc/mewcraft/wakame/<pkg>/
  → wakame-plugin/src/main/kotlin/cc/mewcraft/wakame/<pkg>/
```

如果目标目录已存在，合并文件。

### 3.6 在 wakame-plugin 中注入 Bridge 实现

为每个 Bridge 接口创建实现类并在初始化时注入:

```kotlin
package cc.mewcraft.wakame.<pkg>

import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.mixin.support.MyBridge

@Init(InitStage.BOOTSTRAP) // 或 PRE_WORLD/POST_WORLD，取决于依赖
object MyBridgeImpl : MyBridge {

    @InitFun
    fun init() {
        MyBridge.setImplementation(this)
    }

    override fun doSomething(param: ParamType): ReturnType {
        // 实际实现逻辑（可调用迁移过来的代码）
    }
}
```

**InitStage 选择:**

| 阶段 | 适用场景 |
|---|---|
| `BOOTSTRAP` | Mixin 在服务端启动早期就会被调用的功能 |
| `PRE_WORLD` | 世界加载前需要就绪的功能 |
| `POST_WORLD` | 世界加载后才需要的功能 |

**经验法则:** 如果 Mixin 在世界加载之前就可能被触发（如物品序列化/反序列化、数据组件注册），则使用 `BOOTSTRAP`。

### 3.7 处理 mixin/support 中的非 Bridge 文件

`mixin/support/` 中有些文件不是 Bridge，而是**实际实现**，且引用了待迁移代码。例如:

- `KoishDataSanitizer.kt` — 引用 `util.nms.*`
- `ItemKey.kt` — 引用 `item.KoishItem`, `registry.BuiltInRegistries`
- `KoishIngredient.java` — 引用 `item.ItemRef`, `item.KoishStackData`, `util.item.ExtensionsKt`
- `ExtraDataComponents.java` — 引用 `item.data.ItemDataContainer`

这些文件需要**逐个分析**:

1. **如果引用的类型是简单的类型别名/工具类** — 考虑将类型别名保留在 `mixin/` 中（如 `MojangStack` 这种 typealias）
2. **如果引用的类型是核心数据类型且 mixin 基础设施强依赖** — 考虑将该类型的**纯接口/数据定义**保留在 `mixin/support/`，实现移到 plugin
3. **如果可以用 Bridge 模式替代** — 创建 Bridge 接口，将实现逻辑委托到 plugin
4. **如果引用太深、重构代价过高** — 暂时保留该文件在 `mixin/support/`，后续再处理

---

## 4. 已有的 Bridge 示例

### 4.1 KoishItemBridge — 简单 Bridge

**位置:** `mixin/support/KoishItemBridge.kt`

```kotlin
interface KoishItemBridge {
    companion object Impl : KoishItemBridge {
        private var implementation: KoishItemBridge = object : KoishItemBridge {
            override fun isKoish(stack: MojangStack): Boolean = false
            override fun isPrimaryEnchantment(stack: MojangStack, enchantment: MojangEnchantment): Boolean = false
            override fun isSupportedEnchantment(stack: MojangStack, enchantment: MojangEnchantment): Boolean = false
        }

        fun setImplementation(implementation: KoishItemBridge) {
            this.implementation = implementation
        }

        override fun isKoish(stack: MojangStack): Boolean = implementation.isKoish(stack)
        // ... 其他方法委托
    }

    fun isKoish(stack: MojangStack): Boolean
    fun isPrimaryEnchantment(stack: MojangStack, enchantment: MojangEnchantment): Boolean
    fun isSupportedEnchantment(stack: MojangStack, enchantment: MojangEnchantment): Boolean
}
```

**注入:** `wakame-plugin/.../item/bridge.kt`

```kotlin
@Init(InitStage.BOOTSTRAP)
object KoishItemBridgeImpl : KoishItemBridge {
    @InitFun
    fun init() {
        KoishItemBridge.setImplementation(this)
    }
    override fun isKoish(stack: MojangStack): Boolean = stack.isKoish
    // ... 实际实现
}
```

### 4.2 DamageManagerApi — 领域内 Bridge

**位置:** `wakame-mixin/.../damage/logic-api.kt`（迁移时需拆分）

这类 Bridge 的特点是**已经**在待迁移包中用了委托模式。迁移时:
1. 将 `interface DamageManagerApi` + `companion Impl` 移到 `mixin/support/`
2. 将业务逻辑（`RawDamageContext`、`FinalDamageContext` 等）移到 `wakame-plugin`

### 4.3 MythicPluginBridge — Hook Bridge

**位置:** `mixin/support/MythicPluginBridge.kt`

注入方不在 wakame-plugin，而在 wakame-hook-mythicmobs。说明 Bridge 注入可以来自任何模块。

---

## 5. 特殊情况处理

### 5.1 `util/` 目录中的类型别名

`util/` 中有许多 `typealias`（如 `MojangStack`, `MojangEnchantment`, `KoishKey`），被 `mixin/support` 广泛引用。

**处理方式:** 将纯 typealias 文件保留在 `wakame-mixin` 中，或在 `mixin/support/` 中重新定义。这些 typealias 不含业务逻辑，保留在 mixin 不会引发架构问题。

### 5.2 `mixin/support` 中引用 NMS 工具扩展

如 `KoishDataSanitizer` 引用了 `util.nms.copyItems`。如果这些工具扩展仅操作 NMS 类型，可以将它们保留在 `mixin/` 中（因为它们本质上是 NMS 工具，不是业务逻辑）。

### 5.3 同名文件冲突

如果 `wakame-mixin/.../item/Foo.kt` 和 `wakame-plugin/.../item/Foo.kt` 都存在:
1. 检查两个文件的内容是否互补（接口 vs 实现）
2. 如果是接口 + 实现的关系，合并到同一文件
3. 如果是不同的功能，需要重命名以避免冲突

### 5.4 已有的接口+委托模式

待迁移目录中有些类型**已经**使用了 companion Impl 委托模式（如 `DamageManagerApi`、`ItemStackRenderer`、`ShowItemRenderer`）。这些代码天然适合拆分:
- **接口 + companion Impl** → 移到 `mixin/support/`（如果 mixin 代码需要引用）
- **具体实现** → 移到 `wakame-plugin`

如果 mixin 代码不引用该类型，则整体迁移到 `wakame-plugin`，不需要拆分。

### 5.5 纯接口文件暂不迁移

如果待迁移目录中的某个文件**本身是接口**（`interface` 或 `sealed interface`），且所有其他代码（包括 `wakame-mixin` 和 `wakame-plugin`）都是**通过该接口访问**而非直接引用其实现类，则该接口文件可以**暂时保留在 `wakame-mixin` 中**，不需要立即迁移。

**判断条件:**
1. 文件定义的是 `interface` / `sealed interface` / `abstract class`（纯抽象类型）
2. `wakame-mixin` 中的代码（包括 `mixin/` 下的代码）依赖该接口
3. 所有使用方都是面向接口编程，不直接依赖实现类

**原因:** 接口本身不含业务逻辑，保留在 `wakame-mixin` 中不会引发架构问题，且避免了不必要的 Bridge 创建。实现类仍然照常迁移到 `wakame-plugin`。

**示例:** 假设 `wakame-mixin/.../item/ItemRenderer.kt` 定义了:
```kotlin
interface ItemRenderer {
    fun render(stack: ItemStack): Component
}
```
而 `mixin/support/` 和 `wakame-plugin` 中的代码都通过 `ItemRenderer` 接口访问，实现类 `DefaultItemRenderer` 在待迁移目录中。此时:
- `ItemRenderer`（接口） → **保留**在 `wakame-mixin`
- `DefaultItemRenderer`（实现） → **迁移**到 `wakame-plugin`

这样无需为 `ItemRenderer` 创建 Bridge，因为它已经在 `wakame-mixin` 中，两侧都能直接访问。

---

## 6. 验证清单

每批次迁移完成后，逐项确认:

**编译检查:**
- [ ] `wakame-mixin` 模块可以独立编译通过
- [ ] `wakame-plugin` 模块编译通过
- [ ] `wakame-hooks/*` 相关模块编译通过

**引用检查:**
- [ ] `mixin/core/*.java` 中不再有对已迁移包的直接 import
- [ ] `mixin/support/` 中不再有对已迁移包的直接 import（已替换为 Bridge 或保留的 typealias）
- [ ] 新创建的 Bridge 接口在 `wakame-plugin` 中有对应的实现和注入

**运行时检查:**
- [ ] Bridge 实现在正确的 InitStage 被注入
- [ ] Bridge 的默认实现（未注入时）行为合理（空操作/日志/异常）

**代码规范:**
- [ ] Bridge 接口有中文 KDoc 注释
- [ ] 文件以恰好一个换行符结尾
- [ ] 包名未改变

---

## 7. 完整示例: 迁移 `damage/` 目录

以下展示一个假设性的完整迁移过程:

### 7.1 分析引用

搜索 `mixin/` 中对 `damage` 包的引用:

```
MixinLivingEntity.java → import cc.mewcraft.wakame.damage.DamageManagerApi
```

发现: `DamageManagerApi` **已经**采用了 companion Impl 委托模式。

### 7.2 拆分 DamageManagerApi

由于 `MixinLivingEntity.java` 需要引用 `DamageManagerApi`，将接口部分移到 `mixin/support/`:

```kotlin
// wakame-mixin/.../mixin/support/DamageManagerApi.kt
package cc.mewcraft.wakame.mixin.support

interface DamageManagerApi {
    fun hurt(victim: LivingEntity, metadata: Any, source: DamageSource, knockback: Boolean): Boolean
    fun injectDamageLogic(event: EntityDamageEvent, originLastHurt: Float, isDuringInvulnerable: Boolean): Float

    companion object Impl : DamageManagerApi {
        private var implementation: DamageManagerApi = // ... 默认实现
        fun setImplementation(instance: DamageManagerApi) { this.implementation = instance }
        override fun hurt(...) = implementation.hurt(...)
        override fun injectDamageLogic(...) = implementation.injectDamageLogic(...)
    }
}
```

注意: 如果 `DamageManagerApi` 的方法签名使用了其他待迁移类型（如 `DamageMetadata`），需要:
- 要么先迁移 `DamageMetadata` 并为其创建 Bridge
- 要么在 Bridge 接口中使用更基础的参数类型
- 要么将 `DamageMetadata` 的纯数据定义保留在 `mixin/support/` 中

### 7.3 移动文件

将 `wakame-mixin/.../damage/` 下的其他文件移到 `wakame-plugin/.../damage/`。

### 7.4 更新 MixinLivingEntity.java

```java
// Before
import cc.mewcraft.wakame.damage.DamageManagerApi;
// After
import cc.mewcraft.wakame.mixin.support.DamageManagerApi;
```

### 7.5 注入实现

在 `wakame-plugin` 中创建实现:

```kotlin
@Init(InitStage.PRE_WORLD)
object DamageManagerApiImpl : DamageManagerApi {
    @InitFun
    fun init() {
        DamageManagerApi.setImplementation(this)
    }
    // ... 实际实现
}
```

---

## 8. 注意事项

1. **严格遵循提示词范围**: 一次对话中，只迁移用户提示词明确指定的文件/目录。不要主动扩大范围，不要"顺便"迁移未提及的内容。发现需要额外迁移的依赖时，用 Bridge 接口隔离，留给后续对话。
2. **逐步迁移**: 每次只迁移一个目录，确保编译通过后再迁移下一个。迁移顺序应从**被依赖最少**的目录开始。
2. **Bridge 参数类型**: Bridge 接口的参数和返回类型只能是 mixin 可见的类型。如果需要传递复杂的业务类型，考虑用 `Any` + 强制转换（不推荐）或将关键数据类型的定义保留在 mixin 中。
3. **不要过度 Bridge**: 如果 `mixin/` 中没有代码引用某个待迁移类型，就**不需要**为它创建 Bridge。直接迁移即可。
4. **typealias 保留**: `util/` 中的 `typealias`（`MojangStack` 等）被广泛使用，优先保留在 `wakame-mixin` 中。
5. **纯接口暂不迁移**: 如果文件本身是接口且所有代码都面向该接口编程，可保留在 `wakame-mixin`，无需创建 Bridge（参见 5.5）。
5. **运行时时序**: Bridge 实现必须在 Mixin 代码首次调用之前注入。注意选择正确的 `InitStage`。
6. **测试**: 迁移后进行完整的服务端启动测试，确保 Mixin 在运行时能正确调用到 Bridge 实现。
