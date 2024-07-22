package cc.mewcraft.wakame.gui.modding

import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.tryNekoStack
import cc.mewcraft.wakame.reforge.modding.session.ModdingSession
import cc.mewcraft.wakame.util.hideTooltip
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.ScrollGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.inventory.VirtualInventory
import xyz.xenondevs.invui.inventory.event.UpdateReason
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.ItemWrapper
import xyz.xenondevs.invui.item.impl.SimpleItem
import xyz.xenondevs.invui.item.impl.controlitem.ScrollItem
import xyz.xenondevs.invui.window.Window
import xyz.xenondevs.invui.window.type.context.setTitle

/**
 * 定制台的主菜单, 也是定制台玩家界面的代码入口.
 *
 * @param T 定制的类型
 */
abstract class ModdingMenu<T> : KoinComponent {
    /**
     * 菜单的使用者.
     */
    abstract val viewer: Player

    /**
     * [RecipeMenu] 的构造函数.
     */
    abstract fun recipeMenuConstructor(mainMenu: ModdingMenu<T>, viewer: Player, recipe: ModdingSession.Recipe<T>): RecipeMenu<T>

    /**
     * 日志记录器.
     */
    protected val logger: Logger by inject()

    /**
     * 用于输入被定制物品的容器.
     *
     * 玩家放入定制台的物品将会被放入这个容器中.
     */
    protected val inputInventory: VirtualInventory = VirtualInventory(/* maxStackSizes = */ intArrayOf(1))

    /**
     * 用于输出定制后物品的容器.
     *
     * 定制后的物品将会被放入这个容器中.
     */
    protected val outputInventory: VirtualInventory = VirtualInventory(/* maxStackSizes = */ intArrayOf(1))

    /**
     * 主菜单的 [Gui].
     */
    protected val primaryGui: ScrollGui<Gui> = ScrollGui.guis { builder ->
        builder.setStructure(
            ". . . x x x . . .",
            ". . . x x x . . .",
            ". i . x x x . o .",
            ". . . x x x . . .",
            ". . . x x x . . .",
            "# # # < # > # # #"
        )
        builder.addIngredient('.', SimpleItem(ItemStack(Material.BLACK_STAINED_GLASS_PANE).hideTooltip(true)))
        builder.addIngredient('#', SimpleItem(ItemStack(Material.GREEN_STAINED_GLASS_PANE).hideTooltip(true)))
        builder.addIngredient('i', inputInventory)
        builder.addIngredient('o', outputInventory)
        builder.addIngredient('<', PrevItem())
        builder.addIngredient('>', NextItem())
        builder.addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
    }

    /**
     * 主菜单的 [Window].
     */
    protected val primaryWindow: Window.Builder.Normal.Single = Window.single().apply {
        setGui(primaryGui)
        setTitle(text("定制台").decorate(TextDecoration.BOLD))
    }

    /**
     * 正在进行中的定制.
     *
     * 初始值为 `null`, 因为玩家刚打开定制台时, 应该是没有任何正在进行的定制.
     * 当玩家放入需要定制的物品到定制台时, 会创建一个 [ModdingSession],
     * 并且实例会赋值到这个属性上.
     */
    protected var currentSession: ModdingSession<T>? = null

