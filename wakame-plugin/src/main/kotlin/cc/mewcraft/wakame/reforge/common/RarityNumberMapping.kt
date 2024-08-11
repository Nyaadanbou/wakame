package cc.mewcraft.wakame.reforge.common

import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type
import java.util.stream.Stream

/**
 * 用于将 [稀有度][cc.mewcraft.wakame.rarity.Rarity] 映射成浮点数.
 */
interface RarityNumberMapping : Examinable {
    /**
     * 获取指定稀有度的浮点数值. 如果不存在则返回 `0.0`.
     */
    fun get(key: Key): Double
}

/**
 * [RarityNumberMapping] 的一般实现.
 */
class SimpleRarityNumberMapping(
    private val map: Map<Key, Double>,
) : RarityNumberMapping {
    override fun get(key: Key): Double {
        return map[key] ?: 0.0
    }

    override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.of(
        ExaminableProperty.of("map", map)
    )

    override fun toString(): String =
        toSimpleString()
}

/**
 * [RarityNumberMapping] 的序列化器.
 */
internal object RarityNumberMappingSerializer : TypeSerializer<RarityNumberMapping> {
    override fun deserialize(
        type: Type,
        node: ConfigurationNode,
    ): RarityNumberMapping {
        val map = node.childrenMap()
            .mapKeys { Key.key(Namespaces.ELEMENT, it.key.toString()) }
            .mapValues { it.value.double }
        val ret = SimpleRarityNumberMapping(map)

        return ret
    }
}