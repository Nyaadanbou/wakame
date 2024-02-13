package cc.mewcraft.wakame.item.scheme.meta

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext
import cc.mewcraft.wakame.util.toStableInt
import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Keyed
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

/**
 * 物品的等级。
 *
 * @property level the item level
 */
class LevelMeta(
    /**
     * The item level held in this scheme.
     */
    private val level: Any = 1,
) : SchemeMeta<Int> {
    override fun generate(context: SchemeGenerationContext): Int {
        return when (level) {
            is Int -> {
                return level.toStableInt()
            }

            is String -> {
                when (level) {
                    EXPERIENCE_LEVEL_STRING -> context.playerLevel
                    ADVENTURE_LEVEL_STRING -> context.playerLevel + 1 // TODO actually reads the player's adventure level
                    else -> throw IllegalStateException("Impossible")
                }
            }

            else -> {
                throw IllegalStateException("Impossible")
            }
        }.also {
            context.itemLevel = it // leave trace to the context
        }
    }

    companion object : Keyed {
        override fun key(): Key = Key.key(NekoNamespaces.META, "level")

        const val ADVENTURE_LEVEL_STRING = "ADVENTURE_LEVEL"
        const val EXPERIENCE_LEVEL_STRING = "EXPERIENCE_LEVEL"
        val LEVEL_OPTIONS: Set<String> = setOf(ADVENTURE_LEVEL_STRING, EXPERIENCE_LEVEL_STRING)
    }
}

internal class LevelMetaSerializer : SchemeMetaSerializer<LevelMeta> {
    override val emptyValue: LevelMeta = LevelMeta()

    override fun deserialize(type: Type, node: ConfigurationNode): LevelMeta {
        val scalar = node.rawScalar()
        require(
            scalar is Int || (scalar is String && scalar in LevelMeta.LEVEL_OPTIONS)
        )
        return LevelMeta(scalar!!)
    }
}