package cc.mewcraft.wakame.item.scheme.meta

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.adventure.Keyed
import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext
import cc.mewcraft.wakame.item.scheme.meta.SchemeItemMeta.ResultUtil.nonGenerate
import cc.mewcraft.wakame.item.scheme.meta.SchemeItemMeta.ResultUtil.toMetaResult
import cc.mewcraft.wakame.util.EnumLookup
import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type

/**
 * 物品的等级。
 */
sealed interface LevelMeta : SchemeItemMeta<Int> {
    companion object : Keyed {
        override val key: Key = Key.key(NekoNamespaces.ITEM_META, "level")
    }

    enum class Option {
        CONTEXT
    }
}

/**
 * 物品的等级。
 *
 * 物品等级目前支持两种：固定等级，动态等级。
 *
 * # 固定等级
 * 直接在配置文件中指定好一个常数，然后每次都按照该常数生成等级。
 *
 * # 动态等级
 * 由生成的上下文决定要生成的等级。
 */
private class NonNullLevelMeta(
    /**
     * The item level held in this scheme.
     */
    private val level: Any,
) : KoinComponent, LevelMeta {
    override fun generate(context: SchemeGenerationContext): SchemeItemMeta.Result<Int> {
        val ret: Int = when (level) {
            is Int -> {
                return level.toMetaResult()
            }

            is LevelMeta.Option -> {
                when (level) {
                    LevelMeta.Option.CONTEXT -> context.trigger.level
                }
            }

            else -> throw IllegalStateException("Something wrong with the ${LevelMetaSerializer::class.simpleName}")
        }

        return ret
            .coerceAtLeast(0) // by design, level never goes down below 0
            .also { context.level = it } // populate the context with generated level
            .toMetaResult()
    }
}

private data object DefaultLevelMeta : LevelMeta {
    override fun generate(context: SchemeGenerationContext): SchemeItemMeta.Result<Int> = nonGenerate() // default not to write level at all
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