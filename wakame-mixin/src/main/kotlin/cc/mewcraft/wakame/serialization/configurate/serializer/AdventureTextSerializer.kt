package cc.mewcraft.wakame.serialization.configurate.serializer

import cc.mewcraft.wakame.MM
import cc.mewcraft.wakame.util.typeTokenOf
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.StyleBuilderApplicable
import net.kyori.adventure.text.format.TextDecoration
import org.spongepowered.configurate.serialize.ScalarSerializer
import java.lang.reflect.Type
import java.util.function.Predicate


/*internal*/ object ComponentSerializer : ScalarSerializer<Component>(typeTokenOf()) {
    override fun deserialize(type: Type, obj: Any): Component {
        return MM.deserialize(obj.toString().replace("§", ""))
    }

    override fun serialize(item: Component, typeSupported: Predicate<Class<*>>?): Any {
        return MM.serialize(item)
    }
}

/*internal*/ object StyleSerializer : ScalarSerializer<Style>(typeTokenOf()) {
    override fun deserialize(type: Type, obj: Any): Style {
        return ComponentSerializer.deserialize(type, obj).style()
    }

    override fun serialize(item: Style, typeSupported: Predicate<Class<*>>?): Any {
        val component = Component.text().style(item).build()
        return MM.serialize(component)
    }
}

/*internal*/ object StyleBuilderApplicableSerializer : ScalarSerializer<Array<StyleBuilderApplicable>>(typeTokenOf()) {
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
        return MM.serialize(component)
    }
}

object NamedTextColorSerializer : ScalarSerializer<NamedTextColor>(typeTokenOf()) {
    override fun deserialize(type: Type, obj: Any): NamedTextColor {
        return NamedTextColor.NAMES.value(obj.toString().lowercase())
            ?: throw IllegalArgumentException("Unknown color: $obj")
    }

    override fun serialize(item: NamedTextColor, typeSupported: Predicate<Class<*>>?): Any {
        return NamedTextColor.NAMES.keyOrThrow(item)
    }
}