package cc.mewcraft.wakame.gui.modding

import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.bypassPacket
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.toNekoStack
import cc.mewcraft.wakame.item.tryNekoStack
import cc.mewcraft.wakame.reforge.modding.ModdingSession
import cc.mewcraft.wakame.reforge.modding.ModdingTable
import cc.mewcraft.wakame.util.hideTooltip
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.slf4j.Logger
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.ScrollGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.inventory.VirtualInventory
import xyz.xenondevs.invui.inventory.event.ItemPostUpdateEvent
import xyz.xenondevs.invui.inventory.event.ItemPreUpdateEvent
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.ItemWrapper
import xyz.xenondevs.invui.item.impl.SimpleItem
import xyz.xenondevs.invui.item.impl.controlitem.ScrollItem
import xyz.xenondevs.invui.window.Window
import xyz.xenondevs.invui.window.type.context.setTitle

/**
 * 定制台的主菜单, 也是玩家打开定制台后的代码入口.
 *
 * ## 用户流程
 * 按照整体设计 (假设每一步都没有意外发生):
 * - 玩家首先要将需要定制的物品放入 [inputInventory]
 * - 然后主菜单中便会出现若干个*子菜单*, 每个*子菜单*对应物品上的一个词条栏
 * - 在每一个子菜单中:
 *    - 玩家可以看到子菜单所关联的词条栏信息
 *    - 玩家可以将一个便携式物品放入子菜单的*输入容器*中
 *    - 这相当于告诉定制台: 我要消耗这个物品来定制这个词条栏
 * - 主菜单的 [outputInventory] 会实时显示定制之后的物品
 * - 玩家可以随时将定制后的物品从 [outputInventory] 中取出
 * - 如果玩家取出 [outputInventory] 中的物品, 则相当于完成定制, 同时会消耗掉所有的输入容器中的物品
 *
 * ## 实现原则
 * - GUI 里面玩家能看到的所有物品必须全部都是深度克隆
 * - 所有被实际操作的物品均储存在 [ModdingSession]
 *
 * @param T 定制的类型
 */
abstract class ModdingMenu<T> {
    /**
     * 日志记录器.
     */
    protected abstract val logger: Logger

    /**
     * 该菜单所依赖的定制台.
     */
    protected abstract val table: ModdingTable

    /**
     * 该菜单的用户, 也就是正在查看该菜单的玩家.
     */
    protected abstract val viewer: Player

    /**
     * [ModdingSession] 的构造函数.
     */
    protected abstract fun createModdingSession(
        viewer: Player, input: NekoStack,
    ): ModdingSession<T>? // 返回 null 代表不支持定制

    /**
     * [RecipeMenu] 的构造函数.
     */
    protected abstract fun createRecipeMenu(
        parentMenu: ModdingMenu<T>, viewer: Player, recipeSession: ModdingSession.RecipeSession<T>,
    ): RecipeMenu<T>

    /**
     * 向指定玩家打开定制台的主界面.
     */
    fun open() {
        primaryWindow.open(viewer)
    }

    /**
     * 获取当前的定制会话.
     */
    fun getSession(): ModdingSession<T>? {
        return currentSession
    }

    /**
     * 基于当前的所有状态, 更新输出槽位的物品.
     */
    fun refreshOutput() {
        val session = getSession() ?: run {
            logger.info("Trying to refresh output inventory while session being null.")
            return
        }
        val result = session.reforge()
        val copy = result.copy
        fillOutputSlot(copy.unsafe.handle)
    }

    /**
     * 用于输入被定制物品的容器.
     *
     * 我们通过这个容器来接收玩家放入定制台的物品.
     */
    private val inputInventory: VirtualInventory = VirtualInventory(/* maxStackSizes = */ intArrayOf(1))

    /**
     * 用于输出定制后物品的容器.
     *
     * 我们通过这个容器将定制后的物品给予玩家.
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
     * 当玩家放入需要定制的物品到定制台时, 实现应该创建一个 [ModdingSession],
     * 并且把实例赋值到这个属性上.
     */
    private var currentSession: ModdingSession<T>? = null

    init {
        // 添加输入容器的 handlers
        inputInventory.setPreUpdateHandler(::onInputInventoryPreUpdate)
        inputInventory.setPostUpdateHandler(::onInputInventoryPostUpdate)

        // 添加输出容器的 handlers
        outputInventory.setPreUpdateHandler(::onOutputInventoryPreUpdate)
        outputInventory.setPostUpdateHandler(::onOutputInventoryPostUpdate)

        // 添加窗口的 handlers
        primaryWindow.addCloseHandler(::onWindowClose)
        primaryWindow.addOpenHandler(::onWindowOpen)
    }

