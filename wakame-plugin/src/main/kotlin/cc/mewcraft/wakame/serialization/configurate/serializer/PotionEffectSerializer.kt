package cc.mewcraft.wakame.serialization.configurate.serializer

import cc.mewcraft.lazyconfig.configurate.SimpleSerializer
import cc.mewcraft.lazyconfig.configurate.require
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

/*internal*/ object PotionEffectSerializer : SimpleSerializer<PotionEffect?> {
    override fun deserialize(type: Type, node: ConfigurationNode): PotionEffect? {
        if (node.raw() == null)
            return null

        val effectType = node.node("type").require<PotionEffectType>()
        val duration = node.node("duration").getInt(1)
        val amplifier = node.node("amplifier").getInt(0)
        val ambient = node.node("ambient").getBoolean(false)
        val particles = node.node("particles").getBoolean(true)
        val icon = node.node("icon").getBoolean(true)

        return PotionEffect(effectType, duration, amplifier, ambient, particles, icon)
    }
}