package cc.mewcraft.wakame.kizami

import cc.mewcraft.wakame.util.require
import cc.mewcraft.wakame.util.toStableByte
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializer
import java.lang.reflect.Type

class KizamiSerializer : TypeSerializer<Kizami> {
    override fun deserialize(type: Type, node: ConfigurationNode): Kizami {
        val name = node.key().toString()
        val binary = node.node("binary_index").require<Int>().toStableByte()
        val displayName = node.node("display_name").require<String>()
        return Kizami(name, binary, displayName)
    }

    override fun serialize(type: Type, obj: Kizami?, node: ConfigurationNode) {
        TODO("Not yet implemented")
    }
}