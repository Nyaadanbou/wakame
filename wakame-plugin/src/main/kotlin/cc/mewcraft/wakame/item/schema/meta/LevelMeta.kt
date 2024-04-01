package cc.mewcraft.wakame.item.schema.meta

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.adventure.Keyed
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import cc.mewcraft.wakame.util.EnumLookup
import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type

/**
 * 物品的等级。
 */
sealed interface SLevelMeta : SchemaItemMeta<Int> {
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
     * The item level held in this schema.
     */
    private val level: Any,
) : KoinComponent, SLevelMeta {
    override fun generate(context: SchemaGenerationContext): GenerationResult<Int> {
        val ret: Int = when (level) {
            is Int -> {
                return GenerationResult(level)
            }

            is SLevelMeta.Option -> {
                when (level) {
                    SLevelMeta.Option.CONTEXT -> context.trigger.level
                }
            }

            else -> throw IllegalStateException("Something wrong with the ${LevelMetaSerializer::class.simpleName}")
        }

        return ret
            .coerceAtLeast(0) // by design, level never goes down below 0
            .also { context.level = it } // populate the context with generated level
            .let(::GenerationResult)
    }
}

private data object DefaultLevelMeta : SLevelMeta {
    override fun generate(context: SchemaGenerationContext): GenerationResult<Int> = GenerationResult.empty() // default not to write level at all
}

internal class LevelMetaSerializer : SchemaItemMetaSerializer<SLevelMeta> {
    override val defaultValue: SLevelMeta = DefaultLevelMeta
    override fun deserialize(type: Type, node: ConfigurationNode): SLevelMeta {
        return when (val scalar = node.rawScalar()) {
            is Int -> NonNullLevelMeta(scalar)

            is String -> NonNullLevelMeta(EnumLookup.lookup<SLevelMeta.Option>(scalar).getOrThrow())

            else -> throw SerializationException("Invalid value type for ${SLevelMeta::class.simpleName}")
        }
    }
}