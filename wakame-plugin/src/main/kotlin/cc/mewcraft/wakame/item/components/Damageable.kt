package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.display.TooltipKey
import cc.mewcraft.wakame.display.TooltipProvider
import cc.mewcraft.wakame.item.ItemComponentConstants
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentInjections
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.item.template.GenerationResult
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateType
import cc.mewcraft.wakame.util.RandomizedValue
import cc.mewcraft.wakame.util.editMeta
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.toStableInt
import net.kyori.examination.Examinable
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type
import org.bukkit.inventory.meta.Damageable as CraftDamageable

interface Damageable : Examinable, TooltipProvider {

    /**
     * 当前损耗.
     */
    val damage: Int

    /**
     * 最大损耗.
     */
    val maxDamage: Int

    data class Value(
        override val damage: Int,
        override val maxDamage: Int,
    ) : Damageable {
        override fun provideDisplayLore(): LoreLine {
            if (!showInTooltip) {
                return LoreLine.noop()
            }
            return LoreLine.simple(tooltipKey, listOf(tooltipText.render()))
        }

        private companion object : ItemComponentConfig(ItemComponentConstants.DAMAGEABLE) {
            val tooltipKey: TooltipKey = ItemComponentConstants.createKey { DAMAGEABLE }
            val tooltipText: SingleTooltip = SingleTooltip()
        }
    }

    data class Codec(
        override val id: String,
    ) : ItemComponentType<Damageable, ItemComponentHolder.Complex> {
        override val holder: ItemComponentType.Holder = ItemComponentType.Holder.ITEM

        override fun read(holder: ItemComponentHolder.Complex): Damageable? {
            val itemMeta = (holder.item.itemMeta as? CraftDamageable) ?: return null
            return Value(
                damage = itemMeta.damage,
                maxDamage = itemMeta.maxDamage,
            )
        }

        override fun write(holder: ItemComponentHolder.Complex, value: Damageable) {
            holder.item.editMeta<CraftDamageable> { itemMeta ->
                itemMeta.damage = value.damage
                itemMeta.setMaxDamage(value.maxDamage)
            }
        }

        override fun remove(holder: ItemComponentHolder.Complex) {
            holder.item.editMeta<CraftDamageable> { itemMeta ->
                itemMeta.setMaxDamage(null)
            }
        }

        private companion object
    }

    data class Template(
        val damage: RandomizedValue,
        val maxDamage: RandomizedValue,
        // 开发日记 2024/6/28
        // 这个 disappearWhenBroken 并不会写入物品,
        // 但可以通过物品的 ItemTemplateMap 获取该数据.
        /**
         * 当物品的当前损耗值大于最大损耗值时, 物品是否消失?
         */
        val disappearWhenBroken: Boolean,
    ) : ItemTemplate<Value> {
        override fun generate(context: GenerationContext): GenerationResult<Value> {
            val damage = this.damage.calculate().toStableInt()
            val maxDamage = this.maxDamage.calculate().toStableInt()
            if (damage >= maxDamage) {
                ItemComponentInjections.logger.warn("Detected possible malformed item generation: 'minecraft:damage' >= 'minecraft:max_damage'. Template: $this, Context: $context")
            }
            return GenerationResult.of(Value(damage, maxDamage))
        }

        companion object : ItemTemplateType<Template> {
            /**
             * ## Node structure
             * ```yaml
             * <root>:
             *   damage: <randomized_value>
             *   max_damage: <randomized_value>
             *   disappear_when_broken: <boolean>
             * ```
             */
            override fun deserialize(type: Type, node: ConfigurationNode): Template {
                val damage = node.node("damage").krequire<RandomizedValue>()
                val maxDamage = node.node("max_damage").krequire<RandomizedValue>()
                val disappearWhenBroken = node.node("disappear_when_broken").krequire<Boolean>()
                return Template(damage, maxDamage, disappearWhenBroken)
            }
        }
    }
}