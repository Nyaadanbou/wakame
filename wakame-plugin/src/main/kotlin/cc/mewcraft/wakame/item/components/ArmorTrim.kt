package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.item.template.GenerationResult
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateType
import cc.mewcraft.wakame.util.editMeta
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import org.bukkit.inventory.meta.ArmorMeta
import org.bukkit.inventory.meta.trim.TrimMaterial
import org.bukkit.inventory.meta.trim.TrimPattern
import org.spongepowered.configurate.ConfigurationNode

data class ArmorTrim(
    val pattern: TrimPattern,
    val material: TrimMaterial,
    val showInTooltip: Boolean,
) : Examinable {

    companion object : ItemComponentBridge<ArmorTrim> {
        override fun codec(id: String): ItemComponentType<ArmorTrim> {
            return Codec(id)
        }

        override fun templateType(id: String): ItemTemplateType<Template> {
            return TemplateType(id)
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<ArmorTrim> {
        override fun read(holder: ItemComponentHolder): ArmorTrim? {
            val im = holder.item.itemMeta as? ArmorMeta ?: return null
            val trim = im.trim ?: return null

            val pattern = trim.pattern
            val material = trim.material
            val showInTooltip = !im.hasItemFlag(org.bukkit.inventory.ItemFlag.HIDE_ARMOR_TRIM)

            return ArmorTrim(pattern, material, showInTooltip)
        }

        override fun write(holder: ItemComponentHolder, value: ArmorTrim) {
            holder.item.editMeta<ArmorMeta> {
                it.trim = org.bukkit.inventory.meta.trim.ArmorTrim(value.material, value.pattern)
                if (value.showInTooltip) {
                    it.removeItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ARMOR_TRIM)
                } else {
                    it.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ARMOR_TRIM)
                }
            }
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.item.editMeta<ArmorMeta> {
                it.trim = null
            }
        }
    }

    data class Template(
        val pattern: TrimPattern,
        val material: TrimMaterial,
        val showInTooltip: Boolean,
    ) : ItemTemplate<ArmorTrim> {
        override val componentType: ItemComponentType<ArmorTrim> = ItemComponentTypes.TRIM

        override fun generate(context: GenerationContext): GenerationResult<ArmorTrim> {
            return GenerationResult.of(ArmorTrim(pattern, material, showInTooltip))
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
         *   pattern: <key>
         *   material: <key>
         *   show_in_tooltip: <boolean>
         * ```
         */
        override fun decode(node: ConfigurationNode): Template {
            val patternKey = node.node("pattern").krequire<Key>()
            val materialKey = node.node("material").krequire<Key>()
            val pattern = RegistryAccess.registryAccess().getRegistry(RegistryKey.TRIM_PATTERN).get(patternKey) ?: throw IllegalArgumentException("Unknown trim pattern key: '$patternKey'")
            val material = RegistryAccess.registryAccess().getRegistry(RegistryKey.TRIM_MATERIAL).get(materialKey) ?: throw IllegalArgumentException("Unknown trim material key: '$materialKey'")
            val showInTooltip = node.node("show_in_tooltip").getBoolean(true)

            return Template(pattern, material, showInTooltip)
        }
    }
}