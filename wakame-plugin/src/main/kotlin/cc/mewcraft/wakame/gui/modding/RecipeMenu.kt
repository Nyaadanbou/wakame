package cc.mewcraft.wakame.gui.modding

import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.components.PortableObject
import cc.mewcraft.wakame.item.tryNekoStack
import cc.mewcraft.wakame.reforge.modding.session.ModdingSession
import cc.mewcraft.wakame.util.hideTooltip
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.inventory.VirtualInventory
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.impl.SimpleItem

/**
 * 用于定制*单个*词条栏的菜单, 将被当做子菜单嵌入进 [ModdingMenu] 中.
 *
 * - 该实例在主菜单刚被打开的时候不会创建
 * - 仅当玩家把要定制的物品放入定制台时, 该实例才会被创建
 * - 物品上的*每个*词条栏都有一个对应的 [RecipeMenu] 实例来处理定制
 *
 * @param T 定制的类型
 */
abstract class RecipeMenu<T>(
    private val viewer: Player,
    private val parentMenu: ModdingMenu<T>,
    private val recipeSession: ModdingSession.RecipeSession<T>,
) : KoinComponent {
    private val logger: Logger by inject()
    private val inputInventory: VirtualInventory = VirtualInventory(/* maxStackSizes = */ intArrayOf(1))
    val recipeGui: Gui = Gui.normal { builder ->
        // a: 定制对象的预览物品
        // b: 接收玩家输入的物品的容器
        // *: 起视觉引导作用的物品
        builder.setStructure("a * b")
        builder.addIngredient('a', viewItemConstructor(recipeSession))
        builder.addIngredient('b', inputInventory)
        builder.addIngredient('*', SimpleItem(ItemStack(Material.WHITE_STAINED_GLASS_PANE).hideTooltip(true)))
    }

    protected abstract fun getPortableObject(stack: NekoStack): PortableObject<T>?
    protected abstract fun viewItemConstructor(recipeSession: ModdingSession.RecipeSession<T>): Item

    // 初始化输入容器的 handler
    init {
        inputInventory.setPreUpdateHandler pre@{ event ->
            val prevItem = event.previousItem
            val newItem = event.newItem
            logger.info("Recipe input updating: ${prevItem?.type} -> ${newItem?.type}")

            when {
                // Case 1: 玩家交换输入容器中的物品
                event.isSwap -> {
                    event.isCancelled = true
                }

                // Case 2: 玩家向输入容器中添加物品
                event.isAdd -> {
                    val session = parentMenu.currentSession ?: run {
                        event.isCancelled = true
                        logger.error("Modding session (viewer: ${viewer.name}) is null, but an item is being added to the recipe menu. This is a bug!")
                        return@pre
                    }

                    val stack = newItem?.tryNekoStack ?: run {
                        event.isCancelled = true
                        viewer.sendMessage("请放入一个萌芽物品!")
                        return@pre
                    }

                    val portableObject = getPortableObject(stack) ?: run {
                        event.isCancelled = true
                        viewer.sendMessage("请放入一个便携式物品!")
                        return@pre
                    }

                    val result = recipeSession.test(portableObject.wrapped)
                    if (!result.isSuccess) {
                        event.isCancelled = true
                        viewer.sendMessage("该物品无法用于定制该词条栏!")
                        return@pre
                    }

                    // 保存物品快照
                    recipeSession.input = stack
                    // 通知主菜单, 更新输出容器里的物品
                    parentMenu.refreshOutputInventory()
                    // 玩家做出了修改, 重置确认状态
                    session.confirmed = false
                }

                // Case 3: 玩家从输入容器中移除物品
                event.isRemove -> {
                    val moddingSession = parentMenu.currentSession ?: run {
                        event.isCancelled = true
                        logger.error("Modding session (viewer: ${viewer.name}) is null, but an item is being added to the recipe menu. This is a bug!")
                        return@pre
                    }

                    val input = recipeSession.input ?: run {
                        event.isCancelled = true
                        logger.error("Recipe menu (viewer: ${viewer.name}) input is null, but an item is being removed from the recipe menu. This is a bug!")
                        return@pre
                    }

                    // 设置 itemOnCursor 为原输入
                    setItemOnCursor(input.handle)
                    // 清空物品输入
                    recipeSession.input = null
                    // 通知主菜单, 更新输出容器里的物品
                    parentMenu.refreshOutput()
                    // 玩家做出了修改, 重置确认状态
                    moddingSession.confirmed = false
                }
            }
        }
        inputInventory.setPostUpdateHandler post@{ event ->
            val prevItem = event.previousItem
            val newItem = event.newItem
            logger.info("Recipe input updated: ${prevItem?.type} -> ${newItem?.type}")
        }
    }

    private fun setItemOnCursor(stack: ItemStack) {
        viewer.setItemOnCursor(stack)
    }
}