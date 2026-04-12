---
applyTo: "wakame-plugin/src/**/enchantment/**"
---

# Koish 附魔框架指南

本文档描述 Koish 项目中的自定义附魔框架的整体架构、关键概念和开发范式。

## 概述

Koish 附魔框架**完全复用 Minecraft 原版的附魔定义系统** ([Enchantment definition](https://minecraft.wiki/w/Enchantment_definition))。自定义附魔通过原版数据包 (datapack) 定义，其 `effects` 字段中可以使用 Koish 注册的自定义 enchantment effect component 类型。框架在代码层面扩展了原版的 `EnchantmentEffectComponents` 注册表，使数据包 JSON 中的 `effects` 能够反序列化为 Koish 自定义的数据类型。

**核心思路**: 附魔的"是什么"由数据包定义 (JSON)；附魔的"怎么运行"由 Java/Kotlin 代码实现。

## 模块职责

|模块|附魔相关职责|
|---|---|
|`wakame-mixin`|Mixin 注入：`InvokerEnchantmentEffectComponents` 暴露 NMS 的 `register` 方法；`MixinEnchantmentEffectComponents` 在 `<clinit>` 时通过 Bridge 触发自定义效果组件的注册；`MixinEnchantment` 拦截 `isPrimaryItem`/`isSupportedItem`/`canEnchant` 以支持 Koish 物品的附魔兼容|
|`wakame-plugin`|所有附魔效果的数据类型定义、Codec、运行时组件、System 逻辑实现、初始化注册|

## 目录结构 (wakame-plugin)

```
enchantment/
├── tools.kt                    # 扩展函数: getEffectList, getListenerBasedEffects, koishEnchantments 等
├── effect/                     # 附魔效果组件 (数据类型 + Codec，从数据包 JSON 反序列化)
│   ├── ExtraEnchantmentEffects.java       # 注册所有自定义 DataComponentType
│   ├── ExtraEnchantmentEffectsBootstrap.kt # 通过 Bridge 模式在 BOOTSTRAP 阶段触发注册
│   ├── EnchantmentSpecialEffect.kt        # 标记接口: 特殊效果 (如 Attribute)
│   ├── EnchantmentListenerBasedEffect.kt  # 标记接口: 基于 Listener 的效果 (apply/remove)
│   ├── EnchantmentAttributeEffect.kt      # 具体效果: 自定义属性
│   ├── EnchantmentSmelterEffect.kt        # 具体效果: 自动熔炼
│   ├── EnchantmentBlastMiningEffect.kt    # 具体效果: 爆破采矿
│   └── ...                                # 其他效果
├── component/                  # 运行时数据 (存储在玩家 MetadataMap 上的轻量对象)
│   ├── Smelter.kt
│   ├── BlastMining.kt
│   ├── Veinminer.kt
│   ├── VeinminerChild.kt      # 一次连锁挖矿的遍历状态
│   ├── RangeMining.kt
│   ├── RangeMiningChild.kt    # 一次范围挖掘的执行状态
│   └── ...
└── system/                     # 逻辑实现 (Listener 或 OnlineUserTicker)
    ├── EnchantmentEffectSystem.kt         # 核心: 每 tick 扫描物品槽变化, apply/remove 所有 ListenerBasedEffect
    ├── EnchantmentAttributeSystem.kt      # 核心: 每 tick 扫描物品槽变化, apply/remove 属性效果
    ├── EnchantmentSmelterSystem.kt        # Listener: 监听 BlockDropItemEvent
    ├── EnchantmentBlastMiningSystem.kt    # Listener: 监听 BlockBreakEvent + EntityExplodeEvent
    ├── EnchantmentVeinminerSystem.kt      # Listener: 监听 BlockBreakEvent, BFS 连锁挖矿
    ├── EnchantmentRangeMiningSystem.kt    # Listener: 监听挖掘事件, 范围破坏
    └── ...
```

## 关键概念

### 1. Enchantment Effect Component (附魔效果组件)

与 Minecraft 原版的 enchantment effect component 概念等价 (参考 [Enchantment definition](https://minecraft.wiki/w/Enchantment_definition))。每个自定义效果组件是一个注册到 `EnchantmentEffectComponents` 注册表中的 `DataComponentType<T>`，其中 `T` 是一个带有 `Codec` 的数据类。

注册在 `ExtraEnchantmentEffects.java` 中进行，例如:
```java
public static final DataComponentType<EnchantmentSmelterEffect> SMELTER = register(
    "koish:smelter", builder -> builder.persistent(EnchantmentSmelterEffect.CODEC)
);
```

注册后，数据包 JSON 中可以这样使用:
```json
{
  "effects": {
    "koish:smelter": { "disable_on_crouch": true, "sound": "minecraft:block.lava.extinguish", ... }
  }
}
```

### 2. 两类效果接口

|接口|用途|代表|
|---|---|---|
|`EnchantmentListenerBasedEffect`|基于事件监听器的效果。提供 `apply`/`remove` 方法，在物品槽变化时将运行时数据写入/移除玩家的 `MetadataMap`|Smelter, BlastMining, Veinminer, RangeMining, Fragile, AntigravShot, VoidEscape, AutoReplant|
|`EnchantmentSpecialEffect`|特殊效果，有独立的 apply/remove 逻辑，不走统一的 `EnchantmentEffectSystem`|Attribute (直接操作 AttributeMap)|

### 3. Runtime Component (运行时组件)

`component/` 目录下的类是轻量的运行时数据对象，存储在玩家的 `MetadataMap` 上。它们由 effect 的 `apply` 方法创建，在 `remove` 时移除。System 通过 `MetadataKey` 读取这些组件来决定是否执行逻辑。

例如 `BlastMining` 组件包含 `explosionPower` 和 `minBlockHardness`，由 `EnchantmentBlastMiningEffect.apply()` 在物品槽变化时写入，由 `EnchantmentBlastMiningSystem` 在 `BlockBreakEvent` 中读取。

部分效果还有 "Child" 组件 (如 `VeinminerChild`, `RangeMiningChild`)，用于跟踪一次效果执行的中间状态 (BFS 队列、已访问集合等)。

### 4. System (系统)

System 是效果的实际逻辑执行者，分两种:

- **`OnlineUserTicker`**: 每 tick 执行，在 `ServerOnlineUserTicker` 中调用。
  - `EnchantmentEffectSystem`: 扫描物品槽变化，对所有 `EnchantmentListenerBasedEffect` 调用 `apply`/`remove`
  - `EnchantmentAttributeSystem`: 扫描物品槽变化，对 `EnchantmentAttributeEffect` 调用 `apply`/`remove`
- **`Listener`**: 监听 Bukkit 事件，在 `EnchantmentInitializer` 中注册。
  - 每个 Listener System 从玩家的 `MetadataMap` 读取对应的运行时组件，无组件则跳过 (等价于玩家没有该魔咒)

## 数据流

```
数据包 JSON (effects 字段)
  ↓ Minecraft Codec 反序列化
Effect 数据类 (如 EnchantmentSmelterEffect)
  ↓ EnchantmentEffectSystem.onTickUser() 检测物品槽变化
  ↓ 调用 effect.apply(player, level, slot)
Runtime Component (如 Smelter) 写入 player.metadata()
  ↓ System Listener 监听事件时读取
执行具体逻辑 (如 BlockDropItemEvent → 替换掉落物为熔炼产物)
```

## 注册时机 (Bootstrap 链路)

```
服务器启动 → NMS EnchantmentEffectComponents.<clinit>
  ↓ MixinEnchantmentEffectComponents (Mixin 注入)
  ↓ 调用 ExtraEnchantmentEffectsRegistrar.Impl.bootstrap()
  ↓ Bridge 委托到 wakame-plugin 的 ExtraEnchantmentEffectsRegistrarImpl
  ↓ ExtraEnchantmentEffects.bootstrap() → 初始化所有静态 DataComponentType 字段
```

这确保自定义效果组件在 NMS 注册表初始化期间就被注册，早于数据包加载。

## 现有附魔效果一览

|效果 ID|Effect 类|Component 类|System 类|类型|说明|
|---|---|---|---|---|---|
|`koish:attributes`|`EnchantmentAttributeEffect`|—|`EnchantmentAttributeSystem`|Special|提供自定义属性修饰器|
|`koish:smelter`|`EnchantmentSmelterEffect`|`Smelter`|`EnchantmentSmelterSystem`|Listener|挖掘方块后自动熔炼掉落物|
|`koish:blast_mining`|`EnchantmentBlastMiningEffect`|`BlastMining`|`EnchantmentBlastMiningSystem`|Listener|挖掘产生爆炸，破坏范围内方块|
|`koish:fragile`|`EnchantmentFragileEffect`|`Fragile`|`EnchantmentFragileSystem`|Listener|物品耐久度消耗倍率增加|
|`koish:veinminer`|`EnchantmentVeinminerEffect`|`Veinminer` + `VeinminerChild`|`EnchantmentVeinminerSystem`|Listener|BFS 连锁采矿同种矿物|
|`koish:antigrav_shot`|`EnchantmentAntigravShotEffect`|`AntigravShot`|`EnchantmentAntigravShotSystem`|Listener|射出的弹射物不受重力|
|`koish:void_escape`|`EnchantmentVoidEscapeEffect`|`VoidEscape`|`EnchantmentVoidEscapeSystem`|Listener|虚空伤害时随机传送到安全位置|
|`koish:range_mining`|`EnchantmentRangeMiningEffect`|`RangeMining` + `RangeMiningChild`|`EnchantmentRangeMiningSystem`|Listener|范围挖掘 (宽×高×深)|
|`koish:auto_replant`|`EnchantmentAutoReplantEffect`|`AutoReplant`|`EnchantmentAutoReplantSystem`|Listener|右键成熟作物自动收获并补种|

## 添加新附魔效果的步骤

### 第一步: 定义 Effect 数据类

在 `effect/` 下创建数据类，实现 `EnchantmentListenerBasedEffect` (或 `EnchantmentSpecialEffect`)，并定义 `Codec` 和 `MetadataKey`:

```kotlin
@JvmRecord
data class EnchantmentMyEffect(
    val power: LevelBasedValue,
) : EnchantmentListenerBasedEffect {
    companion object {
        @JvmField
        val DATA_KEY: MetadataKey<MyComponent> = metadataKey("enchantment:my_effect")

        @JvmField
        val CODEC: Codec<EnchantmentMyEffect> = RecordCodecBuilder.create { instance ->
            instance.group(
                LevelBasedValue.CODEC.fieldOf("power").forGetter(EnchantmentMyEffect::power)
            ).apply(instance, ::EnchantmentMyEffect)
        }
    }

    override fun apply(entity: LivingEntity, level: Int, slot: ItemSlot) {
        entity.metadata().put(DATA_KEY, MyComponent(power.calculate(level)))
    }

    override fun remove(entity: LivingEntity, level: Int, slot: ItemSlot) {
        entity.metadata().remove(DATA_KEY)
    }
}
```

**关键规范**:
- Codec 中的字段名使用 **snake_case** (与数据包 JSON 键一致)
- `LevelBasedValue` 用于支持按附魔等级计算的数值 (原版类型，支持 `constant`/`linear`/`clamped` 等)
- 无参数的效果使用 `data object` + `MapCodec.unitCodec(this)` (参考 `EnchantmentAntigravShotEffect`)

### 第二步: 定义 Runtime Component

在 `component/` 下创建轻量类，包含 System 运行时需要的数据:

```kotlin
class MyComponent(
    val power: Float,
)
```

如需跟踪执行状态 (如 BFS 队列)，创建额外的 "Child" 类。

### 第三步: 注册 DataComponentType

在 `ExtraEnchantmentEffects.java` 中添加:

```java
public static final DataComponentType<EnchantmentMyEffect> MY_EFFECT = register(
    "koish:my_effect", builder -> builder.persistent(EnchantmentMyEffect.CODEC)
);
```

> 注意: `ExtraEnchantmentEffects` 使用 Java 编写，因为它直接调用 Mixin 的 `InvokerEnchantmentEffectComponents`。

### 第四步: 实现 System

在 `system/` 下创建 `object`，实现 `Listener`:

```kotlin
object EnchantmentMyEffectSystem : Listener {
    @EventHandler
    fun on(event: SomeEvent) {
        val player = ...
        val myComponent = player.metadata().getOrNull(EnchantmentMyEffect.DATA_KEY) ?: return
        // 执行逻辑
    }
}
```

### 第五步: 注册 System

在 `init/enchantment.kt` 的 `EnchantmentInitializer.init()` 中注册 Listener:

```kotlin
EnchantmentMyEffectSystem.registerEvents()
```

如果 System 是 `OnlineUserTicker`，则在 `ServerOnlineUserTicker` 中添加调用。

### 第六步: 创建数据包 JSON

在数据包的 `data/<namespace>/enchantment/` 下创建 JSON 文件:

```json
{
  "description": { "translate": "enchantment.koish.my_effect", "fallback": "我的效果" },
  "supported_items": "#minecraft:enchantable/mining",
  "slots": ["mainhand"],
  "max_level": 3,
  "min_cost": { "base": 1, "per_level_above_first": 10 },
  "max_cost": { "base": 51, "per_level_above_first": 10 },
  "anvil_cost": 1,
  "weight": 10,
  "effects": {
    "koish:my_effect": {
      "power": { "type": "minecraft:linear", "base": 1.0, "per_level_above_first": 0.5 }
    }
  }
}
```

## Codec 常用类型

|用途|Codec|说明|
|---|---|---|
|按等级计算的数值|`LevelBasedValue.CODEC`|NMS 原版类型，支持 `constant`/`linear`/`clamped` 等|
|布尔值|`Codec.BOOL`|`optionalFieldOf("key", defaultValue)` 提供默认值|
|长整数|`Codec.LONG`|同上|
|Adventure Key|`AdventureCodecs.KEY_WITH_MINECRAFT_NAMESPACE`|无命名空间时默认 `minecraft:`|
|Material (物品)|`PaperCodecs.MATERIAL_ITEM`|Paper 物品材质|
|Material (方块)|`PaperCodecs.MATERIAL_BLOCK`|Paper 方块材质|
|Set|`.setOf()`|`PaperCodecs.MATERIAL_BLOCK.setOf()` 产生 `Set<Material>`|
|Map|`Codec.unboundedMap(keyCodec, valueCodec)`|键值对映射|
|属性|`KoishCodecs.ATTRIBUTE`|Koish 自定义属性|
|属性操作|`KoishCodecs.ATTRIBUTE_MODIFIER_OPERATION`|`add`/`multiply_base`/`multiply_total`|

## 防递归机制

挖掘类魔咒 (BlastMining, Veinminer, RangeMining) 通过 `player.breakBlock()` 触发方块破坏，这会再次触发 `BlockBreakEvent`。为防止无限递归，每个 System 使用 `ThreadLocal<Block>` 标记当前正在破坏的方块:

```kotlin
@JvmStatic
val runningBlock: ThreadLocal<Block> = ThreadLocal()

// 在事件处理中检查
if (block == runningBlock.get()) return
if (block == EnchantmentVeinminerSystem.runningBlock.get()) return
if (block == EnchantmentBlastMiningSystem.runningBlock.get()) return

// 在破坏方块前后设置/清除
runningBlock.set(block)
player.breakBlock(block)
runningBlock.remove()
```

## 潜行取消约定

多数挖掘类魔咒约定: 玩家潜行 (`player.isSneaking`) 时不触发效果。这是一个 UX 设计，允许玩家在需要精确挖掘时临时禁用魔咒效果。

## Koish 物品与附魔兼容

`MixinEnchantment` 拦截了原版 `Enchantment` 的 `isPrimaryItem`/`isSupportedItem`/`canEnchant` 方法。当物品为 Koish 物品时，通过 `KoishItemBridge` 委托给插件逻辑判断该物品是否支持特定附魔，而非使用原版的 `supported_items` 标签判断。

