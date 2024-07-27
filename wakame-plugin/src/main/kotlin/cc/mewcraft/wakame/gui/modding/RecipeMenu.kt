package cc.mewcraft.wakame.gui.modding

import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.components.PortableObject
import cc.mewcraft.wakame.item.tryNekoStack
import cc.mewcraft.wakame.reforge.modding.ModdingSession
import cc.mewcraft.wakame.util.hideTooltip
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.inventory.VirtualInventory
import xyz.xenondevs.invui.inventory.event.ItemPostUpdateEvent
import xyz.xenondevs.invui.inventory.event.ItemPreUpdateEvent
import xyz.xenondevs.invui.inventory.event.UpdateReason
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
    private val recipeGui: Gui = Gui.normal { builder ->
        // a: 定制对象的预览物品
        // b: 接收玩家输入的物品的容器
        // *: 起视觉引导作用的物品
        builder.setStructure("a * b")
        builder.addIngredient('a', viewItemConstructor(recipeSession))
        builder.addIngredient('b', inputInventory)
        builder.addIngredient('*', SimpleItem(ItemStack(Material.WHITE_STAINED_GLASS_PANE).hideTooltip(true)))
    }

    /**
     * 用于当前定制的 [Gui].
     */
    fun getGui(): Gui {
        return recipeGui
    }

    /**
     * 获取 [stack] 中包含的便携式物品.
     */
    protected abstract fun getPortableObject(stack: NekoStack): PortableObject<T>?

    /**
     * 构造用于显示定制对象的物品.
     */
    protected abstract fun viewItemConstructor(recipeSession: ModdingSession.RecipeSession<T>): Item

    /**
     * 当输入容器中的物品发生*变化前*调用.
     */
    protected open fun onInputInventoryPreUpdate(event: ItemPreUpdateEvent) {
        val prevItem = event.previousItem
        val newItem = event.newItem
        logger.info("Recipe input updating: ${prevItem?.type} -> ${newItem?.type}")

        when {
            // Case 1: 玩家交换输入容器中的物品
            event.isSwap -> {
                event.isCancelled = true
                viewer.sendMessage("猫咪不可以!")
            }

            // Case 2: 玩家向输入容器中添加物品
            event.isAdd -> {
                val session = parentMenu.getSession() ?: run {
                    event.isCancelled = true
                    logger.error("Modding session (viewer: ${viewer.name}) is null, but an item is being added to the recipe menu. This is a bug!")
                    viewer.sendMessage("发生未知错误")
                    return
                }

                val stack = newItem?.tryNekoStack ?: run {
                    event.isCancelled = true
                    viewer.sendMessage("请放入一个萌芽物品!")
                    return
                }

                val portableObject = getPortableObject(stack) ?: run {
                    event.isCancelled = true
                    viewer.sendMessage("请放入一个便携式物品!")
                    return
                }

                val result = recipeSession.test(portableObject.wrapped)
                if (!result.isSuccess) {
                    event.isCancelled = true
                    viewer.sendMessage("该物品无法用于定制该词条栏!")
                    return
                }

                // 由于把物品放入容器的方式有好几种,
                // 必须让玩家“自然的”将物品放进容器,
                // 否则处理起来太麻烦.

                // 将*克隆*放进输入容器
                event.newItem = stack.itemStack
                // 保存输入物品
                recipeSession.input = stack
                // 通知主菜单, 更新输出容器
                parentMenu.refreshOutput()
                // 玩家做出了修改, 重置确认状态
                session.confirmed = false
            }

            // Case 3: 玩家从输入容器中移除物品
            event.isRemove -> {
                event.isCancelled = true

                val moddingSession = parentMenu.getSession() ?: run {
                    logger.error("Modding session (viewer: ${viewer.name}) is null, but an item is being removed to the recipe menu. This is a bug!")
                    return
                }

                val input = recipeSession.input ?: run {
                    logger.error("Recipe session (viewer: ${viewer.name}) input is null, but an item is being removed from the recipe menu. This is a bug!")
                    return
                }

                // 清空输入容器
                clearInputSlot()
                // 将原输入物品退回玩家
                viewer.inventory.addItem(input.handle)
                // 清空输入物品
                recipeSession.input = null
                // 通知主菜单, 更新输出容器
                parentMenu.refreshOutput()
                // 玩家做出了修改, 重置确认状态
                moddingSession.confirmed = false
            }
        }
    }

    /**
     * 当输入容器中的物品发生*变化后*调用.
     */
    protected open fun onInputInventoryPostUpdate(event: ItemPostUpdateEvent) {
        val prevItem = event.previousItem
        val newItem = event.newItem
        logger.info("Recipe input updated: ${prevItem?.type} -> ${newItem?.type}")
    }

    init {
        // 初始化输入容器的 handlers
        inputInventory.setPreUpdateHandler(::onInputInventoryPreUpdate)
        inputInventory.setPostUpdateHandler(::onInputInventoryPostUpdate)
    }

    private fun fillInputSlot(stack: ItemStack) {
        inputInventory.setItem(UpdateReason.SUPPRESSED, 0, stack)
    }

    private fun clearInputSlot() {
        inputInventory.setItem(UpdateReason.SUPPRESSED, 0, null)
    }
}