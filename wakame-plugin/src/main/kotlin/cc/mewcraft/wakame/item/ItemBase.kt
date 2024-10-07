package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.util.javaTypeOf
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.ConfigurationOptions
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type
import java.util.stream.Stream

/**
 * 代表一个自定义物品的基础物品堆叠.
 * 自定义物品将在此基础物品上进行创建.
 *
 * 你也可以俗称该类为“物品基底”.
 */
interface ItemBase : Examinable {
    /**
     * 该物品基底的类型 [Material].
     */
    val type: Material

    /**
     * 该物品基底的额外信息.
     *
     * 关于信息格式, 参考 [Command Format](https://minecraft.wiki/w/Data_component_format#Command_format).
     *
     * 有效例子:
     * * `[component1=value,component2=value]`
     * * `[!component3,!component4]`
     */
    val format: String

    /**
     * 以 [format] 格式创建一个新物品.
     */
    fun createItemStack(): ItemStack

    /**
     * 包含了 [ItemBase] 的常量.
     */
    companion object {
        val NOP: ItemBase = object : ItemBase {
            override val type: Material = Material.SHULKER_SHELL
            override val format: String = ""
            override fun createItemStack(): ItemStack = ItemStack.of(type)
        }
        val EMPTY: ItemBase = object : ItemBase {
            override val type: Material = Material.AIR
            override val format: String = ""
            override fun createItemStack(): ItemStack = ItemStack.empty()
        }
    }
}


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
        return toSimpleString()
    }
}

/**
 * [ItemBase] 的序列化器.
 */
internal object ItemBaseSerializer : TypeSerializer<ItemBase> {
    override fun deserialize(type: Type, node: ConfigurationNode): ItemBase {
        val arguments = node.string ?: throw SerializationException(javaTypeOf<String>(), "Expected a string, got ${node.raw()}")
        if (arguments.isEmpty()) {
            return ItemBase.NOP
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
        return ItemBase.NOP
    }
}