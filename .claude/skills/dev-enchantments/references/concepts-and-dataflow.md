# 附魔框架关键概念与数据流

本文件详细描述 Koish 附魔框架的关键概念、数据流和注册时机。概述、目录结构和注意事项见 `SKILL.md`。

---

## Enchantment Effect Component (附魔效果组件)

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

## 两类效果接口

|接口|用途|代表|
|---|---|---|
|`EnchantmentListenerBasedEffect`|基于事件监听器的效果。提供 `apply`/`remove` 方法，在物品槽变化时将运行时数据写入/移除玩家的 `MetadataMap`|Smelter, BlastMining, Veinminer, RangeMining, Fragile, AntigravShot, VoidEscape, AutoReplant|
|`EnchantmentSpecialEffect`|特殊效果，有独立的 apply/remove 逻辑，不走统一的 `EnchantmentEffectSystem`|Attribute (直接操作 AttributeMap)|

## Runtime Component (运行时组件)

`component/` 目录下的类是轻量的运行时数据对象，存储在玩家的 `MetadataMap` 上。它们由 effect 的 `apply` 方法创建，在 `remove` 时移除。System 通过 `MetadataKey` 读取这些组件来决定是否执行逻辑。

例如 `BlastMining` 组件包含 `explosionPower` 和 `minBlockHardness`，由 `EnchantmentBlastMiningEffect.apply()` 在物品槽变化时写入，由 `EnchantmentBlastMiningSystem` 在 `BlockBreakEvent` 中读取。

部分效果还有 "Child" 组件 (如 `VeinminerChild`, `RangeMiningChild`)，用于跟踪一次效果执行的中间状态 (BFS 队列、已访问集合等)。

## System (系统)

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
