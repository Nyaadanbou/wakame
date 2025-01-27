package cc.mewcraft.wakame.item.templates.components

import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.ShownInTooltip
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.*
import cc.mewcraft.wakame.util.javaTypeOf
import cc.mewcraft.wakame.util.require
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import net.kyori.adventure.key.InvalidKeyException
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import cc.mewcraft.wakame.item.components.ItemEnchantments as ItemEnchantmentsData


data class ItemEnchantments(
    override val componentType: ItemComponentType<ItemEnchantmentsData>,
    val enchantments: Map<Key, Int>,
    override val showInTooltip: Boolean,
) : ItemTemplate<ItemEnchantmentsData>, ShownInTooltip {
    override fun generate(context: ItemGenerationContext): ItemGenerationResult<ItemEnchantmentsData> {
        val enchantments = this.enchantments.mapKeys { (key, _) ->
            val registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT)
            registry.get(key) ?: throw IllegalArgumentException("Unknown enchantment: '$key'")
        }

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
                .mapKeys { (key, _) ->
                    try {
                        Key.key(key.toString())
                    } catch (e: InvalidKeyException) {
                        throw SerializationException(node, javaTypeOf<ItemEnchantments>(), "Malformed enchantment key: '$key'", e)
                    }
                }
                .mapValues { (_, node) ->
                    node.require<Int>()
                }
            val showInTooltip = node.node("show_in_tooltip").getBoolean(true)

            return when (id) {
                ItemConstants.ENCHANTMENTS -> {
                    ItemEnchantments(ItemComponentTypes.ENCHANTMENTS, enchantments, showInTooltip)
                }

                ItemConstants.STORED_ENCHANTMENTS -> {
                    ItemEnchantments(ItemComponentTypes.STORED_ENCHANTMENTS, enchantments, showInTooltip)
                }

                else -> {
                    throw IllegalArgumentException("Unknown template id: '$id'")
                }
            }
        }
    }
}