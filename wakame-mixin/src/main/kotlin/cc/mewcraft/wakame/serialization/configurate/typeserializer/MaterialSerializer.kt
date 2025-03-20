package cc.mewcraft.wakame.serialization.configurate.typeserializer

import cc.mewcraft.wakame.util.typeTokenOf
import org.bukkit.Material
import org.spongepowered.configurate.serialize.ScalarSerializer
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type
import java.util.function.Predicate

/*internal*/ object MaterialSerializer : ScalarSerializer<Material>(typeTokenOf()) {
    override fun deserialize(type: Type, obj: Any): Material {
        return Material.matchMaterial(obj.toString()) ?: throw SerializationException(type, "No such material type: '$obj'")
    }

    override fun serialize(item: Material, typeSupported: Predicate<Class<*>>?): Any {
        return item.name
    }
}
