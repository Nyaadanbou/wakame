package cc.mewcraft.wakame.rarity

import cc.mewcraft.wakame.util.require
import cc.mewcraft.wakame.util.toStableByte
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializer
import java.lang.reflect.Type

internal class RaritySerializer : TypeSerializer<Rarity> {
    override fun deserialize(type: Type, node: ConfigurationNode): Rarity {
        val name = node.key().toString()
        val binary = node.node("binary_index").require<Int>().toStableByte()
        val displayName = node.node("display_name").require<String>()
        return Rarity(name, binary, displayName)
    }

    override fun serialize(type: Type, obj: Rarity?, node: ConfigurationNode) {
        TODO("Not yet implemented")
    }
}