package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.display.TooltipKey
import cc.mewcraft.wakame.display.TooltipProvider
import cc.mewcraft.wakame.item.ItemComponentConstants
import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentInjections
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.item.template.GenerationResult
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateType
import cc.mewcraft.wakame.util.RandomizedValue
import cc.mewcraft.wakame.util.editMeta
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.toStableInt
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import net.kyori.examination.Examinable
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type
import org.bukkit.inventory.meta.Damageable as CraftDamageable

data class Damageable(
    /**
     * 当前损耗.
     */
    val damage: Int,

    /**
     * 最大损耗.
     */
    val maxDamage: Int,
) : Examinable, TooltipProvider.Single {

    companion object : ItemComponentBridge<Damageable>, ItemComponentConfig(ItemComponentConstants.DAMAGEABLE) {
        private val tooltipKey: TooltipKey = ItemComponentConstants.createKey { DAMAGEABLE }
        private val tooltipText: SingleTooltip = SingleTooltip()

        override fun codec(id: String): ItemComponentType<Damageable> {
            return Codec(id)
        }

        override fun templateType(): ItemTemplateType<Damageable> {
            return TemplateType
        }
    }

    override fun provideTooltipLore(): LoreLine {
        if (!showInTooltip) {
            return LoreLine.noop()
        }
        return LoreLine.simple(tooltipKey, listOf(tooltipText.render()))
    }

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<Damageable> {
        override fun read(holder: ItemComponentHolder): Damageable? {
            val itemMeta = (holder.item.itemMeta as? CraftDamageable) ?: return null
            return Damageable(
                damage = itemMeta.damage,
                maxDamage = itemMeta.maxDamage,
            )
        }

        override fun write(holder: ItemComponentHolder, value: Damageable) {
            holder.item.editMeta<CraftDamageable> { itemMeta ->
                itemMeta.damage = value.damage
                itemMeta.setMaxDamage(value.maxDamage)
            }
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.item.editMeta<CraftDamageable> { itemMeta ->
                itemMeta.setMaxDamage(null)
            }
        }

        private companion object
    }

    // 开发日记 2024/6/28
    // 这个 disappearWhenBroken 并不会写入物品 NBT,
    // 但可以通过物品的 ItemTemplateMap 获取该数据.
    private data class Template(
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
    ) : ItemTemplate<Damageable> {
        override val componentType: ItemComponentType<Damageable> = ItemComponentTypes.DAMAGEABLE

        override fun generate(context: GenerationContext): GenerationResult<Damageable> {
            val damage = this.damage.calculate().toStableInt()
            val maxDamage = this.maxDamage.calculate().toStableInt()
            if (damage >= maxDamage) {
                ItemComponentInjections.logger.warn("Detected possible malformed item generation: 'minecraft:damage' >= 'minecraft:max_damage'. Template: $this, Context: $context")
            }
            return GenerationResult.of(Damageable(damage, maxDamage))
        }
    }

    private data object TemplateType : ItemTemplateType<Damageable> {
        override val typeToken: TypeToken<ItemTemplate<Damageable>> = typeTokenOf()

        /**
         * ## Node structure
         * ```yaml
         * <node>:
         *   damage: <randomized_value>
         *   max_damage: <randomized_value>
         *   disappear_when_broken: <boolean>
         * ```
         */
        override fun deserialize(type: Type, node: ConfigurationNode): ItemTemplate<Damageable> {
            val damage = node.node("damage").krequire<RandomizedValue>()
            val maxDamage = node.node("max_damage").krequire<RandomizedValue>()
            val disappearWhenBroken = node.node("disappear_when_broken").krequire<Boolean>()
            return Template(damage, maxDamage, disappearWhenBroken)
        }
    }
}