package cc.mewcraft.wakame.config.configurate

import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.util.typeTokenOf
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.StyleBuilderApplicable
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import org.spongepowered.configurate.serialize.ScalarSerializer
import java.lang.reflect.Type
import java.util.function.Predicate

internal object KeySerializer : ScalarSerializer<Key>(typeTokenOf()) {
    override fun deserialize(type: Type, obj: Any): Key = Key.key(obj.toString())
    override fun serialize(item: Key, typeSupported: Predicate<Class<*>>?): Any = item.toString()
}

internal object ComponentSerializer : ScalarSerializer<Component>(typeTokenOf()) {
    override fun deserialize(type: Type, obj: Any): Component {
        val message = obj.toString().replace("§", "")
        return Injector.get<MiniMessage>().deserialize(message)
    }

    override fun serialize(item: Component, typeSupported: Predicate<Class<*>>?): Any = Injector.get<MiniMessage>().serialize(item)
}

internal object StyleSerializer : ScalarSerializer<Style>(typeTokenOf()) {
    override fun deserialize(type: Type, obj: Any): Style = ComponentSerializer.deserialize(type, obj).style()
    override fun serialize(item: Style, typeSupported: Predicate<Class<*>>?): Any {
        val component = Component.text().style(item).build()
        return Injector.get<MiniMessage>().serialize(component)
    }
}

internal object StyleBuilderApplicableSerializer : ScalarSerializer<Array<StyleBuilderApplicable>>(typeTokenOf()) {
    override fun deserialize(type: Type, obj: Any): Array<StyleBuilderApplicable> {
        val component = ComponentSerializer.deserialize(type, obj)
        val styleList = ArrayList<StyleBuilderApplicable>()

        with(component) {
            font()?.let { font -> styleList += StyleBuilderApplicable { it.font(font) } }
            color()?.let { styleList += it }
            TextDecoration.entries
                .filter { decoration(it) == TextDecoration.State.TRUE }
                .map { it.withState(decoration(it)) }
                .forEach { styleList += it }
            clickEvent()?.let { styleList += it }
            hoverEvent()?.let { styleList += it }
        }

        return styleList.toTypedArray()
    }

    override fun serialize(item: Array<StyleBuilderApplicable>, typeSupported: Predicate<Class<*>>?): Any {
        val component = Component.text().style { builder -> item.forEach(builder::apply) }.build()
        return Injector.get<MiniMessage>().serialize(component)
    }
}