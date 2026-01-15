package cc.mewcraft.wakame.reforge.common

import cc.mewcraft.lazyconfig.configurate.SimpleSerializer
import cc.mewcraft.wakame.util.adventure.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.ConfigurationOptions
import java.lang.reflect.Type
import java.util.stream.Stream

/**
 * 用于将 [稀有度][cc.mewcraft.wakame.rarity.Rarity] 映射成浮点数.
 */
fun interface RarityNumberMapping : Examinable {
    companion object {
        /**
         * 创建一个永远返回固定值的 [RarityNumberMapping] 实例.
         */
        fun constant(value: Double): RarityNumberMapping {
            return ConstantRarityNumberMapping(value)
        }

        /**
         * 创建一个标准的 [RarityNumberMapping] 实例, 用于从配置文件构建.
         */
        fun simple(map: Map<Key, Double>): RarityNumberMapping {
            return SimpleRarityNumberMapping(map)
        }
    }

    /**
     * 获取指定稀有度对应的数值. 如果不存在则返回 `0.0`.
     */
    fun get(key: Key): Double
}

/**
 * [RarityNumberMapping] 的序列化器.
 */
internal object RarityNumberMappingSerializer : SimpleSerializer<RarityNumberMapping> {
    override fun deserialize(
        type: Type,
        node: ConfigurationNode,
    ): RarityNumberMapping {
        // FIXME: 稀有度不要 Namespace
        val map = node.childrenMap()
            .mapKeys { Key.key("rarity", it.key.toString()) }
            .mapValues { it.value.double }
        val ret = SimpleRarityNumberMapping(map)

        return ret
    }

    override fun emptyValue(specificType: Type, options: ConfigurationOptions): RarityNumberMapping? {
        return EmptyRarityNumberMapping
    }
}

private object EmptyRarityNumberMapping : RarityNumberMapping {
    override fun get(key: Key): Double = 0.0
    override fun toString(): String = toSimpleString()
}

private class ConstantRarityNumberMapping(
    val constant: Double,
) : RarityNumberMapping {
    override fun get(key: Key): Double {
        return constant
    }

    override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.of(
        ExaminableProperty.of("constant", constant)
    )

    override fun toString(): String = toSimpleString()
}

private class SimpleRarityNumberMapping(
    private val data: Map<Key, Double>,
) : RarityNumberMapping {
    override fun get(key: Key): Double {
        return data[key] ?: 0.0
    }

    override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.of(
        ExaminableProperty.of("data", data)
    )

    override fun toString(): String = toSimpleString()
}
