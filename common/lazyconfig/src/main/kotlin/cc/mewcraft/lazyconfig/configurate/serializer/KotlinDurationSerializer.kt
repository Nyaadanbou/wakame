package cc.mewcraft.lazyconfig.configurate.serializer

import cc.mewcraft.lazyconfig.configurate.SimpleSerializer
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import java.lang.reflect.Type
import kotlin.time.Duration

// FIXME KotlinDuration 如果声明在 data class 里会被 inline 成 Long 类型
object KotlinDurationSerializer : SimpleSerializer<Duration> {

    override fun deserialize(type: Type, node: ConfigurationNode): Duration? {
        val raw = node.get<String>() ?: return null
        return try {
            Duration.parse(raw)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Failed to parse Duration from string: $raw", e)
        }
    }

    override fun serialize(type: Type, obj: Duration?, node: ConfigurationNode) {
        if (obj == null) {
            node.set(null)
        } else {
            node.set(obj.toString())
        }
    }
}