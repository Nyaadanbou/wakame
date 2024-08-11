package cc.mewcraft.wakame.gui.merge

import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.tryNekoStack
import cc.mewcraft.wakame.reforge.merge.MergingSession
import cc.mewcraft.wakame.reforge.merge.MergingTable
import cc.mewcraft.wakame.util.hideTooltip
import net.kyori.adventure.text.Component.text
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.inventory.VirtualInventory
import xyz.xenondevs.invui.inventory.event.ItemPreUpdateEvent
import xyz.xenondevs.invui.item.ItemWrapper
import xyz.xenondevs.invui.item.impl.SimpleItem
import xyz.xenondevs.invui.window.Window
import xyz.xenondevs.invui.window.type.context.setTitle

internal class MergingMenu(
    val table: MergingTable,
    val viewer: Player,
) : KoinComponent {

    /**
     * 给玩家展示合并台.
     */
    fun open() {
        primaryWindow.open()
    }

    /**
     * 根据当前 [mergingSession] 的状态, 刷新输出容器.
     */
    fun refreshOutput() {
        val result = mergingSession.merge()
        if (result.successful) {
            val item = result.item
            ResultRenderer(result).render(item)
            setOutputSlot(item.unsafe.handle)
        } else {
            setOutputSlot(null)
        }
    }

    /**
     * 本次合并的会话.
     *
     * ## 开发日记 2024/8/9
     * 不像 mod 和 reroll 的会话, merge 的会话是在打开菜单的时候创建.
     * 并且直到菜单关闭之前, 会话永远是这一个对象, 不会中途替换成其他的.
     * 而菜单这边的逻辑, 需要根据几个虚拟容器的变化, 来改变会话中的状态.
     */
    private val mergingSession: MergingSession = MergingSessionFactory.create(
        this, null, null
    )

    private val logger: Logger by inject()

    private val inputSlot1: VirtualInventory = VirtualInventory(intArrayOf(1))
    private val inputSlot2: VirtualInventory = VirtualInventory(intArrayOf(1))
    private val outputSlot: VirtualInventory = VirtualInventory(intArrayOf(1))

    private val primaryGui: Gui = Gui.normal { builder ->
        builder.setStructure(
            ". . . . . . . . .",
            ". . . . . . . . .",
            ". a . b . . . c .",
            ". . . . . . . . .",
            ". . . . . . . . .",
        )
        builder.addIngredient('.', SimpleItem(ItemStack(Material.BLACK_STAINED_GLASS_PANE).hideTooltip(true)))
        builder.addIngredient('a', inputSlot1)
        builder.addIngredient('b', inputSlot2)
        builder.addIngredient('c', outputSlot, ItemWrapper(ItemStack(Material.BARRIER).hideTooltip(true)))
    }

    private val primaryWindow: Window = Window.single { builder ->
        builder.setGui(primaryGui)
        builder.setTitle(text("合并台"))
        builder.addOpenHandler(::onWindowOpen)
        builder.addCloseHandler(::onWindowClose)
    }

    init {
        inputSlot1.setPreUpdateHandler(::onInputSlot1PreUpdate)
        inputSlot2.setPreUpdateHandler(::onInputSlot2PreUpdate)
        outputSlot.setPreUpdateHandler(::onOutputSlotPreUpdate)
    }

    //<editor-fold desc="inventory listeners">
    private fun onInputSlotPreUpdate(
        e: ItemPreUpdateEvent,
        setMenuInputSlot: (ItemStack?) -> Unit,
        setBackInputItem: (NekoStack?) -> Unit,
        retBackInputItem: (Player) -> Unit,
    ) {
        val oldItem = e.previousItem
        val newItem = e.newItem
        logger.info("MergingMenu input slot pre-update: $oldItem -> $newItem")

        when {
            e.isSwap -> {
                viewer.sendPlainMessage("猫咪不可以!")
                e.isCancelled = true
            }

            e.isAdd -> {
                val added = newItem?.tryNekoStack ?: run {
                    viewer.sendPlainMessage("请放入一个萌芽物品!")
                    e.isCancelled = true; return
                }

                setBackInputItem(added)
                refreshOutput()
            }

            e.isRemove -> {
                e.isCancelled = true
                setMenuInputSlot(null)
                setOutputSlot(null)

                retBackInputItem(viewer)
                setBackInputItem(null)
                refreshOutput()
            }
        }
    }

    private fun onInputSlot1PreUpdate(e: ItemPreUpdateEvent) = onInputSlotPreUpdate(
        e,
        { setInputSlot1(it) },
        { mergingSession.inputItemX = it },
        { mergingSession.returnInputItemX(it) }
    )

    private fun onInputSlot2PreUpdate(e: ItemPreUpdateEvent) = onInputSlotPreUpdate(
        e,
        { setInputSlot2(it) },
        { mergingSession.inputItemY = it },
        { mergingSession.returnInputItemY(it) }
    )

    private fun onOutputSlotPreUpdate(e: ItemPreUpdateEvent) {
        val oldItem = e.previousItem
        val newItem = e.newItem
        logger.info("MergingMenu output slot pre-update: $oldItem -> $newItem")

        when {
            e.isSwap || e.isAdd -> {
                viewer.sendPlainMessage("猫咪不可以!")
                e.isCancelled = true
            }

            e.isRemove -> {
                e.isCancelled = true

                val result = mergingSession.result
                if (result.successful) {
                    // 把合并后的物品递给玩家
                    val handle = result.item.unsafe.handle
                    viewer.inventory.addItem(handle)

                    // 清空菜单中的物品
                    setInputSlot1(null)
                    setInputSlot2(null)
                    setOutputSlot(null)
                } else {
                    viewer.sendPlainMessage("合并失败!")
                }
            }
        }
    }
    //</editor-fold>

    private fun onWindowClose() {
        logger.info("MergingMenu closed for ${viewer.name}")

        setInputSlot1(null)
        setInputSlot2(null)
        setOutputSlot(null)

        mergingSession.returnInputItemX(viewer)
        mergingSession.returnInputItemY(viewer)
        mergingSession.frozen = true
    }

    private fun onWindowOpen() {
        logger.info("MergingMenu opened for ${viewer.name}")
    }

    private fun getInputSlot1(): ItemStack? {
        return inputSlot1.getItem(0)
    }

    private fun setInputSlot1(item: ItemStack?) {
        inputSlot1.setItemSilently(0, item)
    }

    private fun getInputSlot2(): ItemStack? {
        return inputSlot2.getItem(0)
    }

    private fun setInputSlot2(item: ItemStack?) {
        inputSlot2.setItemSilently(0, item)
    }

    private fun getOutputSlot(): ItemStack? {
        return outputSlot.getItem(0)
    }

    private fun setOutputSlot(item: ItemStack?) {
        outputSlot.setItemSilently(0, item)
    }
}