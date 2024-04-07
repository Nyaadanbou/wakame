package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.util.Key
import cc.mewcraft.wakame.util.krequire
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializer
import java.lang.reflect.Type

internal interface DisplaySerializer<T> : TypeSerializer<T> {
    override fun serialize(type: Type, obj: T?, node: ConfigurationNode) {
        throw UnsupportedOperationException("Serialization is not supported")
    }
}

internal object LoreFormatSerializer : DisplaySerializer<ItemMetaStylizer.LoreFormat> {
    override fun deserialize(type: Type, node: ConfigurationNode): ItemMetaStylizer.LoreFormat {
        return ItemMetaStylizerImpl.LoreFormatImpl(
            line = node.node("line").krequire(),
            header = node.node("header").krequire(),
            bottom = node.node("footer").krequire()
        )
    }
}

internal object ListFormatSerializer : DisplaySerializer<ItemMetaStylizer.ListFormat> {
    override fun deserialize(type: Type, node: ConfigurationNode): ItemMetaStylizer.ListFormat {
        return ItemMetaStylizerImpl.ListFormatImpl(
            merged = node.node("merged").krequire(),
            single = node.node("single").krequire(),
            separator = node.node("separator").krequire()
        )
    }
}

internal object AttributeFormatSerializer : DisplaySerializer<AttributeStylizer.AttributeFormat> {
    override fun deserialize(type: Type, node: ConfigurationNode): AttributeStylizer.AttributeFormat {
        return AttributeStylizerImpl.AttributeFormatImpl(
            values = node.childrenMap().mapKeys { (key, _) -> key.toString() }
                .filter { (key, _) -> key != Attributes.ATTACK_SPEED_LEVEL.key.value() }
                .mapKeys { (key, _) -> Key(NekoNamespaces.ATTRIBUTE, key) }
                .mapValues { (_, value) -> value.krequire<String>() }
                .withDefault { value -> "${value.asString()} (missing config)" }
        )
    }
}

internal object AttackSpeedFormatSerializer : DisplaySerializer<AttributeStylizer.AttackSpeedFormat> {
    override fun deserialize(type: Type, node: ConfigurationNode): AttributeStylizer.AttackSpeedFormat {
        return AttributeStylizerImpl.AttackSpeedFormatImpl(
            merged = node.node("merged").krequire<String>(),
            levels = node.node("levels")
                .childrenMap()
                .mapKeys { (key, _) -> key.toString().toInt() }
                .mapValues { (_, value) -> value.krequire<String>() }
                .withDefault { key -> "$key (missing config)" }
        )
    }
}

internal object OperationFormatSerializer : DisplaySerializer<AttributeStylizer.OperationFormat> {
    override fun deserialize(type: Type, node: ConfigurationNode): AttributeStylizer.OperationFormat {
        return AttributeStylizerImpl.OperationFormatImpl(
            values = AttributeModifier.Operation.entries.associateWith { node.node(it.key).krequire() }
        )
    }
}