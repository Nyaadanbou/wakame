package cc.mewcraft.wakame.item.templates.components

import cc.mewcraft.wakame.item.ShownInTooltip
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.*
import cc.mewcraft.wakame.util.require
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import net.kyori.adventure.key.Key
import org.bukkit.inventory.meta.trim.TrimMaterial
import org.bukkit.inventory.meta.trim.TrimPattern
import org.spongepowered.configurate.ConfigurationNode
import cc.mewcraft.wakame.item.components.ArmorTrim as ArmorTrimData


data class ArmorTrim(
    val pattern: TrimPattern,
    val material: TrimMaterial,
    override val showInTooltip: Boolean,
) : ItemTemplate<ArmorTrimData>, ShownInTooltip {
    override val componentType: ItemComponentType<ArmorTrimData> = ItemComponentTypes.TRIM

    override fun generate(context: ItemGenerationContext): ItemGenerationResult<ArmorTrimData> {
        return ItemGenerationResult.of(ArmorTrimData(pattern, material, showInTooltip))
    }

    companion object : ItemTemplateBridge<ArmorTrim> {
        override fun codec(id: String): ItemTemplateType<ArmorTrim> {
            return Codec(id)
        }
    }

    private data class Codec(
        override val id: String,
    ): ItemTemplateType<ArmorTrim> {
        override val type: TypeToken<ArmorTrim> = typeTokenOf()

        /**
         * ## Node structure
         * ```yaml
         * <node>:
         *   pattern: <key>
         *   material: <key>
         *   show_in_tooltip: <boolean>
         * ```
         */
        override fun decode(node: ConfigurationNode): ArmorTrim {
            val patternKey = node.node("pattern").require<Key>()
            val materialKey = node.node("material").require<Key>()
            val pattern = RegistryAccess.registryAccess().getRegistry(RegistryKey.TRIM_PATTERN).get(patternKey) ?: throw IllegalArgumentException("Unknown trim pattern key: '$patternKey'")
            val material = RegistryAccess.registryAccess().getRegistry(RegistryKey.TRIM_MATERIAL).get(materialKey) ?: throw IllegalArgumentException("Unknown trim material key: '$materialKey'")
            val showInTooltip = node.node("show_in_tooltip").getBoolean(true)

            return ArmorTrim(pattern, material, showInTooltip)
        }
    }
}