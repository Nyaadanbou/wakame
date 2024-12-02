package cc.mewcraft.wakame.item.templates.components

import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.ItemGenerationContext
import cc.mewcraft.wakame.item.template.ItemGenerationResult
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateBridge
import cc.mewcraft.wakame.item.template.ItemTemplateType
import cc.mewcraft.wakame.util.RandomizedValue
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.toStableInt
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import org.spongepowered.configurate.ConfigurationNode

data class ItemDamage(
    /**
     * 初始耐久损耗.
     */
    val damage: RandomizedValue,
) : ItemTemplate<Int> {
    override val componentType: ItemComponentType<Int> = ItemComponentTypes.DAMAGE

    override fun generate(context: ItemGenerationContext): ItemGenerationResult<Int> {
        val (value, _) = this.damage.calculate()
        val damage = value.toStableInt()
        return ItemGenerationResult.of(damage)
    }

    companion object : ItemTemplateBridge<ItemDamage> {
        override fun codec(id: String): ItemTemplateType<ItemDamage> {
            return Codec(id)
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemTemplateType<ItemDamage> {
        override val type: TypeToken<ItemDamage> = typeTokenOf()

        /**
         * ## Node structure
         * ```yaml
         * <node>: <randomized_value>
         * ```
         */
        override fun decode(node: ConfigurationNode): ItemDamage {
            val damage = node.krequire<RandomizedValue>()
            return ItemDamage(damage)
        }
    }
}