package cc.mewcraft.wakame.serialization.configurate.typeserializer

import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.require
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

object AttributeModifierSerializer : TypeSerializer<AttributeModifier> {
    override fun deserialize(type: Type, node: ConfigurationNode): AttributeModifier {
        val id = node.node("id").require<Identifier>()
        val operation = node.node("operation").require<AttributeModifier.Operation>()
        val value = node.node("value").require<Double>()
        return AttributeModifier(id, value, operation)
    }
}