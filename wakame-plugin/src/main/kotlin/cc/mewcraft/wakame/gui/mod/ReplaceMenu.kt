package cc.mewcraft.wakame.gui.mod

import cc.mewcraft.wakame.display2.ItemRenderers
import cc.mewcraft.wakame.display2.implementation.modding_table.ModdingTableContext
import cc.mewcraft.wakame.item.tryNekoStack
import cc.mewcraft.wakame.reforge.common.CoreIcons
import cc.mewcraft.wakame.reforge.mod.ModdingSession
import cc.mewcraft.wakame.util.*
import me.lucko.helper.text3.mini
import net.kyori.adventure.extra.kotlin.text
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.inventory.VirtualInventory
import xyz.xenondevs.invui.inventory.event.ItemPreUpdateEvent
import xyz.xenondevs.invui.inventory.event.UpdateReason
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.ItemWrapper
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.item.impl.SimpleItem

/**
 * 用于定制*单个*核孔核心的菜单, 需要被嵌入到 [ModdingMenu] 中.
 *
 * 物品上的*每个*核孔都有一个对应的 [ReplaceMenu] 实例来处理定制.
 */
internal class ReplaceMenu
private constructor(
    private val parent: ModdingMenu,
    private val replace: ModdingSession.Replace,
) : KoinComponent {
    companion object {
        private val MESSAGE_CANCELLED = text { content("猫咪不可以!"); color(NamedTextColor.RED) }

        operator fun invoke(parent: ModdingMenu, replace: ModdingSession.Replace): Gui {
            return ReplaceMenu(parent, replace).primaryGui
        }
    }

    private val viewer: Player = parent.viewer
    private val inputSlot: VirtualInventory = VirtualInventory(intArrayOf(1)).apply {
        setPreUpdateHandler(::onInputInventoryPreUpdate)
        guiPriority = 10
    }
    private val primaryGui: Gui = Gui.normal { builder ->
        // a: 被定制对象的预览物品
        // b: 接收玩家输入的虚拟容器
        // *: 起视觉引导作用的物品
        builder.setStructure("a * b")
        builder.addIngredient('a', ViewItem(replace))
        builder.addIngredient('b', inputSlot)
        builder.addIngredient('*', SimpleItem(ItemStack(Material.WHITE_STAINED_GLASS_PANE).hideTooltip(true)))
    }

    /**
     * 当输入容器中的物品发生*变化前*调用.
     */
    private fun onInputInventoryPreUpdate(event: ItemPreUpdateEvent) {
        val prevItem = event.previousItem
        val newItem = event.newItem
        parent.logger.info("Replace input updating: ${prevItem?.type} -> ${newItem?.type}")

        if (parent.session.frozen) {
            parent.logger.error("The modding session is frozen, but the player is trying to interact with the replace's input slot. This is a bug!")
            event.isCancelled = true
            return
        }

        when {
            // 玩家尝试交换 inputSlot 中的物品:
            event.isSwap -> {
                event.isCancelled = true
                viewer.sendMessage(MESSAGE_CANCELLED)
            }

            // 玩家尝试向 inputSlot 中添加物品:
            event.isAdd -> {
                val addedNekoStack = newItem?.tryNekoStack ?: run {
                    viewer.sendMessage(MESSAGE_CANCELLED)
                    event.isCancelled = true
                    return
                }

                // 执行一次替换流程
                replace.executeReplace(addedNekoStack)

                // 重新渲染放入的物品
                event.newItem = renderInputSlot(replace)

                // 执行一次定制流程
                parent.executeReforge()

                // 更新主菜单的状态
                parent.updateInputSlot()
                parent.updateOutputSlot()

                // 重置确认状态
                parent.confirmed = false
            }

            // 玩家尝试从 inputSlot 中移除物品
            event.isRemove -> {
                event.isCancelled = true

                val ingredient = replace.latestResult.ingredient ?: run {
                    parent.logger.error("Ingredient is null, but an item is being removed from the replace menu. This is a bug!")
                    return
                }

                // 将用于定制的物品退给玩家
                viewer.inventory.addItem(ingredient.itemStack)

                // 更新本定制的结果
                replace.executeReplace(null)

                // 更新本菜单的内容
                setInputSlot(null)

                // 执行一次定制流程
                parent.executeReforge()

                // 更新主菜单
                parent.updateInputSlot()
                parent.updateOutputSlot()

                // 重置确认状态
                parent.confirmed = false
            }
        }
    }

    @Suppress("SameParameterValue")
    private fun setInputSlot(item: ItemStack?) {
        inputSlot.setItem(UpdateReason.SUPPRESSED, 0, item)
    }

    private fun renderInputSlot(replace: ModdingSession.Replace): ItemStack {
        val replaceResult = replace.latestResult
        val ingredient = replaceResult.ingredient

        val clickToWithdraw = "<gray>点击取回核心".mini

        if (replaceResult.applicable) {
            // 耗材可用于定制

            if (ingredient == null) {
                // 出现内部错误

                return ItemStack.of(Material.BARRIER).edit {
                    itemName = "<white>结果: <red>内部错误".mini
                    lore = listOf(
                        Component.empty(),
                        clickToWithdraw
                    ).removeItalic
                }
            } else {
                // 正常情况

                // 重新渲染耗材
                ItemRenderers.MODDING_TABLE.render(ingredient, ModdingTableContext.ReplaceInputSlot(replace))

                // 加入其他信息
                return ingredient.itemStack.edit {
                    customName = null // 玩家可能改了名, 所以清除一下
                    itemName = "<white>结果: <green>就绪".mini
                    lore = buildList {
                        lore?.map { it.colorRecursively(NamedTextColor.DARK_GRAY) }?.let(::addAll)
                        add(Component.empty())
                        add(clickToWithdraw)
                    }.removeItalic
                }
            }
        } else {
            // 耗材不可用于定制

            return ItemStack.of(Material.BARRIER).edit {
                itemName = "<white>结果: <red>无效".mini
                lore = buildList {
                    addAll(replaceResult.description)
                    add(Component.empty())
                    add(clickToWithdraw)
                }.removeItalic
            }
        }
    }

    private class ViewItem(
        val replace: ModdingSession.Replace,
    ) : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            val core = replace.cell.getCore()
            val changeable = replace.changeable
            val coreIcon = CoreIcons.get(core)
            val itemStack = coreIcon.edit {
                itemName =
                    if (changeable) {
                        core.displayName
                    } else {
                        core.displayName.append(
                            text { content(" (不可修改)"); color(NamedTextColor.RED) }
                        )
                    }
                lore = core.description
            }

            return ItemWrapper(itemStack)
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            // NOP
        }
    }
}