package cc.mewcraft.wakame.serialization.configurate.serializer

import cc.mewcraft.wakame.serialization.configurate.TypeSerializer2
import cc.mewcraft.wakame.util.require
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

/*internal*/ object PotionEffectSerializer : TypeSerializer2<PotionEffect?> {
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

// /*internal*/ object PotionEffectTypeSerializer : TypeSerializer<PotionEffectType?> {
//     override fun deserialize(type: Type, node: ConfigurationNode): PotionEffectType? {
//         if (node.raw() == null)
//             return null
//
//         return Registry.POTION_EFFECT_TYPE.get(node.get<NamespacedKey>()!!)
//             ?: throw SerializationException(node, type, "No such potion type: ${node.raw()}")
//     }
//
//     override fun serialize(type: Type, obj: PotionEffectType?, node: ConfigurationNode) {
//         if (obj == null) {
//             node.raw(null)
//             return
//         }
//
//         node.set(obj.key)
//     }
// }
