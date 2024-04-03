package cc.mewcraft.wakame.util

import com.google.common.collect.Range
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.StyleBuilderApplicable
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import org.koin.core.context.GlobalContext
import org.spongepowered.configurate.CommentedConfigurationNode
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.ScalarSerializer
import org.spongepowered.configurate.serialize.TypeSerializer
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import org.spongepowered.configurate.yaml.NodeStyle
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.lang.reflect.Type
import java.util.function.Predicate
import kotlin.reflect.KType
import kotlin.reflect.jvm.javaType
import kotlin.reflect.typeOf

internal typealias NekoConfigurationLoader = YamlConfigurationLoader
internal typealias NekoConfigurationNode = CommentedConfigurationNode

/**
 * Apply common settings for the [YamlConfigurationLoader.Builder].
 */
internal fun YamlConfigurationLoader.Builder.withDefaultSettings(): YamlConfigurationLoader.Builder {
    return this
        // use 2 spaces indent
        .indent(2)
        // always use block style
        .nodeStyle(NodeStyle.BLOCK)
        // register common serializers
        .defaultOptions { options ->
            options
                // don't automatically write default values from serializers back to config files
                .shouldCopyDefaults(false)
                // add common serializers
                .serializers {
                    it.kregister(KeySerializer)
                    it.kregister(IntRangeSerializer)
                    it.kregister(NumericValueSerializer)
                    it.register(ComponentSerializer)
                    it.register(StyleBuilderApplicableSerializer)
                }
        }
}

/**
 * Creates a basic builder of configuration loader.
 */
internal fun buildYamlLoader(
    builder: TypeSerializerCollection.Builder.() -> Unit = { },
): YamlConfigurationLoader.Builder {
    return YamlConfigurationLoader.builder()
        .withDefaultSettings()
        .defaultOptions { options ->
            options.serializers {
                it.builder()
            }
        }
}

/**
 * @see TypeSerializerCollection.Builder.register
 */
internal inline fun <reified T> TypeSerializerCollection.Builder.kregister(serializer: TypeSerializer<T>): TypeSerializerCollection.Builder {
    return this.register({ javaTypeOf<T>() == it }, serializer)
}

/**
 * @see ConfigurationNode.require
 */
internal inline fun <reified T> ConfigurationNode.krequire(): T {
    return this.krequire(typeOf<T>())
}

/**
 * @see ConfigurationNode.require
 */
internal fun <T> ConfigurationNode.krequire(type: KType): T {
    val ret = this.get(type.javaType) ?: throw NoSuchElementException(
        "Missing value of type '${type}' at '${path().joinToString(".")}'"
    )
    return (@Suppress("UNCHECKED_CAST") (ret as T))
}

//<editor-fold desc="Basic Serializers">
internal object KeySerializer : ScalarSerializer<Key>(typeTokenOf()) {
    override fun deserialize(type: Type, obj: Any): Key = Key(obj.toString())
    override fun serialize(item: Key, typeSupported: Predicate<Class<*>>?): Any = item.toString()
}

internal object IntRangeSerializer : ScalarSerializer<Range<Int>>(typeTokenOf()) {
    override fun deserialize(type: Type, obj: Any): Range<Int> = RangeParser.parseIntRange(obj.toString())
    override fun serialize(item: Range<Int>?, typeSupported: Predicate<Class<*>>?): Any = throw UnsupportedOperationException()
}

internal object ComponentSerializer : ScalarSerializer<Component>(typeTokenOf()) {
    override fun deserialize(type: Type, obj: Any): Component {
        val message = obj.toString().replace("§", "")
        return GlobalContext.get().get<MiniMessage>().deserialize(message)
    }

    override fun serialize(item: Component, typeSupported: Predicate<Class<*>>?): Any {
        return GlobalContext.get().get<MiniMessage>().serialize(item)
    }
}

internal object StyleBuilderApplicableSerializer : ScalarSerializer<Array<StyleBuilderApplicable>>(typeTokenOf()) {
    override fun deserialize(type: Type, obj: Any): Array<StyleBuilderApplicable> {
        val component = ComponentSerializer.deserialize(type, obj)
        val styleList = ArrayList<StyleBuilderApplicable>()

        with(component) {
            // font()?.let { font -> styleList += StyleBuilderApplicable { it.font(font) } }
            color()?.let { styleList += it }
            TextDecoration.entries
                .filter { decoration(it) == TextDecoration.State.TRUE }
                .map { it.withState(decoration(it)) }
                .forEach { styleList += it }
            // clickEvent()?.let { styleList += it }
            // hoverEvent()?.let { styleList += it }
        }

        return styleList.toTypedArray()
    }

    override fun serialize(item: Array<StyleBuilderApplicable>, typeSupported: Predicate<Class<*>>?): Any {
        val component = Component.text().style { builder -> item.forEach(builder::apply) }.build()
        return GlobalContext.get().get<MiniMessage>().serialize(component)
    }
}
//</editor-fold>