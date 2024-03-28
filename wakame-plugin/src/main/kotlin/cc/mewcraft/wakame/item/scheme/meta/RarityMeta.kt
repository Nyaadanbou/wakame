package cc.mewcraft.wakame.item.scheme.meta

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.adventure.Keyed
import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext
import cc.mewcraft.wakame.item.scheme.meta.SchemeItemMeta.ResultUtil.nonGenerate
import cc.mewcraft.wakame.item.scheme.meta.SchemeItemMeta.ResultUtil.toMetaResult
import cc.mewcraft.wakame.rarity.LevelMappings
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.registry.LevelMappingRegistry
import cc.mewcraft.wakame.registry.RarityRegistry
import cc.mewcraft.wakame.util.requireKt
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type

/**
 * 物品的稀有度。
 */
sealed interface RarityMeta : SchemeItemMeta<Rarity> {
    companion object : Keyed {
        override val key: Key = Key.key(NekoNamespaces.ITEM_META, "rarity")
    }
}

private class NonNullRarityMeta(
    /**
     * The default rarity held in this scheme.
     */
    private val static: Rarity? = null,
    /**
     * The mappings used to generate the rarity.
     */
    private val dynamic: LevelMappings? = null,
) : RarityMeta {
    init {
        require(static != null || dynamic != null) { "static != null || dynamic != null" }
    }

    override fun generate(context: SchemeGenerationContext): SchemeItemMeta.Result<Rarity> {
        @Suppress("IfThenToElvis") // FUNKY IDE
        return if (static != null) {
            // use static rarity
            static.toMetaResult()
        } else if (dynamic != null) {
            // use dynamic rarity
            dynamic.pick(context.level, context.random).toMetaResult()
        } else {
            // fallback to the global rarity mappings
            LevelMappingRegistry.INSTANCES.get(LevelMappingRegistry.GLOBAL_NAME).pick(context.level, context.random).toMetaResult()
        }.also {
            context.rarities += it.value // leave trace to the context
        }
    }
}

private data object DefaultRarityMeta : RarityMeta {
    override fun generate(context: SchemeGenerationContext): SchemeItemMeta.Result<Rarity> = nonGenerate()
}

internal class RarityMetaSerializer : SchemeItemMetaSerializer<RarityMeta> {
    override val defaultValue: RarityMeta = DefaultRarityMeta
    override fun deserialize(type: Type, node: ConfigurationNode): RarityMeta {
        val string = node.requireKt<String>()
        val mappingPrefix = "mapping:"
        val rarityPrefix = "rarity:"
        when {
            string.startsWith(mappingPrefix) -> {
                return NonNullRarityMeta(
                    dynamic = LevelMappingRegistry.INSTANCES.get(string.substringAfter(mappingPrefix)),
                )
            }

            string.startsWith(rarityPrefix) -> {
                return NonNullRarityMeta(
                    static = RarityRegistry.INSTANCES.get(string.substringAfter(rarityPrefix))
                )
            }

            else -> {
                throw SerializationException("Can't parse rarity value '$string'")
            }
        }
    }
}