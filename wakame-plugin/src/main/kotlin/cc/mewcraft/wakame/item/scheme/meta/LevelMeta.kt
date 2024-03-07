package cc.mewcraft.wakame.item.scheme.meta

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext
import cc.mewcraft.wakame.level.CUSTOM_ADVENTURE_LEVEL
import cc.mewcraft.wakame.level.PlayerLevelGetter
import cc.mewcraft.wakame.level.VANILLA_EXPERIENCE_LEVEL
import cc.mewcraft.wakame.util.EnumLookup
import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Keyed
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type

/**
 * 物品的等级。
 */
sealed interface LevelMeta : SchemeItemMeta<Int> {
    companion object : Keyed {
        override fun key(): Key = Key.key(NekoNamespaces.ITEM_META, "level")
    }

    enum class Option {
        CRATE_LEVEL,
        ADVENTURE_LEVEL,
        EXPERIENCE_LEVEL,
    }
}

/**
 * 物品的等级。
 */
private class NonNullLevelMeta(
    /**
     * The item level held in this scheme.
     */
    private val level: Any,
) : KoinComponent, LevelMeta {

    private val customLevelGetter: PlayerLevelGetter by inject<PlayerLevelGetter>(named(CUSTOM_ADVENTURE_LEVEL))
    private val vanillaLevelGetter: PlayerLevelGetter by inject<PlayerLevelGetter>(named(VANILLA_EXPERIENCE_LEVEL))

    override fun generate(context: SchemeGenerationContext): Int {
        val ret: Int = when (level) {
            is Int -> {
                return level
            }

            is LevelMeta.Option -> {
                when (level) {
                    LevelMeta.Option.CRATE_LEVEL -> context.crateObject?.level
                    LevelMeta.Option.ADVENTURE_LEVEL -> context.playerObject?.let(customLevelGetter::get)
                    LevelMeta.Option.EXPERIENCE_LEVEL -> context.playerObject?.let(vanillaLevelGetter::get)
                } ?: 1 // returns level 1 if we can't get the expected level
            }

            else -> throw IllegalStateException("Something wrong with the ${LevelMetaSerializer::class.simpleName}")
        }

        return ret
            .coerceAtLeast(1) // by design, level never goes down below 1
            .also {
                context.level = it // leave trace to the context
            }
    }
}

private data object DefaultLevelMeta : LevelMeta {
    override fun generate(context: SchemeGenerationContext): Int? = null // default not to write level at all
}

internal class LevelMetaSerializer : SchemeItemMetaSerializer<LevelMeta> {
    override val defaultValue: LevelMeta = DefaultLevelMeta
    override fun deserialize(type: Type, node: ConfigurationNode): LevelMeta {
        return when (val scalar = node.rawScalar()) {
            is Int -> NonNullLevelMeta(scalar)

            is String -> NonNullLevelMeta(EnumLookup.lookup<LevelMeta.Option>(scalar).getOrThrow())

            else -> throw SerializationException("Invalid value type for ${LevelMeta::class.simpleName}")
        }
    }
}