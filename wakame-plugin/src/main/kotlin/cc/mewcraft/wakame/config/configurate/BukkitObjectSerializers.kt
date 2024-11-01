package cc.mewcraft.wakame.config.configurate

import cc.mewcraft.wakame.SchemaSerializer
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.typeTokenOf
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.damage.DamageType
import org.bukkit.entity.EntityType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.ScalarSerializer
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type
import java.util.function.Predicate

internal object MaterialSerializer : ScalarSerializer<Material>(typeTokenOf()) {
    override fun deserialize(type: Type, obj: Any): Material {
        val name = obj.toString()
        val material = Material.matchMaterial(name) ?: throw SerializationException(type, "Can't parse material '$name'")
        return material
    }

    override fun serialize(item: Material, typeSupported: Predicate<Class<*>>?): Any {
        return item.key().asString()
    }
}

internal object PotionEffectTypeSerializer : ScalarSerializer<PotionEffectType>(typeTokenOf()) {
    override fun deserialize(type: Type, obj: Any): PotionEffectType {
        val key = NamespacedKey.minecraft(obj.toString())
        return Registry.EFFECT.get(key) ?: throw SerializationException(type, "Can't find potion effect type with key '$key'")
    }

    override fun serialize(item: PotionEffectType, typeSupported: Predicate<Class<*>>?): Any {
        return item.key.value()
    }
}

internal object EntityTypeSerializer : ScalarSerializer<EntityType>(typeTokenOf()) {
    override fun deserialize(type: Type, obj: Any): EntityType {
        val key = NamespacedKey.minecraft(obj.toString())
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.ENTITY_TYPE).get(key) ?: throw SerializationException(type, "Can't find entity type with key '$key'")
    }

    override fun serialize(item: EntityType, typeSupported: Predicate<Class<*>>?): Any {
        return item.key.value()
    }
}

internal object DamageTypeSerializer : ScalarSerializer<DamageType>(typeTokenOf()) {
    override fun deserialize(type: Type, obj: Any): DamageType {
        val key = NamespacedKey.minecraft(obj.toString())
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.DAMAGE_TYPE).get(key) ?: throw SerializationException(type, "Can't find damage type with key '$key'")
    }

    override fun serialize(item: DamageType, typeSupported: Predicate<Class<*>>?): Any {
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
