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
abstract class RecipeMenu<T> : KoinComponent {
    protected val logger: Logger by inject()

    protected abstract val viewer: Player
    protected abstract val parentMenu: ModdingMenu<T>
    protected abstract val targetRecipe: ModdingSession.Recipe<T>
    protected abstract fun getPortableObject(stack: NekoStack): PortableObject<T>?

    protected val inputInventory: VirtualInventory = VirtualInventory(1)
    protected val recipeGui: Gui = Gui.normal { builder ->
        // a: 定制对象的预览物品
        // b: 接收玩家输入的物品的容器
        // *: 起视觉引导作用的物品
        builder.setStructure("a * b")

        builder.addIngredient('a', viewItemConstructor(targetRecipe))
        builder.addIngredient('b', inputInventory)
        builder.addIngredient('*', SimpleItem(ItemStack(Material.SUNFLOWER).hideTooltip(true)))
    }

    protected abstract fun viewItemConstructor(recipe: ModdingSession.Recipe<T>): Item

    /**
     * 创建的 [Gui].
     */
    val createdGui: Gui
        get() = recipeGui

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
                    val stack = newItem?.tryNekoStack ?: run {
                        viewer.sendMessage("请放入一个萌芽物品!")
                        event.isCancelled = true
                        return@pre
                    }

                    val portableObject = getPortableObject(stack) ?: run {
                        viewer.sendMessage("请放入一个便携式物品!")
                        event.isCancelled = true
                        return@pre
                    }

                    val result = targetRecipe.test(portableObject.wrapped)
                    if (result.isFailure) {
                        viewer.sendMessage("无法将该修改应用到词条栏上!")
                        event.isCancelled = true
                        return@pre
                    }

                    // 保存物品快照
                    targetRecipe.input = stack
                    // 通知主菜单, 更新输出容器里的物品
                    parentMenu.refreshOutputInventory()
                }

                // Case 3: 玩家从输入容器中移除物品
                event.isRemove -> {
                    // 清空物品快照
                    targetRecipe.input = null
                }
            }
        }
        inputInventory.setPostUpdateHandler post@{ event ->
            val prevItem = event.previousItem
            val newItem = event.newItem
            logger.info("Recipe input updated: ${prevItem?.type} -> ${newItem?.type}")
        }
    }
}