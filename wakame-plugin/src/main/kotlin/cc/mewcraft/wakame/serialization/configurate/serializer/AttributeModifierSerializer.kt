package cc.mewcraft.wakame.serialization.configurate.serializer

import cc.mewcraft.lazyconfig.configurate.SimpleSerializer
import cc.mewcraft.lazyconfig.configurate.require
import cc.mewcraft.wakame.entity.attribute.AttributeModifier
import cc.mewcraft.wakame.util.KoishKey
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

object AttributeModifierSerializer : SimpleSerializer<AttributeModifier> {
    override fun deserialize(type: Type, node: ConfigurationNode): AttributeModifier {
        val id = node.node("id").require<KoishKey>()
        val operation = node.node("operation").require<AttributeModifier.Operation>()
        val value = node.node("value").require<Double>()
        return AttributeModifier(id, value, operation)
    }

    override fun serialize(type: Type, obj: AttributeModifier?, node: ConfigurationNode) {
        if (obj == null) return
        val id = obj.id
        val operation = obj.operation
        val amount = obj.amount
        node.node("id").set(id)
        node.node("operation").set(operation)
        node.node("value").set(amount)
    }
}