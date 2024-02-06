package cc.mewcraft.wakame.item.scheme.meta

import cc.mewcraft.wakame.SchemeSerializer
import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.rarity.RarityMappings
import cc.mewcraft.wakame.registry.RarityMappingRegistry
import cc.mewcraft.wakame.registry.RarityRegistry
import cc.mewcraft.wakame.util.typedRequire
import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Keyed
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type

/**
 * 物品的稀有度。
 *
 * @property static the item rarity
 */
class RarityMeta(
    /**
     * The mappings used to generate the rarity.
     */
    private val dynamic: RarityMappings? = null,
    /**
     * The default rarity held in this scheme.
     */
    private val static: Rarity? = null,
) : SchemeMeta<Rarity> {
    override fun generate(context: SchemeGenerationContext): Rarity {
        @Suppress("IfThenToElvis") // FUNKY IDE
        return if (static != null) {
            // use static rarity
            static
        } else if (dynamic != null) {
            // use dynamic rarity
            dynamic.pick(context.itemLevel)
        } else {
            // fallback to global rarity mappings
            RarityMappingRegistry.getOrThrow(RarityMappingRegistry.GLOBAL_RARITY_MAPPING_NAME).pick(context.itemLevel)
        }.also {
            context.rarities += it // leave trace to the context
        }
    }

    companion object : Keyed {
        override fun key(): Key = Key.key(SchemeMeta.ITEM_META_NAMESPACE, "rarity")
    }
}

internal class RarityMetaSerializer : SchemeSerializer<RarityMeta> {
    override fun deserialize(type: Type, node: ConfigurationNode): RarityMeta {
        if (node.virtual()) { // make it optional
            return RarityMeta()
        }

        val string = node.typedRequire<String>()
        val mappingPrefix = "mapping:"
        val rarityPrefix = "rarity:"
        when {
            string.startsWith(mappingPrefix) -> {
                return RarityMeta(
                    dynamic = RarityMappingRegistry.getOrThrow(string.substringAfter(mappingPrefix)),
                )
            }

            string.startsWith(rarityPrefix) -> {
                return RarityMeta(
                    static = RarityRegistry.getOrThrow(string.substringAfter(rarityPrefix))
                )
            }

            else -> {
                throw SerializationException("Can't parse rarity config $string")
            }
        }
    }
}