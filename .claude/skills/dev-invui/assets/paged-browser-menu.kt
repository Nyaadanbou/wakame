// 完整示例: 分页物品浏览菜单
// 来源: dev-invui SKILL.md §5

internal class ItemBrowserMenu(
    val viewer: Player,
) {
    private val settings: BasicMenuSettings = SomeMenuSettings.getMenuSettings("browser")

    private val primaryGui: PagedGui<Item> = PagedGui.itemsBuilder()
        .setStructure(*settings.structure)
        .addIngredient(
            '.', Item.builder()
                .setItemProvider { _ ->
                    settings.getIcon("background").resolveToItemWrapper()
                }
        )
        .addIngredient(
            '<', BoundItem.pagedBuilder()
                .setItemProvider { _, gui ->
                    if (gui.page <= 0)
                        settings.getIcon("background").resolveToItemWrapper()
                    else
                        settings.getIcon("prev_page").resolveToItemWrapper {
                            standard {
                                component("current_page", Component.text(gui.page + 1))
                                component("total_page", Component.text(gui.pageCount))
                            }
                        }
                }
                .addClickHandler { _, gui, _ ->
                    gui.page -= 1
                }
        )
        .addIngredient(
            '>', BoundItem.pagedBuilder()
                .setItemProvider { _, gui ->
                    if (gui.page >= gui.pageCount - 1)
                        settings.getIcon("background").resolveToItemWrapper()
                    else
                        settings.getIcon("next_page").resolveToItemWrapper {
                            standard {
                                component("current_page", Component.text(gui.page + 1))
                                component("total_page", Component.text(gui.pageCount))
                            }
                        }
                }
                .addClickHandler { _, gui, _ ->
                    gui.page += 1
                }
        )
        .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
        .setContent(buildContentItems())
        .build()

    private val primaryWindow: Window = Window.builder()
        .setUpperGui(primaryGui)
        .setViewer(viewer)
        .setTitle(settings.title)
        .build()

    fun open() {
        primaryWindow.open()
    }

    private fun buildContentItems(): List<Item> {
        return listOf(/* ... */)
    }
}
