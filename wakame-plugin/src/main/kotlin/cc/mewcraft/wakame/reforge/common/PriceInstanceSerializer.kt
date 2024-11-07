package cc.mewcraft.wakame.reforge.common

import cc.mewcraft.wakame.config.configurate.TypeSerializer
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import java.lang.reflect.Type

internal object PriceInstanceSerializer : TypeSerializer<PriceInstance> {
    override fun deserialize(type: Type, node: ConfigurationNode): PriceInstance {
        // 如果配置文件里只有 base, 那么 min_base = max_base = base
        // 如果配置文件里没有 base, 那么 min_base 和 max_base 按照自己的来
        // 如果都不存在, 那么 min_base = max_base = 0
        val base = node.node("base").get<Double>(.0)
        val minBase = node.node("min_base").get<Double>(base)
        val maxBase = node.node("max_base").get<Double>(base)

        // 该反序列化要求 ObjectMapper 已经存在于 TypeSerializerCollection 里
        val modifiers = node.node("modifiers").get<Map<String, PriceModifier>>(emptyMap())

        return PriceInstance(minBase, maxBase, modifiers)
    }
}