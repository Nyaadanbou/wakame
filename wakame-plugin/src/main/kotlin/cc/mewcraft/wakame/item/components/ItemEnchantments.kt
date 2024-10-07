package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.item.template.GenerationResult
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateType
import cc.mewcraft.wakame.util.editMeta
import cc.mewcraft.wakame.util.javaTypeOf
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import net.kyori.adventure.key.InvalidKeyException
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException

data class ItemEnchantments(
    val enchantments: Map<Enchantment, Int>,
    val showInTooltip: Boolean,
) : Examinable {
    companion object : ItemComponentBridge<ItemEnchantments> {
        override fun codec(id: String): ItemComponentType<ItemEnchantments> {
            return when (id) {
                ItemConstants.ENCHANTMENTS -> CodecForEnchantments(id)
                ItemConstants.STORED_ENCHANTMENTS -> CodecForStoredEnchantments(id)
                else -> throw IllegalArgumentException("Unknown codec id: '$id'")
            }
        }

        override fun templateType(id: String): ItemTemplateType<Template> {
            return TemplateType(id)
        }
    }

    private data class CodecForEnchantments(
        override val id: String,
    ) : ItemComponentType<ItemEnchantments> {
        override fun read(holder: ItemComponentHolder): ItemEnchantments? {
            val im = holder.item.itemMeta ?: return null
            val enchantments = im.enchants
            val showInTooltip = !im.hasItemFlag(ItemFlag.HIDE_ENCHANTS)
            return ItemEnchantments(enchantments, showInTooltip)
        }

        override fun write(holder: ItemComponentHolder, value: ItemEnchantments) {
            holder.item.editMeta {
                for ((enchantment, level) in value.enchantments) {
                    it.addEnchant(enchantment, level, true)
                }
                if (value.showInTooltip) {
                    it.removeItemFlags(ItemFlag.HIDE_ENCHANTS)
                } else {
                    it.addItemFlags(ItemFlag.HIDE_ENCHANTS)
                }
            }
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.item.removeEnchantments()
        }
    }

    private data class CodecForStoredEnchantments(
        override val id: String,
    ) : ItemComponentType<ItemEnchantments> {
        override fun read(holder: ItemComponentHolder): ItemEnchantments? {
            val im = holder.item.itemMeta as? EnchantmentStorageMeta ?: return null
            val enchantments = im.enchants
            val showInTooltip = !im.hasItemFlag(ItemFlag.HIDE_STORED_ENCHANTS)
            return ItemEnchantments(enchantments, showInTooltip)
        }

        override fun write(holder: ItemComponentHolder, value: ItemEnchantments) {
            holder.item.editMeta<EnchantmentStorageMeta> {
                for ((storedEnchant, _) in it.storedEnchants) {
                    it.removeStoredEnchant(storedEnchant)
                }
                for ((enchantment, level) in value.enchantments) {
                    it.addStoredEnchant(enchantment, level, true)
                }
                if (value.showInTooltip) {
                    it.removeItemFlags(ItemFlag.HIDE_STORED_ENCHANTS)
                } else {
                    it.addItemFlags(ItemFlag.HIDE_STORED_ENCHANTS)
                }
            }
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.item.editMeta<EnchantmentStorageMeta> {
                for ((storedEnchant, _) in it.storedEnchants) {
                    it.removeStoredEnchant(storedEnchant)
                }
            }
        }
    }

    data class Template(
        override val componentType: ItemComponentType<ItemEnchantments>,
        val enchantments: Map<Key, Int>,
        val showInTooltip: Boolean,
    ) : ItemTemplate<ItemEnchantments> {
        override fun generate(context: GenerationContext): GenerationResult<ItemEnchantments> {
            val enchantments = this.enchantments.mapKeys { (key, _) ->
                val registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT)
                registry.get(key) ?: throw IllegalArgumentException("Unknown enchantment: '$key'")
            }
            return GenerationResult.of(ItemEnchantments(enchantments, this.showInTooltip))
        }
    }

    private data class TemplateType(
        override val id: String,
    ) : ItemTemplateType<Template> {
        override val type: TypeToken<Template> = typeTokenOf()

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
        override fun decode(node: ConfigurationNode): Template {
            val enchantments = node.node("entries").childrenMap()
                .mapKeys { (key, _) ->
                    try {
                        Key.key(key.toString())
                    } catch (e: InvalidKeyException) {
                        throw SerializationException(node, javaTypeOf<Template>(), "Malformed enchantment key: '$key'", e)
                    }
                }
                .mapValues { (_, node) ->
                    node.krequire<Int>()
                }
            val showInTooltip = node.node("show_in_tooltip").getBoolean(true)
            return when (id) {
                ItemConstants.ENCHANTMENTS -> {
                    Template(ItemComponentTypes.ENCHANTMENTS, enchantments, showInTooltip)
                }

                ItemConstants.STORED_ENCHANTMENTS -> {
                    Template(ItemComponentTypes.STORED_ENCHANTMENTS, enchantments, showInTooltip)
                }

                else -> {
                    throw IllegalArgumentException("Unknown template id: '$id'")
                }
            }
        }
    }
}