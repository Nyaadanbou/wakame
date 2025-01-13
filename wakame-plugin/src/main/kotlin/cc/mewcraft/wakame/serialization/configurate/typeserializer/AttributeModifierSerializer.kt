package cc.mewcraft.wakame.serialization.configurate.typeserializer

import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.krequire
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

object AttributeModifierSerializer : TypeSerializer<AttributeModifier> {
    override fun deserialize(type: Type, node: ConfigurationNode): AttributeModifier {
        val id = node.node("id").krequire<Identifier>()
        val operation = node.node("operation").krequire<AttributeModifier.Operation>()
        val value = node.node("value").krequire<Double>()
        return AttributeModifier(id, value, operation)
    }
}