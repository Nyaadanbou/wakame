package cc.mewcraft.wakame.item2.config.property.impl

import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.util.MojangStack
import cc.mewcraft.wakame.util.item.toBukkit
import cc.mewcraft.wakame.util.item.toNMS
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.exceptions.CommandSyntaxException
import net.minecraft.commands.arguments.item.ItemParser
import net.minecraft.server.MinecraftServer
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type

/**
 * 代表一个自定义物品的基础物品堆叠.
 * 自定义物品将在此基础物品上进行创建.
 *
 * 平日交流也可以俗称该数据为“物品底模”.
 */
interface ItemBase {

    companion object {

        @JvmField
        val EMPTY: ItemBase = EmptyItemBase

        @JvmField
        val SERIALIZER: TypeSerializer<ItemBase> = SimpleItemBase.Serializer

    }

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
    fun createMojang(): MojangStack

    /**
     * 以 [format] 格式创建一个新物品.
     */
    fun createBukkit(): ItemStack

}

// ------------
// 内部实现
// ------------

// EmptyItemBase 作为一个默认值, 类型使用 AIR 不合适.
// 因为我们无法往空气物品堆叠写入任何的 DataComponent,
// 也就无法让套皮物品的生成逻辑共用自定义物品的生成逻辑.
private data object EmptyItemBase : ItemBase {
    override val type: Material = Material.STONE
    override val format: String = ""
    override fun createMojang(): MojangStack = createBukkit().toNMS()
    override fun createBukkit(): ItemStack = ItemStack.of(Material.STONE)
}

private data class SimpleItemBase(
    override val type: Material,
    override val format: String = "",
) : ItemBase {

    companion object {
        private val NOT_USABLE_TYPES = setOf(
            Material.AIR, Material.CAVE_AIR, Material.VOID_AIR
        )
    }

    override fun createMojang(): MojangStack {
        // Source: org.bukkit.craftbukkit.inventory.CraftItemFactory.createItemStack
        try {
            val arguments = type.name.lowercase() + format
            val arg = ItemParser(MinecraftServer.getDefaultRegistryAccess()).parse(StringReader(arguments))

            val item = arg.item().value()
            val mojangStack = MojangStack(item)

            val nbt = arg.components()
            if (nbt != null) {
                mojangStack.applyComponents(nbt)
            }

            return mojangStack
        } catch (ex: CommandSyntaxException) {
            throw IllegalArgumentException("Could not parse ItemStack: $format", ex)
        }
    }

    override fun createBukkit(): ItemStack {
        return createMojang().toBukkit()
    }

    object Serializer : TypeSerializer<ItemBase> {
        override fun deserialize(type: Type, node: ConfigurationNode): ItemBase {
            val arguments = node.string ?: throw SerializationException(node, type, "Expected a string, got ${node.raw()}")
            if (arguments.isEmpty()) {
                return ItemBase.EMPTY
            }

            val i = arguments.indexOf('[')
            val material =
                if (i == -1) Material.matchMaterial(arguments) ?: throw SerializationException("Unknown type: $arguments")
                else Material.matchMaterial(arguments.substring(0, i)) ?: throw SerializationException("Unknown type: ${arguments.substring(0, i)}")
            val format =
                if (i == -1) ""
                else arguments.substring(i, arguments.length)

            if (material in NOT_USABLE_TYPES) {
                throw SerializationException(node, type, "Cannot use $material as base item type.")
            }

            return SimpleItemBase(material, format)
        }

        // 默认值由外部显式指定, 不在这里隐式指定, 代码会更好维护点儿
        //override fun emptyValue(specificType: Type, options: ConfigurationOptions): ItemBase2 {
        //    return ItemBase2.EMPTY
        //}
    }
}