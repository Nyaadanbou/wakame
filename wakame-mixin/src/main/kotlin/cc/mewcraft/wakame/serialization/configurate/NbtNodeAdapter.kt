package cc.mewcraft.wakame.serialization.configurate

import net.minecraft.nbt.ByteArrayTag
import net.minecraft.nbt.ByteTag
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.DoubleTag
import net.minecraft.nbt.EndTag
import net.minecraft.nbt.FloatTag
import net.minecraft.nbt.IntArrayTag
import net.minecraft.nbt.IntTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.LongArrayTag
import net.minecraft.nbt.LongTag
import net.minecraft.nbt.ShortTag
import net.minecraft.nbt.StringTag
import net.minecraft.nbt.Tag
import org.spongepowered.configurate.BasicConfigurationNode
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.ConfigurationNodeFactory
import org.spongepowered.configurate.ConfigurationOptions
import java.io.IOException

/**
 * A conNbtfiguration adapter that converts Minecraft NBT data into a [ConfigurationNode].
 */
object NbtNodeAdapter {

    /**
     * Factory for creating configuration nodes.
     */
    private val FACTORY = ConfigurationNodeFactory { options ->
        BasicConfigurationNode.root(
            options.nativeTypes(
                setOf(
                    Map::class.java, List::class.java, Byte::class.java,
                    Short::class.java, Int::class.java, Long::class.java,
                    Float::class.java, Double::class.java,
                    LongArray::class.java, ByteArray::class.java, IntArray::class.java,
                    String::class.java
                )
            )
        )
    }

    /**
     * Given a tag, convert it to a node.
     *
     * @param tag the tag to convert
     * @param node the node to populate
     * @throws IOException if invalid tags are provided
     */
    fun nbtToNode(tag: Tag?, node: ConfigurationNode) {
        when (tag) {
            is CompoundTag -> tag.keySet().forEach { key -> nbtToNode(tag.get(key), node.node(key)) }
            is ListTag -> tag.forEach { value -> nbtToNode(value, node.appendListNode()) }
            is StringTag -> node.raw(tag.asString())
            is ByteTag -> node.raw(tag.asByte())
            is ShortTag -> node.raw(tag.asShort())
            is IntTag -> node.raw(tag.asInt())
            is LongTag -> node.raw(tag.asLong())
            is FloatTag -> node.raw(tag.asFloat())
            is DoubleTag -> node.raw(tag.asDouble())
            is ByteArrayTag -> setArrayOrList(node, tag.asByteArray)
            is IntArrayTag -> setArrayOrList(node, tag.asIntArray)
            is LongArrayTag -> setArrayOrList(node, tag.asLongArray)
            is EndTag -> Unit // no-op
            else -> throw IOException("Unknown tag type: ${tag?.javaClass}")
        }
    }

    /**
     * Convert a node to a tag. Because NBT is strongly typed and does not permit
     * lists with mixed types, some configuration nodes will not be convertible
     * to Tags.
     *
     * @param node the configuration node
     * @return the converted tag object
     * @throws IOException if an IO error occurs while converting the tag
     */
    fun nodeToNbt(node: ConfigurationNode): Tag {
        return when {
            node.isMap -> CompoundTag().apply {
                node.childrenMap().forEach { (key, value) -> put(key.toString(), nodeToNbt(value)) }
            }

            node.isList -> ListTag().apply {
                node.childrenList().forEach { add(nodeToNbt(it)) }
            }

            else -> when (val obj = node.raw()) {
                is ByteArray -> ByteArrayTag(obj)
                is IntArray -> IntArrayTag(obj)
                is LongArray -> LongArrayTag(obj)
                is Byte -> ByteTag.valueOf(obj)
                is Short -> ShortTag.valueOf(obj)
                is Int -> IntTag.valueOf(obj)
                is Long -> LongTag.valueOf(obj)
                is Float -> FloatTag.valueOf(obj)
                is Double -> DoubleTag.valueOf(obj)
                is String -> StringTag.valueOf(obj)
                else -> throw IOException("Unsupported object type ${obj?.javaClass}")
            }
        }
    }

    /**
     * Create an empty node with options appropriate for handling NBT data.
     *
     * @return the new node
     */
    fun createEmptyNode(): ConfigurationNode = FACTORY.createNode(ConfigurationOptions.defaults())

    /**
     * Create an empty node with options appropriate for handling NBT data.
     *
     * @param options options to work with
     * @return the new node
     */
    fun createEmptyNode(options: ConfigurationOptions): ConfigurationNode = FACTORY.createNode(options)

    /**
     * Get a factory for nodes prepared to handle NBT data.
     *
     * @return the factory
     */
    fun nodeFactory(): ConfigurationNodeFactory<BasicConfigurationNode> = FACTORY

    /**
     * Helper function to set an array or list in the node, depending on its type acceptance.
     *
     * @param node the configuration node
     * @param array the array to be set
     */
    private fun setArrayOrList(node: ConfigurationNode, array: ByteArray) {
        if (node.options().acceptsType(array.javaClass)) {
            node.raw(array)
        } else {
            node.raw(null)
            array.forEach { node.appendListNode().raw(it) }
        }
    }

    /**
     * Helper function to set an array or list in the node, depending on its type acceptance.
     *
     * @param node the configuration node
     * @param array the array to be set
     */
    private fun setArrayOrList(node: ConfigurationNode, array: IntArray) {
        if (node.options().acceptsType(array.javaClass)) {
            node.raw(array)
        } else {
            node.raw(null)
            array.forEach { node.appendListNode().raw(it) }
        }
    }

    /**
     * Helper function to set an array or list in the node, depending on its type acceptance.
     *
     * @param node the configuration node
     * @param array the array to be set
     */
    private fun setArrayOrList(node: ConfigurationNode, array: LongArray) {
        if (node.options().acceptsType(array.javaClass)) {
            node.raw(array)
        } else {
            node.raw(null)
            array.forEach { node.appendListNode().raw(it) }
        }
    }
}
