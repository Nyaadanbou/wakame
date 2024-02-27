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
 *
 * @property level the item level
 */
data class LevelMeta(
    /**
     * The item level held in this scheme.
     */
    private val level: Any = 1,
) : KoinComponent, SchemeItemMeta<Int> {

    private val adventureLevelGetter: PlayerLevelGetter by inject<PlayerLevelGetter>(named(CUSTOM_ADVENTURE_LEVEL))
    private val experienceLevelGetter: PlayerLevelGetter by inject<PlayerLevelGetter>(named(VANILLA_EXPERIENCE_LEVEL))

    override fun generate(context: SchemeGenerationContext): Int {
        val ret: Int = when (level) {
            is Int -> {
                return level
            }

            is LevelOption -> {
                when (level) {
                    LevelOption.CRATE_LEVEL -> context.crateObject?.level
                    LevelOption.ADVENTURE_LEVEL -> context.playerObject?.let(adventureLevelGetter::get)
                    LevelOption.EXPERIENCE_LEVEL -> context.playerObject?.let(experienceLevelGetter::get)
                } ?: 1 // returns level 1 if we can't get the expected level
            }

            else -> throw IllegalStateException("Something wrong with the ${LevelMetaSerializer::class.simpleName}")
        }

        return ret
            .coerceAtLeast(1) // by design, level never goes down below 1
            .also {
                context.itemLevel = it // leave trace to the context
            }
    }

    enum class LevelOption {
        CRATE_LEVEL,
        ADVENTURE_LEVEL,
        EXPERIENCE_LEVEL,
    }

    companion object : Keyed {
        override fun key(): Key = Key.key(NekoNamespaces.ITEM_META, "level")
    }
}

internal class LevelMetaSerializer : SchemeItemMetaSerializer<LevelMeta> {
    override val emptyValue: LevelMeta = LevelMeta()

    override fun deserialize(type: Type, node: ConfigurationNode): LevelMeta {
        return when (val scalar = node.rawScalar()) {
            is Int -> LevelMeta(scalar)

            is String -> LevelMeta(EnumLookup.lookup<LevelMeta.LevelOption>(scalar).getOrThrow())

            else -> throw SerializationException("Invalid value for ${LevelMeta::class.simpleName}")
        }
    }
}