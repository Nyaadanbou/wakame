package cc.mewcraft.wakame.gui.mod

import cc.mewcraft.wakame.adventure.translator.TranslatableMessages
import cc.mewcraft.wakame.display2.ItemRenderers
import cc.mewcraft.wakame.display2.implementation.modding_table.ModdingTableContext
import cc.mewcraft.wakame.reforge.common.CoreIcons
import cc.mewcraft.wakame.reforge.mod.ModdingSession
import cc.mewcraft.wakame.util.edit
import cc.mewcraft.wakame.util.itemLoreOrEmpty
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.inventory.VirtualInventory
import xyz.xenondevs.invui.inventory.event.ItemPreUpdateEvent
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.ItemWrapper
import xyz.xenondevs.invui.item.impl.AbstractItem

/**
 * 用于定制*单个*核孔核心的菜单, 需要被嵌入到 [ModdingMenu] 中.
 *
 * 物品上的*每个*核孔都有一个对应的 [ReplaceMenu] 实例来处理定制.
 */
internal class ReplaceMenu(
    val parent: ModdingMenu,
    val replace: ModdingSession.Replace,
) {

    private val viewer: Player = parent.viewer
    private val inputSlot: VirtualInventory = VirtualInventory(intArrayOf(1)).apply {
        setPreUpdateHandler(::onInputInventoryPreUpdate)
        guiPriority = 10
    }

    val primaryGui: Gui = Gui.normal { builder ->
        // a: 被定制对象的预览物品
        // b: 接收玩家输入的虚拟容器
        // *: 起视觉引导作用的物品
        builder.setStructure("a * b")
        builder.addIngredient('a', ViewItem(replace))
        builder.addIngredient('b', inputSlot)
        builder.addIngredient('*', parent.table.replaceMenuSettings.getSlotDisplay("compatibility_view").resolveToItemWrapper())
    }

    /**
     * 当输入容器中的物品发生*变化前*调用.
     */
    private fun onInputInventoryPreUpdate(event: ItemPreUpdateEvent) {
        val prevItem = event.previousItem
        val newItem = event.newItem
        // parent.logger.info("Replace input updating: ${prevItem?.type} -> ${newItem?.type}")

        if (parent.session.frozen) {
            parent.logger.error("The modding session is frozen, but the player is trying to interact with the replace's input slot. This is a bug!")
            event.isCancelled = true
            return
        }

        when {
            // 玩家尝试交换 inputSlot 中的物品:
            event.isSwap -> {
                event.isCancelled = true
                viewer.sendMessage(TranslatableMessages.MSG_ERR_CANCELLED)
            }

            // 玩家尝试向 inputSlot 中添加物品:
            event.isAdd -> {
                // 执行一次替换流程
                replace.originalInput = newItem

                // 重新渲染放入的物品
                event.newItem = renderInputItem()

                parent.refreshReplaceGuis(this)
                parent.executeReforge()
                parent.updateInputSlot()
                parent.updateOutputSlot()
                parent.confirmed = false
            }

            // 玩家尝试从 inputSlot 中移除物品:
            event.isRemove -> {
                event.isCancelled = true

                val inputItem = replace.originalInput ?: run {
                    parent.logger.error("Ingredient is null, but an item is being removed from the replace menu. This is a bug!")
                    return
                }

                // 将定制的耗材还给玩家
                viewer.inventory.addItem(inputItem)

                // 更新本定制的状态
                replace.originalInput = null

                // 更新本菜单的内容
                updateInputSlot()

                parent.refreshReplaceGuis(this)
                parent.executeReforge()
                parent.updateInputSlot()
                parent.updateOutputSlot()
                parent.confirmed = false
            }
        }
    }

    /**
     * 更新 [inputSlot] 中的物品.
     * 这将从 [replace] 读取最新的数据.
     */
    fun updateInputSlot() {
        setInputSlot(renderInputItem())
    }

    private fun renderInputItem(): ItemStack {
        val originalInput = replace.originalInput
        if (originalInput == null) {
            // 没有任何耗材输入:
            return ItemStack.empty()
        }

        val replaceResult = replace.latestResult
        val usableInput = replace.usableInput
        if (usableInput == null) {
            // 耗材不可用于定制:

            val resolved = parent.table.replaceMenuSettings.getSlotDisplay("core_unusable").resolveEverything {
                folded("result_description", replaceResult.description)
            }

            resolved.applyTo(
                originalInput
            ).edit {
                // originalInput 虽然无法定制, 但可能是一个合法的萌芽物品.
                // 为了避免被发包系统接管, 我们直接把 `custom_data` 删掉.
                customData = null
            }

            return originalInput

        } else {
            // 耗材可用于定制:

            // 重新渲染耗材
            val session = parent.session
            val context = ModdingTableContext.Replace(session, replace)
            ItemRenderers.MODDING_TABLE.render(usableInput, context)

            // 使用 SlotDisplay 再处理一遍
            val resolved = parent.table.replaceMenuSettings.getSlotDisplay("core_usable").resolveEverything {
                folded("item_lore", usableInput.wrapped.itemLoreOrEmpty)
                folded("result_description", replaceResult.description)
            }

            return resolved.applyTo(usableInput.wrapped)
        }
    }

    private fun setInputSlot(item: ItemStack?) {
        inputSlot.setItemSilently(0, item)
    }

    private inner class ViewItem(
        val replace: ModdingSession.Replace,
    ) : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            val core = replace.cell.core
            val icon = CoreIcons.getItemStack(core)
            val resolved = parent.table.replaceMenuSettings.getSlotDisplay("core_view").resolveEverything {
                standard { component("core_name", core.displayName) }
                folded("core_description", core.description)
            }
            resolved.applyTo(icon)
            return ItemWrapper(icon)
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            // NOP
        }
    }
}