    /**
     * 设置当前的定制会话.
     */
    private fun setSession(session: ModdingSession<T>) {
        currentSession = session
    }

    /**
     * 丢弃当前的定制会话.
     */
    private fun dropSession() {
        currentSession = null
    }

    /**
     * 当输入容器中的物品发生*变化前*调用.
     */
    protected open fun onInputInventoryPreUpdate(event: ItemPreUpdateEvent) {
        val newItem = event.newItem
        val prevItem = event.previousItem
        logger.info("Input item updating: ${prevItem?.type} -> ${newItem?.type}")

        when {
            // Case 1: 玩家交换输入槽位中的物品
            event.isSwap -> {
                event.isCancelled = true
                viewer.sendMessage("猫咪不可以!")
            }

            // Case 2: 玩家将物品放入输入槽位
            event.isAdd -> {
                // 不是 NekoStack - 返回
                val stack = newItem?.tryNekoStack ?: run {
                    event.isCancelled = true
                    viewer.sendPlainMessage("请放入一个萌芽物品!")
                    return
                }

                // 不是有词条栏的物品 - 返回
                if (!stack.components.has(ItemComponentTypes.CELLS)) {
                    event.isCancelled = true
                    viewer.sendPlainMessage("请放入一个拥有词条栏的物品!")
                    return
                }

                // 创建会话
                val session = createModdingSession(viewer, stack) ?: run {
                    event.isCancelled = true
                    viewer.sendPlainMessage("该物品不支持定制!")
                    return
                }
                // 设置会话
                setSession(session)

                // 由于把物品放入容器的方式有好几种,
                // 必须让玩家“自然的”将物品放进容器,
                // 否则处理起来太麻烦.

                // 将*克隆*放进输入容器
                event.newItem = stack.itemStack

                // 更新主菜单的内容
                val recipeMenus = session.recipeSessions.map { (_, recipeSession) ->
                    createRecipeMenu(this, viewer, recipeSession)
                }
                val recipeGuis = recipeMenus.map {
                    it.getGui()
                }
                fillRecipes(recipeGuis)

                // 设置输出容器的物品
                refreshOutput()
            }

            // Case 3: 玩家将物品从输入槽位取出
            event.isRemove -> {
                // 玩家将物品从*输入容器*取出, 意味着定制过程被中途终止了.
                // 我们需要把玩家放入定制台的所有物品*原封不动*的归还给玩家.
                event.isCancelled = true

                val session = getSession() ?: run {
                    logger.error("Modding session (viewer: ${viewer.name}) is null, but input item is being removed. This is a bug!")
                    return
                }

                // 清空输入容器
                clearInputSlot()
                // 清空输出容器
                clearOutputSlot()
                // 清空所有子菜单
                clearRecipes()

                // 归还玩家放入定制台的主要物品 (被定制的物品)
                viewer.inventory.addItem(session.input.unsafe.handle)
                // 归还玩家放入定制台的其他物品 (定制所需的耗材), 这里我们直接把物品添加到玩家的背包里
                session.recipeSessions.getInputItems().forEach { itemStack ->
                    viewer.inventory.addItem(itemStack)
                }

                // 冻结 session
                session.freeze()
                // 最后把 session 置为 null (让其被 GC)
                dropSession()
            }
        }
    }

    /**
     * 当输入容器中的物品发生*变化后*调用.
     */
    protected open fun onInputInventoryPostUpdate(event: ItemPostUpdateEvent) {
        val newItem = event.newItem
        val prevItem = event.previousItem
        logger.info("Input item updated: ${prevItem?.type} -> ${newItem?.type}")
    }

