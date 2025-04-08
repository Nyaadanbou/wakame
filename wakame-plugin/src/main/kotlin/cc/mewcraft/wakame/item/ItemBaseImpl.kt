package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.serialization.configurate.TypeSerializer2
import cc.mewcraft.wakame.util.javaTypeOf
import net.kyori.examination.ExaminableProperty
import net.kyori.examination.string.StringExaminer
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.ConfigurationOptions
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type
import java.util.stream.Stream


/* Implementations */


/**
 * [ItemBase] 的标准实现.
 */
internal class ItemBaseImpl(
    override val type: Material,
    override val format: String = "",
) : ItemBase {
    override fun createItemStack(): ItemStack {
        val arguments = type.name.lowercase() + format
        return Bukkit.getItemFactory().createItemStack(arguments)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty?> {
        return Stream.of(
            ExaminableProperty.of("type", type),
            ExaminableProperty.of("format", format)
        )
    }

    override fun toString(): String {
        return examine(StringExaminer.simpleEscaping())
    }
}

/**
 * [ItemBase] 的序列化器.
 */
internal object ItemBaseSerializer : TypeSerializer2<ItemBase> {
    override fun deserialize(type: Type, node: ConfigurationNode): ItemBase {
        val arguments = node.string ?: throw SerializationException(javaTypeOf<String>(), "Expected a string, got ${node.raw()}")
        if (arguments.isEmpty()) {
            return ItemBase.nop()
        }

        val i = arguments.indexOf('[')
        val type =
            if (i == -1) Material.matchMaterial(arguments) ?: throw SerializationException("Unknown type: $arguments")
            else Material.matchMaterial(arguments.substring(0, i)) ?: throw SerializationException("Unknown type: ${arguments.substring(0, i)}")
        val format =
            if (i == -1) ""
            else arguments.substring(i, arguments.length)

        return ItemBaseImpl(type, format)
    }

    override fun emptyValue(specificType: Type, options: ConfigurationOptions): ItemBase? {
        return ItemBase.nop()
    }
}