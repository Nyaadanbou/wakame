package cc.mewcraft.wakame.item.scheme.meta

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.rarity.LevelMappings
import cc.mewcraft.wakame.registry.LevelMappingRegistry
import cc.mewcraft.wakame.registry.RarityRegistry
import cc.mewcraft.wakame.util.requireKt
import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Keyed
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type

/**
 * 物品的稀有度。
 *
 * @property static the static item rarity
 * @property dynamic the dynamic item rarity
 */
class RarityMeta(
    /**
     * The default rarity held in this scheme.
     */
    private val static: Rarity? = null,
    /**
     * The mappings used to generate the rarity.
     */
    private val dynamic: LevelMappings? = null,
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
            // fallback to the global rarity mappings
            LevelMappingRegistry.getOrThrow(LevelMappingRegistry.GLOBAL_NAME).pick(context.itemLevel)
        }.also {
            context.rarities += it // leave trace to the context
        }
    }

    companion object : Keyed {
        override fun key(): Key = Key.key(NekoNamespaces.META, "rarity")
    }
}

internal class RarityMetaSerializer : SchemeMetaSerializer<RarityMeta> {
    override val emptyValue: RarityMeta = RarityMeta()

    override fun deserialize(type: Type, node: ConfigurationNode): RarityMeta {
        val string = node.requireKt<String>()
        val mappingPrefix = "mapping:"
        val rarityPrefix = "rarity:"
        when {
            string.startsWith(mappingPrefix) -> {
                return RarityMeta(
                    dynamic = LevelMappingRegistry.getOrThrow(string.substringAfter(mappingPrefix)),
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