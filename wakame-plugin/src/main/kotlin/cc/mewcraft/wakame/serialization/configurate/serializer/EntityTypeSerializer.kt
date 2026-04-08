package cc.mewcraft.wakame.serialization.configurate.serializer

import cc.mewcraft.wakame.util.EnumLookup
import cc.mewcraft.wakame.util.typeTokenOf
import org.bukkit.entity.EntityType
import org.spongepowered.configurate.serialize.ScalarSerializer
import java.lang.reflect.Type
import java.util.function.Predicate

/*internal*/ object EntityTypeSerializer : ScalarSerializer<EntityType>(typeTokenOf()) {
    override fun deserialize(type: Type, obj: Any): EntityType? {
        return EnumLookup.lookup<EntityType>(obj.toString()).getOrNull()
    }

    override fun serialize(item: EntityType, typeSupported: Predicate<Class<*>>?): Any {
        return item.name
    }
}