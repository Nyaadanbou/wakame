package cc.mewcraft.wakame.config.configurate

import cc.mewcraft.wakame.SchemaSerializer
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.typeTokenOf
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.ScalarSerializer
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type
import java.util.function.Predicate

internal object PotionEffectTypeSerializer : ScalarSerializer<PotionEffectType>(typeTokenOf()) {
    override fun deserialize(type: Type, obj: Any): PotionEffectType {
        val key = NamespacedKey.minecraft(obj.toString())
        return Registry.EFFECT.get(key) ?: throw SerializationException(type, "Can't find potion effect type with key '$key'")
    }

    override fun serialize(item: PotionEffectType, typeSupported: Predicate<Class<*>>?): Any {
        return item.key.value()
    }
}

internal object PotionEffectSerializer : SchemaSerializer<PotionEffect> {
    override fun deserialize(type: Type, node: ConfigurationNode): PotionEffect {
        val effectType = node.node("type").krequire<PotionEffectType>()
        val duration = node.node("duration").getInt(1)
        val amplifier = node.node("amplifier").getInt(0)
        val ambient = node.node("ambient").getBoolean(false)
        val particles = node.node("particles").getBoolean(true)
        val icon = node.node("icon").getBoolean(true)

        return PotionEffect(effectType, duration, amplifier, ambient, particles, icon)
    }
}
