---
name: dev-enchantments
description: >
  Guide to the Koish custom enchantment framework. Covers architecture, key concepts,
  data flow, Bootstrap chain, effect types, and step-by-step instructions for adding
  new enchantment effects. Use this skill when working with enchantment code in
  wakame-plugin/src/main/kotlin/cc/mewcraft/wakame/enchantment/, or when asked to
  add, modify, or debug custom enchantments, enchantment effects, enchantment systems,
  or Codec/data component registration for enchantments.
---

# Koish 附魔框架指南

本技能指导你在 Koish (wakame) 项目中开发自定义附魔效果。

## 概述

Koish 附魔框架**完全复用 Minecraft 原版的附魔定义系统** ([Enchantment definition](https://minecraft.wiki/w/Enchantment_definition))。自定义附魔通过原版数据包 (datapack) 定义，其 `effects` 字段中可以使用 Koish 注册的自定义 enchantment effect component 类型。

**核心思路**: 附魔的"是什么"由数据包定义 (JSON)；附魔的"怎么运行"由 Java/Kotlin 代码实现。

## 魔咒是什么

### 玩家视角

在玩家眼中，一个**魔咒** (Enchantment) 就是一条可以附在物品上的属性/能力，例如"连锁采矿"、"爆破采矿"、"自动熔炼"。魔咒有名字、有等级上限、有适用的物品类型。

### 数据包视角 (一个 JSON = 一个魔咒)

每个魔咒对应数据包 `data/<namespace>/enchantment/` 目录下的**一个 JSON 文件**。JSON 遵循原版 [Enchantment definition](https://minecraft.wiki/w/Enchantment_definition#JSON_format) 格式。其中 `effects` 字段包含 **0 到 N 个 enchantment effect component**，键为效果 ID，值为该效果的配置数据。

### 代码视角 (Bukkit API)

一个魔咒对应 `org.bukkit.enchantments.Enchantment` 实例。项目中提供扩展函数 (`tools.kt`):

```kotlin
val effects: DataComponentMap = enchantment.getEffectList()
val smelter: EnchantmentSmelterEffect? = enchantment.getEffect(ExtraEnchantmentEffects.SMELTER)
val attributes: List<EnchantmentAttributeEffect> = enchantment.getEffectList(ExtraEnchantmentEffects.ATTRIBUTES)
val enchants: Map<Enchantment, Int> = itemStack.koishEnchantments
val customs: Map<Enchantment, Int> = itemStack.customEnchantments
```

### 魔咒 vs 魔咒效果

|概念|对应物|数量关系|
|---|---|---|
|魔咒 (Enchantment)|一个数据包 JSON 文件 / 一个 `org.bukkit.enchantments.Enchantment` 实例|—|
|魔咒效果 (Enchantment Effect)|JSON `effects` 中的一个键值对 / 一个 `DataComponentType<T>` 实例|一个魔咒拥有 0-N 个效果|

魔咒是面向玩家的实体 (有名字、有等级、可以附在物品上)；魔咒效果是面向开发者的实现单元 (一段可复用的效果逻辑)。

## 模块职责

|模块|附魔相关职责|
|---|---|
|`wakame-mixin`|Mixin 注入：`InvokerEnchantmentEffectComponents` 暴露 NMS 的 `register` 方法；`MixinEnchantmentEffectComponents` 在 `<clinit>` 时通过 Bridge 触发自定义效果组件的注册；`MixinEnchantment` 拦截 `isPrimaryItem`/`isSupportedItem`/`canEnchant` 以支持 Koish 物品的附魔兼容|
|`wakame-plugin`|所有附魔效果的数据类型定义、Codec、运行时组件、System 逻辑实现、初始化注册|

## 目录结构 (wakame-plugin)

```
enchantment/
├── tools.kt                    # 扩展函数
├── effect/                     # 附魔效果组件 (数据类型 + Codec)
│   ├── ExtraEnchantmentEffects.java       # 注册所有自定义 DataComponentType
│   ├── ExtraEnchantmentEffectsBootstrap.kt # Bridge 模式触发注册
│   ├── EnchantmentSpecialEffect.kt        # 标记接口: 特殊效果
│   ├── EnchantmentListenerBasedEffect.kt  # 标记接口: 基于 Listener 的效果
│   └── ...                                # 具体效果类
├── component/                  # 运行时数据 (MetadataMap 上的轻量对象)
│   └── ...
└── system/                     # 逻辑实现 (Listener 或 OnlineUserTicker)
    ├── EnchantmentEffectSystem.kt         # 每 tick 扫描物品槽, apply/remove
    ├── EnchantmentAttributeSystem.kt      # 每 tick 扫描物品槽, apply/remove 属性
    └── ...
```

## 防递归机制

挖掘类魔咒 (BlastMining, Veinminer, RangeMining) 使用 `ThreadLocal<Block>` 标记当前正在破坏的方块，防止 `player.breakBlock()` 再次触发 `BlockBreakEvent` 导致无限递归。

## 潜行取消约定

多数挖掘类魔咒约定: 玩家潜行 (`player.isSneaking`) 时不触发效果，允许玩家在需要精确挖掘时临时禁用魔咒效果。

## Koish 物品与附魔兼容

`MixinEnchantment` 拦截了原版 `Enchantment` 的 `isPrimaryItem`/`isSupportedItem`/`canEnchant` 方法。当物品为 Koish 物品时，通过 `KoishItemBridge` 委托给插件逻辑判断该物品是否支持特定附魔。

---

## 子文件索引

- `references/concepts-and-dataflow.md` — 关键概念（Effect Component、两类效果接口、Runtime Component、System）、数据流、注册时机 Bootstrap 链路、防递归机制详解
- `references/adding-effects.md` — 添加新效果的六步骤完整指南 + Codec 常用类型参考
- `references/effect-catalog.md` — 现有所有附魔效果一览表
