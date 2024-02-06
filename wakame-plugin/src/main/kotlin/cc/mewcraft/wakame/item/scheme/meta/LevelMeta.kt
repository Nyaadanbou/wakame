package cc.mewcraft.wakame.item.scheme.meta

import cc.mewcraft.wakame.SchemeSerializer
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
    private val level: Any,
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
        }
    }

    companion object : Keyed {
        override fun key(): Key = Key.key(SchemeMeta.ITEM_META_NAMESPACE, "level")

        const val ADVENTURE_LEVEL_STRING = "ADVENTURE_LEVEL"
        const val EXPERIENCE_LEVEL_STRING = "EXPERIENCE_LEVEL"

        val LEVEL_OPTIONS = setOf(ADVENTURE_LEVEL_STRING, EXPERIENCE_LEVEL_STRING)
    }
}

internal class LevelMetaSerializer : SchemeSerializer<LevelMeta> {
    override fun deserialize(type: Type, node: ConfigurationNode): LevelMeta {
        val scalar = node.rawScalar()
        require(
            scalar is Int || (scalar is String && scalar in LevelMeta.LEVEL_OPTIONS)
        )
        return LevelMeta(scalar!!)
    }
}