    // 添加输入容器的 handlers
    init {
        inputInventory.setPreUpdateHandler pre@{ event ->
            val newItem = event.newItem
            val prevItem = event.previousItem
            logger.info("Input item updating: ${prevItem?.type} -> ${newItem?.type}")

            when {
                // Case 1: 玩家交换输入槽位中的物品
                event.isSwap -> {
                    event.isCancelled = true // 简化操作, 防止BUG
                    viewer.sendMessage("猫咪不可以!")
                }

                // Case 2: 玩家将物品放入输入槽位
                event.isAdd -> {
                    // 不是 NekoStack - 返回
                    val stack = newItem?.tryNekoStack ?: run {
                        viewer.sendPlainMessage("请放入一个萌芽物品!")
                        return@pre
                    }

                    // 不是有词条栏的物品 - 返回
                    if (stack.components.has(ItemComponentTypes.CELLS)) {
                        viewer.sendPlainMessage("请放入一个有核心的物品!")
                        return@pre
                    }

                    // 创建会话, 并赋值到成员变量上
                    val session = ModdingSession.of<T>(stack).also {
                        currentSession = it
                    }

                    val recipeGuis = ArrayList<Gui>(session.recipes.size)
                    val recipes = session.recipes
                    recipes.forEach { (_, recipe) ->
                        val recipeMenu = recipeMenuConstructor(this, viewer, recipe)
                        val recipeGui = recipeMenu.createdGui
                        recipeGuis += recipeGui
                    }
                    // 更新主菜单的内容
                    primaryGui.setContent(recipeGuis)
                    // 设置输出容器的物品
                    outputInventory.setItem(UpdateReason.SUPPRESSED, 0, ItemStack(Material.BARRIER).hideTooltip(true))
                }

                // Case 3: 玩家将物品从输入槽位取出
                event.isRemove -> {
                    // 玩家将物品从输入容器取出, 意味着定制过程被中途终止了.
                    // 我们需要把玩家放入定制台的所有物品*原封不动*的归还给玩家.

                    val session = currentSession ?: run {
                        event.isCancelled = true
                        logger.error("Modding session is null, but input item is removed. This is a bug! Current viewer: ${viewer.name}")
                        return@pre
                    }

                    // 把主菜单的内容清空
                    primaryGui.setContent(null)
                    // 清空输出容器的物品
                    outputInventory.setItem(UpdateReason.SUPPRESSED, 0, null)

                    // 输入容器中的物品已经由玩家自己拿出来了,
                    // 因此不需要再将输入容器里的物品归还给玩家.
                    // session.inputSnapshot.itemStack.let { viewer.inventory.addItem(it) }

                    // 归还定制过程中放入定制台的其他物品
                    session.recipes
                        .mapNotNull { (_, recipe) ->
                            recipe.input
                        }
                        .map { nekoStack ->
                            nekoStack.itemStack
                        }
                        .forEach { itemStack ->
                            viewer.inventory.addItem(itemStack)
                        }

                    // 最后把 session 置为 null (让其被 GC)
                    currentSession = null
                }
            }
        }
        inputInventory.setPostUpdateHandler post@{ event ->
            val newItem = event.newItem
            val prevItem = event.previousItem
            logger.info("Input item updated: ${prevItem?.type} -> ${newItem?.type}")
        }
    }

    // 添加输出容器的 handler
    init {
        outputInventory.setPreUpdateHandler pre@{ event ->
            logger.info("Output item updating: ${event.previousItem?.type} -> ${event.newItem?.type}")
            when {
                // Case 1: 玩家向输出容器中添加物品
                event.isSwap || event.isAdd -> {
                    event.isCancelled = true
                }

                // Case 2: 玩家从输出容器中取出物品
                event.isRemove -> {
                    currentSession?.run {
                        logger.error("Modding session is null, but output item is removed. This is a bug!")
                    }
                    // 当玩家从输出容器取出物品时:
                    // - inputItemInventory 中的物品需要清空 (相当于消耗掉原始物品),
                    inputInventory.setItemSilently(0, null)
                    // - 所有的 RecipeGui 中的输入需要清空 (相当于消耗掉所需材料),
                    primaryGui.setContent(null)
                    // - 最后把 session 置为 null (让其被 GC)
                    currentSession = null
                }
            }
        }
        outputInventory.setPostUpdateHandler post@{ event ->
            logger.info("Output item updated: ${event.previousItem?.type} -> ${event.newItem?.type}")
        }
    }

    // 添加窗口的 handler
    init {
        primaryWindow.addCloseHandler close@{
            // 如果当前没有会话, 则直接返回
            val session = currentSession ?: run {
                return@close
            }

            // 有会话, 则将定制过程中玩家输入的物品归还给玩家
            viewer.inventory.addItem(session.input.itemStack)
            viewer.inventory.addItem(*session.recipes.getInputItems().toTypedArray())
        }
        primaryWindow.addOpenHandler open@{
            viewer.playSound(Sound.sound().type(org.bukkit.Sound.BLOCK_ANVIL_PLACE).volume(1f).pitch(0f).build())
        }
    }

    /**
     * 基于当前的所有状态, 刷新输出槽位的物品.
     */
    fun refreshOutputInventory() {
        val session = currentSession ?: return
        val result = session.reforge()
        val output = result.modded
        outputInventory.setItem(UpdateReason.SUPPRESSED, 0, output.handle)
    }

    /**
     * 向指定玩家打开定制台的主界面.
     */
    fun open() {
        primaryWindow.open(viewer)
    }

    /**
     * 向前翻页的 [Item].
     */
    class PrevItem : ScrollItem(-1) {
        override fun getItemProvider(gui: ScrollGui<*>): ItemProvider {
            val stack = ItemStack(Material.ARROW)
            stack.editMeta { it.itemName(text("Prev")) }
            return ItemWrapper(stack)
        }
    }

    /**
     * 向后翻页的 [Item].
     */
    class NextItem : ScrollItem(1) {
        override fun getItemProvider(gui: ScrollGui<*>): ItemProvider {
            val stack = ItemStack(Material.ARROW)
            stack.editMeta { it.itemName(text("Next")) }
            return ItemWrapper(stack)
        }
    }
}