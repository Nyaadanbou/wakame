// 完整示例: 带输入/输出的工作台菜单
// 来源: dev-invui SKILL.md §6

internal class WorkbenchMenu(
    val viewer: Player,
) {
    private val playerInventorySuppressor = PlayerInventorySuppressor(viewer)

    private val inputSlot: VirtualInventory = VirtualInventory(1).apply {
        addPreUpdateHandler { event ->
            when {
                event.isSwap -> event.isCancelled = true
                event.isAdd -> { /* 处理放入 */ }
                event.isRemove -> {
                    event.isCancelled = true
                    // 归还物品
                }
            }
        }
    }

    private val outputSlot: VirtualInventory = VirtualInventory(1).apply {
        addPreUpdateHandler { event ->
            when {
                event.isAdd || event.isSwap -> event.isCancelled = true
                event.isRemove -> {
                    event.isCancelled = true
                    // 检查条件, 给予结果
                }
            }
        }
    }

    private val primaryGui: Gui = Gui.builder()
        .setStructure(
            ". . . . . . . . .",
            ". . . i . o . . .",
            ". . . . . . . . .",
        )
        .addIngredient('.', backgroundItem)
        .addIngredient('i', inputSlot)
        .addIngredient('o', outputSlot)
        .build()

    private val primaryWindow: Window = Window.builder()
        .setUpperGui(primaryGui)
        .setTitle(Component.text("工作台"))
        .setViewer(viewer)
        .addOpenHandler { playerInventorySuppressor.startListening() }
        .addCloseHandler {
            playerInventorySuppressor.stopListening()
            // 归还物品, 清理状态
        }
        .build()

    fun open() {
        primaryWindow.open()
    }
}
