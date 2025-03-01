package cc.mewcraft.wakame.item.templates.components

import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.ShownInTooltip
import cc.mewcraft.wakame.item.template.*
import cc.mewcraft.wakame.serialization.configurate.extension.transformKeys
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.paper.getValueOrThrow
import cc.mewcraft.wakame.util.require
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.TypedKey
import org.bukkit.enchantments.Enchantment
import org.spongepowered.configurate.ConfigurationNode
import cc.mewcraft.wakame.item.components.ItemEnchantments as ItemEnchantmentsData


data class ItemEnchantments(
    val enchantments: Map<TypedKey<Enchantment>, Int>,
    override val componentType: ItemComponentType<ItemEnchantmentsData>,
    override val showInTooltip: Boolean,
) : ItemTemplate<ItemEnchantmentsData>, ShownInTooltip {
    override fun generate(context: ItemGenerationContext): ItemGenerationResult<ItemEnchantmentsData> {
        val enchantments = this.enchantments.mapKeys { (key, _) -> key.getValueOrThrow() }
        return ItemGenerationResult.of(ItemEnchantmentsData(enchantments, this.showInTooltip))
    }

    companion object : ItemTemplateBridge<ItemEnchantments> {
        override fun codec(id: String): ItemTemplateType<ItemEnchantments> {
            return Codec(id)
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemTemplateType<ItemEnchantments> {
        override val type: TypeToken<ItemEnchantments> = typeTokenOf()

        /**
         * ## Node structure
         * ```yaml
         * <node>:
         *   show_in_tooltip: <boolean>
         *   entries:
         *     <key>: <level>
         *     <key>: <level>
         * ```
         */
        override fun decode(node: ConfigurationNode): ItemEnchantments {
            val enchantments = node.node("entries").childrenMap()
                .transformKeys<Identifier>()
                .mapKeys { (id, _) -> TypedKey.create(RegistryKey.ENCHANTMENT, id) }
                .mapValues { (_, node) -> node.require<Int>() }
            val showInTooltip = node.node("show_in_tooltip").getBoolean(true)

            return when (id) {
                ItemConstants.ENCHANTMENTS -> {
                    ItemEnchantments(enchantments, ItemComponentTypes.ENCHANTMENTS, showInTooltip)
                }

                ItemConstants.STORED_ENCHANTMENTS -> {
                    ItemEnchantments(enchantments, ItemComponentTypes.STORED_ENCHANTMENTS, showInTooltip)
                }

                else -> {
                    throw IllegalArgumentException("Unknown template id: '$id'")
                }
            }
        }
    }
}