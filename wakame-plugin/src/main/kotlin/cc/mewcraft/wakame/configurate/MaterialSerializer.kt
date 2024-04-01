package cc.mewcraft.wakame.configurate

import cc.mewcraft.wakame.SchemaSerializer
import cc.mewcraft.wakame.util.javaTypeOf
import cc.mewcraft.wakame.util.requireKt
import org.bukkit.Material
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.ConfigurationOptions
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type

internal object MaterialSerializer : SchemaSerializer<Material> {
    override fun emptyValue(specificType: Type?, options: ConfigurationOptions?): Material? {
        return Material.STONE
    }

    override fun deserialize(type: Type, node: ConfigurationNode): Material {
        val name = node.requireKt<String>()
        val material = Material.matchMaterial(name) ?: throw SerializationException(node.path(), javaTypeOf<Material>(), "Can't parse material '$name'")
        return material
    }
}