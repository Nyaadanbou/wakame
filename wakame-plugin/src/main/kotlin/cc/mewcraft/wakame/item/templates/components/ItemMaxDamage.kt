package cc.mewcraft.wakame.item.templates.components

import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.*
import cc.mewcraft.wakame.util.RandomizedValue
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.toStableInt
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import org.spongepowered.configurate.ConfigurationNode


data class ItemMaxDamage(
    /**
     * 最大耐久损耗.
     */
    val maxDamage: RandomizedValue
) : ItemTemplate<Int> {
    override val componentType: ItemComponentType<Int> = ItemComponentTypes.MAX_DAMAGE

    override fun generate(context: ItemGenerationContext): ItemGenerationResult<Int> {
        val maxDamage = this.maxDamage.calculate().toStableInt()
        return ItemGenerationResult.of(maxDamage)
    }

    companion object: ItemTemplateBridge<ItemMaxDamage> {
        override fun codec(id: String): ItemTemplateType<ItemMaxDamage> {
            return Codec(id)
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemTemplateType<ItemMaxDamage> {
        override val type: TypeToken<ItemMaxDamage> = typeTokenOf()

        /**
         * ## Node structure
         * ```yaml
         * <node>: <randomized_value>
         * ```
         */
        override fun decode(node: ConfigurationNode): ItemMaxDamage {
            val maxDamage = node.krequire<RandomizedValue>()
            return ItemMaxDamage(maxDamage)
        }
    }
}