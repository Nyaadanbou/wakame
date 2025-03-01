package cc.mewcraft.wakame.item.templates.components

import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.ShownInTooltip
import cc.mewcraft.wakame.item.template.*
import cc.mewcraft.wakame.serialization.configurate.typeserializer.requireTypedKey
import cc.mewcraft.wakame.util.paper.getValueOrThrow
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.TypedKey
import org.bukkit.inventory.meta.trim.TrimMaterial
import org.bukkit.inventory.meta.trim.TrimPattern
import org.spongepowered.configurate.ConfigurationNode
import cc.mewcraft.wakame.item.components.ArmorTrim as ArmorTrimData


data class ArmorTrim(
    val pattern: TypedKey<TrimPattern>,
    val material: TypedKey<TrimMaterial>,
    override val showInTooltip: Boolean,
) : ItemTemplate<ArmorTrimData>, ShownInTooltip {
    override val componentType: ItemComponentType<ArmorTrimData> = ItemComponentTypes.TRIM

    override fun generate(context: ItemGenerationContext): ItemGenerationResult<ArmorTrimData> {
        val trimPattern = pattern.getValueOrThrow()
        val trimMaterial = material.getValueOrThrow()
        return ItemGenerationResult.of(ArmorTrimData(trimPattern, trimMaterial, showInTooltip))
    }

    companion object : ItemTemplateBridge<ArmorTrim> {
        override fun codec(id: String): ItemTemplateType<ArmorTrim> {
            return Codec(id)
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemTemplateType<ArmorTrim> {
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
            val pattern = node.node("pattern").requireTypedKey(RegistryKey.TRIM_PATTERN)
            val material = node.node("material").requireTypedKey(RegistryKey.TRIM_MATERIAL)
            val showInTooltip = node.node("show_in_tooltip").getBoolean(true)

            return ArmorTrim(pattern, material, showInTooltip)
        }
    }
}