    /**
     * 当输出容器中的物品发生*变化前*调用.
     */
    protected open fun onOutputInventoryPreUpdate(event: ItemPreUpdateEvent) {
        logger.info("Output item updating: ${event.previousItem?.type} -> ${event.newItem?.type}")
        when {
            // Case 1: 玩家向输出容器中添加物品
            event.isSwap || event.isAdd -> {
                event.isCancelled = true
                viewer.sendMessage("猫咪不可以!")
            }

            // Case 2: 玩家从输出容器中取出物品
            event.isRemove -> {
                // 首先取消事件, 禁止玩家直接从容器中取出物品.
                // 原因是我们可能对容器内的物品进行了修改.
                // 我们将“手动”替换玩家指针上的物品.
                event.isCancelled = true

                val session = getSession()
                if (session == null) {
                    logger.error("Modding session (viewer: ${viewer.name}) is null, but output item is being removed. This is a bug!")
                    return
                }

                // 玩家必须点两次才能取出定制后的物品
                if (!session.confirmed) {
                    editOutputSlot {
                        val ns = it.toNekoStack
                        ns.bypassPacket(true) // 让发包系统忽略该物品
                        ns.unsafe.handle.editMeta { meta ->
                            meta.displayName(
                                text("请再次点击以取出")
                                    .color(NamedTextColor.AQUA)
                                    .decoration(TextDecoration.BOLD, true)
                                    .decoration(TextDecoration.ITALIC, false)
                            )
                        }
                    }
                    session.confirmed = true
                    return
                }

                val output = session.output ?: run {
                    logger.error("Output item is null while player is trying to take it. This is a bug!")
                    return
                }

                // 如果玩家取出定制后的物品, 意味着定制过程即将完成.
                // 我们需要消耗掉所有的材料, 然后替换玩家指针上的物品.

                // TODO 计算价格, 然后扣钱

                // 将定制后的物品添加到玩家背包
                viewer.inventory.addItem(output.unsafe.handle)
                // 清空输入容器 (相当于消耗掉原始物品)
                clearInputSlot()
                // 清空输出容器 (因为玩家已经拿到手了)
                clearOutputSlot()
                // 清空所有子菜单 (相当于消耗掉所需材料)
                clearRecipes()
                // 冻结会话
                session.freeze()
                // 丢弃会话
                dropSession()
            }
        }
    }

    /**
     * 当输出容器中的物品发生*变化后*调用.
     */
    protected open fun onOutputInventoryPostUpdate(event: ItemPostUpdateEvent) {
        val prevItem = event.previousItem
        val newItem = event.newItem
        logger.info("Output item updated: ${prevItem?.type} -> ${newItem?.type}")
    }

    /**
     * 当窗口关闭时调用.
     */
    protected open fun onWindowClose() {
        // 如果当前没有会话, 则直接返回
        val session = getSession() ?: run {
            logger.info("The window of modding menu is closed while session being null.")
            return
        }

        // 有会话, 则将定制过程中玩家输入的所有物品归还给玩家
        viewer.inventory.addItem(session.input.unsafe.handle)
        viewer.inventory.addItem(*session.recipeSessions.getInputItems().toTypedArray())
    }

    /**
     * 当窗口打开时调用.
     */
    protected open fun onWindowOpen() {
        viewer.playSound(Sound.sound().type(org.bukkit.Sound.BLOCK_ANVIL_PLACE).volume(1f).pitch(0f).build())
    }

    private fun fillRecipes(guis: List<Gui>) {
        primaryGui.setContent(guis)
    }

    private fun clearRecipes() {
        primaryGui.setContent(null)
    }

    private fun fillInputSlot(stack: ItemStack) {
        inputInventory.setItemSilently(0, stack)
    }

    private fun clearInputSlot() {
        inputInventory.setItemSilently(0, null)
    }

    private fun fillOutputSlot(stack: ItemStack) {
        outputInventory.setItemSilently(0, stack)
    }

    private fun getOutputSlot(): ItemStack? {
        return outputInventory.getItem(0)
    }

    private fun editOutputSlot(block: (ItemStack) -> Unit) {
        val stack = getOutputSlot() ?: return
        block(stack)
        fillOutputSlot(stack)
    }

    private fun clearOutputSlot() {
        outputInventory.setItemSilently(0, null)
    }

    /**
     * 向前翻页的 [Item].
     */
    private class PrevItem : ScrollItem(-1) {
        override fun getItemProvider(gui: ScrollGui<*>): ItemProvider {
            val stack = ItemStack(Material.PAPER)
            stack.editMeta { it.itemName(text("上一页")) }
            return ItemWrapper(stack)
        }
    }

    /**
     * 向后翻页的 [Item].
     */
    private class NextItem : ScrollItem(1) {
        override fun getItemProvider(gui: ScrollGui<*>): ItemProvider {
            val stack = ItemStack(Material.PAPER)
            stack.editMeta { it.itemName(text("下一页")) }
            return ItemWrapper(stack)
        }
    }
}