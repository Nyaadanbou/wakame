package cc.mewcraft.wakame.gui.mod

import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.tryNekoStack
import cc.mewcraft.wakame.reforge.mod.ModdingSession
import cc.mewcraft.wakame.util.hideAllFlags
import cc.mewcraft.wakame.util.hideTooltip
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.inventory.VirtualInventory
import xyz.xenondevs.invui.inventory.event.ItemPostUpdateEvent
import xyz.xenondevs.invui.inventory.event.ItemPreUpdateEvent
import xyz.xenondevs.invui.inventory.event.UpdateReason
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.ItemWrapper
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.item.impl.SimpleItem

/**
 * 用于定制*单个*词条栏核心的菜单, 将被当做子菜单嵌入到 [ModdingMenu] 中.
 *
 * - 该实例在主菜单刚被打开的时候不会创建
 * - 仅当玩家把要定制的物品放入定制台时, 该实例才会被创建
 * - 物品上的*每个*词条栏都有一个对应的 [ReplaceMenu] 实例来处理定制
 */
internal class ReplaceMenu(
    private val parentMenu: ModdingMenu,
    private val replace: ModdingSession.Replace,
) : KoinComponent {
    private val logger: Logger by inject()
    private val viewer: Player = parentMenu.viewer

    private val inputInventory: VirtualInventory = VirtualInventory(/* maxStackSizes = */ intArrayOf(1))
    private val primaryGui: Gui = Gui.normal { builder ->
        // a: 定制对象的预览物品
        // b: 接收玩家输入的物品的容器
        // *: 起视觉引导作用的物品
        builder.setStructure("a * b")
        builder.addIngredient('a', ViewItem(replace))
        builder.addIngredient('b', inputInventory)
        builder.addIngredient('*', SimpleItem(ItemStack(Material.WHITE_STAINED_GLASS_PANE).hideTooltip(true)))
    }

    init {
        inputInventory.setPreUpdateHandler(::onInputInventoryPreUpdate)
        inputInventory.setPostUpdateHandler(::onInputInventoryPostUpdate)
    }

    /**
     * 获取当前定制的 [Gui].
     */
    val gui: Gui
        get() = primaryGui

    /**
     * 当输入容器中的物品发生*变化前*调用.
     */
    private fun onInputInventoryPreUpdate(event: ItemPreUpdateEvent) {
        val prevItem = event.previousItem
        val newItem = event.newItem
        logger.info("Replace input updating: ${prevItem?.type} -> ${newItem?.type}")

        when {
            // Case 1: 玩家交换输入容器中的物品
            event.isSwap -> {
                event.isCancelled = true
                viewer.sendMessage("猫咪不可以!")
            }

            // Case 2: 玩家向输入容器中添加物品
            event.isAdd -> {
                val session = parentMenu.currentSession ?: run {
                    logger.error("Modding session (viewer: ${viewer.name}) is null, but an item is being added to the replace menu. This is a bug!")
                    viewer.sendMessage("发生未知错误")
                    event.isCancelled = true; return
                }

                val stack = newItem?.tryNekoStack ?: run {
                    viewer.sendMessage("请放入一个萌芽物品!")
                    event.isCancelled = true; return
                }

                val portableObject = stack.components.get(ItemComponentTypes.PORTABLE_CORE) ?: run {
                    viewer.sendMessage("请放入一个便携式物品!")
                    event.isCancelled = true; return
                }

                val result = replace.test(portableObject.wrapped)
                if (!result.successful) {
                    viewer.sendMessage("该物品无法用于定制该词条栏!")
                    event.isCancelled = true; return
                }

                // 由于把物品放入容器的方式有好几种,
                // 必须让玩家“自然的”将物品放进容器,
                // 否则处理起来太麻烦.

                // 将*克隆*放进输入容器
                event.newItem = stack.itemStack
                // 保存输入物品
                replace.input = stack
                // 通知主菜单, 更新输出容器
                parentMenu.refreshOutput()
                // 玩家做出了修改, 重置确认状态
                session.confirmed = false
            }

            // Case 3: 玩家从输入容器中移除物品
            event.isRemove -> {
                event.isCancelled = true

                val moddingSession = parentMenu.currentSession ?: run {
                    logger.error("Modding session is null (viewer: ${viewer.name}), but an item is being removed from the replace menu. This is a bug!")
                    return
                }

                val input = replace.input ?: run {
                    logger.error("Replace's input is null (viewer: ${viewer.name}), but an item is being removed from the replace menu. This is a bug!")
                    return
                }

                // 清空输入容器
                clearInputSlot()
                // 将原输入物品退回玩家
                viewer.inventory.addItem(input.unsafe.handle)
                // 清空输入物品
                replace.input = null
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
    private fun onInputInventoryPostUpdate(event: ItemPostUpdateEvent) {
        val prevItem = event.previousItem
        val newItem = event.newItem
        logger.info("Replace input updated: ${prevItem?.type} -> ${newItem?.type}")
    }

    private fun fillInputSlot(stack: ItemStack) {
        inputInventory.setItem(UpdateReason.SUPPRESSED, 0, stack)
    }

    private fun clearInputSlot() {
        inputInventory.setItem(UpdateReason.SUPPRESSED, 0, null)
    }

    private class ViewItem(
        val replace: ModdingSession.Replace,
    ) : AbstractItem() {

        // 临时实现, 用于方便预览
        private companion object {
            val trims: List<Material> = Tag.ITEMS_TRIM_TEMPLATES.values.toList()
        }

        private fun getTrimMaterial(): Material {
            val sessionHash = replace.hashCode()
            val index = sessionHash % trims.size
            return trims[index]
        }

        override fun getItemProvider(): ItemProvider {
            val stack = ItemStack(getTrimMaterial()).hideAllFlags()
            replace.display.apply(stack)
            return ItemWrapper(stack)
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            player.sendPlainMessage("Clicked slot number: ${event.slot}")
        }
    }
}