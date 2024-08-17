package cc.mewcraft.wakame.gui.mod

import cc.mewcraft.wakame.item.tryNekoStack
import cc.mewcraft.wakame.reforge.common.ReforgeLoggerPrefix
import cc.mewcraft.wakame.reforge.mod.ModdingSession
import cc.mewcraft.wakame.util.hideTooltip
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.inventory.VirtualInventory
import xyz.xenondevs.invui.inventory.event.ItemPreUpdateEvent
import xyz.xenondevs.invui.inventory.event.UpdateReason
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.ItemWrapper
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.item.impl.SimpleItem

/**
 * 用于定制*单个*词条栏核心的菜单, 需要被嵌入到 [ModdingMenu] 中.
 *
 * 物品上的*每个*词条栏都有一个对应的 [ReplaceMenu] 实例来处理定制.
 */
internal class ReplaceMenu(
    private val parent: ModdingMenu,
    private val replace: ModdingSession.Replace,
) : KoinComponent {
    companion object {
        private const val PREFIX = ReforgeLoggerPrefix.MOD
    }

    private val logger: Logger by inject()
    private val viewer: Player = parent.viewer

    private val inputSlot: VirtualInventory = VirtualInventory(intArrayOf(1))
    private val primaryGui: Gui = Gui.normal { builder ->
        // a: 被定制对象的预览物品
        // b: 接收玩家输入的虚拟容器
        // *: 起视觉引导作用的物品
        builder.setStructure("a * b")
        builder.addIngredient('a', ViewItem(replace))
        builder.addIngredient('b', inputSlot)
        builder.addIngredient('*', SimpleItem(ItemStack(Material.WHITE_STAINED_GLASS_PANE).hideTooltip(true)))
    }

    init {
        inputSlot.setPreUpdateHandler(::onInputInventoryPreUpdate)
        inputSlot.guiPriority = 10
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
        logger.info("$PREFIX Replace input updating: ${prevItem?.type} -> ${newItem?.type}")

        when {
            // 玩家尝试交换 inputSlot 中的物品:
            event.isSwap -> {
                event.isCancelled = true
                viewer.sendPlainMessage("猫咪不可以!")
            }

            // 玩家尝试向 inputSlot 中添加物品:
            event.isAdd -> {
                val stack = newItem?.tryNekoStack ?: run {
                    viewer.sendPlainMessage("猫咪不可以!")
                    event.isCancelled = true
                    return
                }

                // 执行一次替换流程, 并获取其结果
                val result = replace.executeReplace(stack)

                // 将容器里的物品替换成渲染后的物品
                val rendered = ReplaceRender.render(result)
                event.newItem = rendered

                // 执行一次定制流程
                parent.moddingSession.executeReforge()

                // 更新主菜单的状态
                parent.updateOutput()

                // 重置确认状态
                parent.confirmed = false
            }

            // 玩家尝试从 inputSlot 中移除物品
            event.isRemove -> {
                event.isCancelled = true
                setInputSlot(null)

                val ingredient = replace.latestResult.ingredient ?: run {
                    logger.error("$PREFIX Ingredient is null, but an item is being removed from the replace menu. This is a bug!")
                    return
                }

                // 将用于定制的物品退给玩家
                viewer.inventory.addItem(ingredient.itemStack)

                // 更新本定制的结果
                replace.executeReplace(null)

                // 执行一次定制流程
                parent.moddingSession.executeReforge()

                // 更新主菜单
                parent.updateOutput()

                // 重置确认状态
                parent.confirmed = false
            }
        }
    }

    private fun setInputSlot(item: ItemStack?) {
        inputSlot.setItem(UpdateReason.SUPPRESSED, 0, item)
    }

    private class ViewItem(
        val replace: ModdingSession.Replace,
    ) : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            return ItemWrapper(replace.display)
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            // NOP
        }
    }
}