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
 * 按照整体设计 (假设每一步都是符合要求的):
 * - 玩家首先要将需要定制的物品放入 [inputInventory]
 * - 之后, 主菜单中便会出现物品上每一个词条栏的*子菜单*
 * - 在每一个关于词条栏的子菜单中
 *    - 玩家可以看到子菜单所关联的词条栏的核心/诅咒信息
 *    - 玩家可以将一个便携式物品放入子菜单的*输入容器*中
 *    - 这相当于告诉系统: 我要消耗这个物品来定制这个词条栏
 * - 主菜单的 [outputInventory] 会实时显示定制之后的物品
 * - 玩家可以随时将定制后的物品从 [outputInventory] 中取出
 * - 如果玩家取出 [outputInventory] 中的物品, 则相当于完成定制, 同时会消耗掉所有的输入容器中的物品
 *
 * @param T 定制的类型
 */
abstract class ModdingMenu<T> {
    /**
     * 日志记录器.
     */
    protected abstract val logger: Logger

    /**
     * 菜单的使用者.
     */
    protected abstract val viewer: Player

    /**
     * [RecipeMenu] 的构造函数.
     */
    protected abstract fun recipeMenuConstructor(
        parentMenu: ModdingMenu<T>,
        viewer: Player,
        recipe: ModdingSession.Recipe<T>,
    ): RecipeMenu<T>

    /**
     * 用于输入被定制物品的容器.
     *
     * 玩家放入定制台的物品将会被放入这个容器中.
     */
    private val inputInventory: VirtualInventory = VirtualInventory(/* maxStackSizes = */ intArrayOf(1))

    /**
     * 用于输出定制后物品的容器.
     *
     * 定制后的物品将会被放入这个容器中.
     */
    private val outputInventory: VirtualInventory = VirtualInventory(/* maxStackSizes = */ intArrayOf(1))

    /**
     * 主菜单的 [Gui].
     */
    private val primaryGui: ScrollGui<Gui> = ScrollGui.guis { builder ->
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
    private val primaryWindow: Window.Builder.Normal.Single = Window.single().apply {
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
    var currentSession: ModdingSession<T>? = null

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
                        viewer.sendPlainMessage("请放入一个拥有核心的物品!")
                        return@pre
                    }

                    // 创建会话, 并赋值到成员变量上
                    val session = ModdingSession.of<T>(stack).also {
                        currentSession = it
                    }

                    val recipeGuis = session.recipes
                        .map { (_, recipe) ->
                            recipeMenuConstructor(this, viewer, recipe)
                        }
                        .map { it.createdGui }
                    // 更新主菜单的内容
                    fillRecipes(recipeGuis)
                    // 设置输出容器的物品
                    refreshOutputInventory()
                }

                // Case 3: 玩家将物品从输入槽位取出
                event.isRemove -> {
                    // 玩家将物品从输入容器取出, 意味着定制过程被中途终止了.
                    // 我们需要把玩家放入定制台的所有物品*原封不动*的归还给玩家.

                    val session = currentSession ?: run {
                        event.isCancelled = true
                        logger.error("Modding session (viewer: ${viewer.name}) is null, but input item is being removed. This is a bug!")
                        return@pre
                    }

                    // 把主菜单的内容清空
                    clearRecipes()
                    // 清空输出容器的物品
                    clearOutputSlot()

                    // 玩家把物品从输入容器中拿出来时, 我们不能直接让该操作自然的发生.
                    // 因为输入容器中的物品可能是经过修改的, 而不是玩家物品的原始状态.
                    // 因此这里: 清空输入容器, 然后 setItemOnCurse
                    clearInputSlot()
                    setItemOnCursor(session.input.handle)

                    // 归还定制过程中放入定制台的其他物品
                    session.recipes
                        .mapNotNull { (_, recipe) ->
                            recipe.input?.handle
                        }
                        .forEach { itemStack ->
                            viewer.inventory.addItem(itemStack)
                        }

                    // 冻结 session
                    session.frozen = true
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
                    val session = currentSession
                    if (session == null) {
                        event.isCancelled = true
                        logger.error("Modding session (viewer: ${viewer.name}) is null, but output item is being removed. This is a bug!")
                        return@pre
                    }

                    // 玩家必须点两次才能取出定制后的物品
                    if (!session.confirmed) {
                        event.isCancelled = true
                        session.confirmed = true
                        return@pre
                    }

                    // 如果玩家取出定制后的物品, 意味着定制过程即将完成.
                    // 我们需要消耗掉所有的材料, 然后替换玩家指针上的物品.
                    val output = session.output ?: run {
                        event.isCancelled = true
                        logger.error("Output item is null while player is trying to take it. This is a bug!")
                        return@pre
                    }

                    // 将玩家指针上的物品替换为定制后的物品
                    setItemOnCursor(output.handle)
                    // 清空 inputInventory 中的物品 (相当于消耗掉原始物品),
                    clearInputSlot()
                    // 清空 RecipeGui 中输入容器的物品 (相当于消耗掉所需材料),
                    clearRecipes()
                    // 冻结会话
                    session.frozen = true
                    // 让会话被 GC
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
                logger.info("The window of modding menu is closed while session being null.")
                return@close
            }

            // 有会话, 则将定制过程中玩家输入的物品归还给玩家
            viewer.inventory.addItem(session.input.handle)
            viewer.inventory.addItem(*session.recipes.getInputItems().toTypedArray())
        }
        primaryWindow.addOpenHandler open@{
            viewer.playSound(Sound.sound().type(org.bukkit.Sound.BLOCK_ANVIL_PLACE).volume(1f).pitch(0f).build())
        }
    }

    private fun setItemOnCursor(stack: ItemStack) {
        viewer.setItemOnCursor(stack)
    }

    private fun fillRecipes(guis: List<Gui>) {
        primaryGui.setContent(guis)
    }

    private fun clearRecipes() {
        primaryGui.setContent(null)
    }

    private fun fillInputSlot(stack: ItemStack) {
        inputInventory.setItem(UpdateReason.SUPPRESSED, 0, stack)
    }

    private fun clearInputSlot() {
        inputInventory.setItem(UpdateReason.SUPPRESSED, 0, null)
    }

    private fun fillOutputSlot(stack: ItemStack) {
        outputInventory.setItem(UpdateReason.SUPPRESSED, 0, stack)
    }

    private fun clearOutputSlot() {
        outputInventory.setItem(UpdateReason.SUPPRESSED, 0, null)
    }

    /**
     * 基于当前的所有状态, 更新输出槽位的物品.
     */
    fun refreshOutputInventory() {
        val session = currentSession ?: return
        val result = session.reforge()
        val output = result.modded
        fillOutputSlot(output.handle)
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