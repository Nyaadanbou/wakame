package cc.mewcraft.wakame.item.schema.meta

import cc.mewcraft.wakame.item.ItemMetaKeys
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import cc.mewcraft.wakame.rarity.LevelMappings
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.registry.LevelMappingRegistry
import cc.mewcraft.wakame.registry.RarityRegistry
import cc.mewcraft.wakame.util.krequire
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type

/**
 * 物品的稀有度。
 */
sealed interface SRarityMeta : SchemaItemMeta<Rarity> {
    override val key: Key get() = ItemMetaKeys.RARITY
}

private class NonNullRarityMeta(
    /**
     * The default rarity held in this schema.
     */
    private val static: Rarity? = null,
    /**
     * The mappings used to generate the rarity.
     */
    private val dynamic: LevelMappings? = null,
) : SRarityMeta {
    init {
        require(static != null || dynamic != null) { "static != null || dynamic != null" }
    }

    override fun generate(context: SchemaGenerationContext): GenerationResult<Rarity> {
        @Suppress("IfThenToElvis")
        val rarity = if (static != null) {
            // use static rarity
            static
        } else if (dynamic != null) {
            // use dynamic rarity
            dynamic.pick(context.level, context.random)
        } else {
            // fallback to the global rarity mappings
            LevelMappingRegistry.INSTANCES[LevelMappingRegistry.GLOBAL_NAME].pick(context.level, context.random)
        }.also {
            // leave trace to the context
            context.rarities += it
        }
        return GenerationResult(rarity)
    }
}

private data object DefaultRarityMeta : SRarityMeta {
    override fun generate(context: SchemaGenerationContext): GenerationResult<Rarity> = GenerationResult.empty()
}

internal data object RarityMetaSerializer : SchemaItemMetaSerializer<SRarityMeta> {
    override val defaultValue: SRarityMeta = DefaultRarityMeta
    override fun deserialize(type: Type, node: ConfigurationNode): SRarityMeta {
        val string = node.krequire<String>()
        val mappingPrefix = "mapping:"
        val rarityPrefix = "rarity:"
        when {
            string.startsWith(mappingPrefix) -> {
                return NonNullRarityMeta(
                    dynamic = LevelMappingRegistry.INSTANCES[string.substringAfter(mappingPrefix)],
                )
            }

            string.startsWith(rarityPrefix) -> {
                return NonNullRarityMeta(
                    static = RarityRegistry.INSTANCES[string.substringAfter(rarityPrefix)]
                )
            }

            else -> {
                throw SerializationException("Can't parse rarity value '$string'")
            }
        }
    }
}