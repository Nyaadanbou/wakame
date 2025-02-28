@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.item.templates.components

import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.*
import cc.mewcraft.wakame.util.require
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.tag.TagKey
import net.kyori.adventure.key.Key
import org.bukkit.damage.DamageType
import org.spongepowered.configurate.ConfigurationNode
import cc.mewcraft.wakame.item.components.DamageResistant as DamageResistantData


data class DamageResistant(
    val types: TagKey<DamageType>,
) : ItemTemplate<DamageResistantData> {
    override val componentType: ItemComponentType<DamageResistantData> = ItemComponentTypes.DAMAGE_RESISTANT

    override fun generate(context: ItemGenerationContext): ItemGenerationResult<DamageResistantData> {
        return ItemGenerationResult.of(DamageResistantData(this.types))
    }

    companion object : ItemTemplateBridge<DamageResistant> {
        override fun codec(id: String): ItemTemplateType<DamageResistant> {
            return Codec(id)
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemTemplateType<DamageResistant> {
        override val type: TypeToken<DamageResistant> = typeTokenOf()

        /**
         * ## Node structure
         * ```yaml
         * <node>: <tag key>
         * ```
         */
        override fun decode(node: ConfigurationNode): DamageResistant {
            val type = node.require<String>()
            val tagKey = TagKey.create(RegistryKey.DAMAGE_TYPE, Key.key(type))

            return DamageResistant(tagKey)
        }
    }
}