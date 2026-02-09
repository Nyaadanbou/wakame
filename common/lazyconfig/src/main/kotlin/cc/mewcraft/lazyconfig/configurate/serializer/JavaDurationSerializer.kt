package cc.mewcraft.lazyconfig.configurate.serializer

import cc.mewcraft.lazyconfig.configurate.SimpleSerializer
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import java.lang.reflect.Type
import kotlin.time.toJavaDuration
import kotlin.time.toKotlinDuration
import java.time.Duration as JavaDuration
import kotlin.time.Duration.Companion as KotlinDuration

object JavaDurationSerializer : SimpleSerializer<JavaDuration> {

    override fun deserialize(type: Type, node: ConfigurationNode): JavaDuration? {
        val raw = node.get<String>() ?: return null
        return try {
            KotlinDuration.parse(raw).toJavaDuration()
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to parse Duration from string: $raw", e)
        }
    }

    override fun serialize(type: Type, obj: JavaDuration?, node: ConfigurationNode) {
        if (obj == null) {
            node.set(null)
        } else {
            node.set(obj.toKotlinDuration().toString())
        }
    }
}