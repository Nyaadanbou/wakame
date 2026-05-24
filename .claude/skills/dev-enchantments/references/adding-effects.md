# 添加新附魔效果的步骤

本文档提供添加新附魔效果的完整步骤指南和 Codec 常用类型参考。

---

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

---

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
