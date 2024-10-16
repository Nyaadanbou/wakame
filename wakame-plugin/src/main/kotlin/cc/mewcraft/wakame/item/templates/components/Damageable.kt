package cc.mewcraft.wakame.item.templates.components

import cc.mewcraft.wakame.item.component.*
import cc.mewcraft.wakame.item.template.*
import cc.mewcraft.wakame.util.*
import io.leangen.geantyref.TypeToken
import org.spongepowered.configurate.ConfigurationNode
import cc.mewcraft.wakame.item.components.Damageable as DamageableData


// 开发日记 2024/6/28
// 这个 disappearWhenBroken 并不会写入物品 NBT,
// 但可以通过物品的 ItemTemplateMap 获取该数据.
data class Damageable(
    /**
     * 初始损耗.
     */
    val damage: RandomizedValue,
    /**
     * 最大损耗.
     */
    val maxDamage: RandomizedValue,
    /**
     * 当物品的当前损耗值大于最大损耗值时, 物品是否消失?
     */
    val disappearWhenBroken: Boolean,
) : ItemTemplate<DamageableData> {
    override val componentType: ItemComponentType<DamageableData> = ItemComponentTypes.DAMAGEABLE

    override fun generate(context: ItemGenerationContext): ItemGenerationResult<DamageableData> {
        val damage = this.damage.calculate().toStableInt()
        val maxDamage = this.maxDamage.calculate().toStableInt()
        if (damage >= maxDamage) {
            ItemComponentInjections.logger.warn("Detected possible malformed item generation: 'minecraft:damage' >= 'minecraft:max_damage'. Template: $this, Context: $context")
        }
        return ItemGenerationResult.of(DamageableData(damage, maxDamage))
    }

    companion object: ItemTemplateBridge<Damageable> {
        override fun codec(id: String): ItemTemplateType<Damageable> {
            return Codec(id)
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemTemplateType<Damageable> {
        override val type: TypeToken<Damageable> = typeTokenOf()

        /**
         * ## Node structure
         * ```yaml
         * <node>:
         *   damage: <randomized_value>
         *   max_damage: <randomized_value>
         *   disappear_when_broken: <boolean>
         * ```
         */
        override fun decode(node: ConfigurationNode): Damageable {
            val damage = node.node("damage").krequire<RandomizedValue>()
            val maxDamage = node.node("max_damage").krequire<RandomizedValue>()
            val disappearWhenBroken = node.node("disappear_when_broken").krequire<Boolean>()
            return Damageable(damage, maxDamage, disappearWhenBroken)
        }
    }
}