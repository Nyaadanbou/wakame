package cc.mewcraft.wakame.item

import net.kyori.examination.Examinable
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

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
        private val NOP: ItemBase = object : ItemBase {
            override val type: Material = Material.SHULKER_SHELL
            override val format: String = ""
            override fun createItemStack(): ItemStack = ItemStack.of(type)
        }
        private val EMPTY: ItemBase = object : ItemBase {
            override val type: Material = Material.AIR
            override val format: String = ""
            override fun createItemStack(): ItemStack = ItemStack.empty()
        }

        fun nop(): ItemBase = NOP

        fun empty(): ItemBase = EMPTY
    }
}