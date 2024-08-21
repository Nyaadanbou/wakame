package cc.mewcraft.wakame.display2

import cc.mewcraft.wakame.core.Pipeline
import cc.mewcraft.wakame.item.NekoItem
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.realize
import cc.mewcraft.wakame.registry.ItemRegistry
import net.kyori.adventure.key.Key
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

/**
 * 持有一个 [NekoItem],
 */
class MenuItemHolder(
    /**
     * 萌芽物品的唯一标识.
     */
    val itemId: Key,
) {
    /**
     * [itemId] 对应的物品模板.
     */
    val template: NekoItem? by lazy { ItemRegistry.CUSTOM.find(itemId) }

    /**
     * 创建一个菜单物品.
     */
    fun create(): MenuItem {
        return MenuItem(this)
    }
}

/**
 * 负责从萌芽物品构建出一个适用于自定义菜单的 [ItemStack].
 *
 * 该类可以:
 * - 已知一个萌芽物品的唯一标识, 构建一个静态的 [ItemStack] 用于菜单图标.
 *
 * 该类不可以:
 * - 将已存在的 [NekoStack] 转换为一个 [ItemStack] 用于菜单图标.
 */
class MenuItem(
    private val menuItem: MenuItemHolder
) {
    private var tooltipPipeline: Pipeline<Nothing, NekoStack, ItemStack> = TODO()
    private val cachedItemStack: ItemStack by lazy {
        val nyaItem = menuItem.template
        if (nyaItem == null) {
            return@lazy ItemStack(Material.BARRIER)
        }
        val nyaStack = nyaItem.realize()
        val itemStack = tooltipPipeline.execute(nyaStack)
        itemStack
    }

    /**
     * 修改菜单图标的渲染设置.
     */
    fun configure(config: Pipeline<Nothing, NekoStack, ItemStack>) {
        tooltipPipeline = config
    }

    /**
     * 生成一个用于菜单图标的 [ItemStack].
     */
    fun getItemStack(): ItemStack {
        return cachedItemStack.clone()
    }
}