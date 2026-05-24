# InvUI2 菜单范式

本文件详细描述项目中使用 InvUI2 的各个菜单构建范式。核心概念和注意事项见 `SKILL.md`。

---

## 菜单类基本结构

每个菜单是一个 `class`，接收 `viewer: Player` 和其他业务参数，提供 `open()` 方法:

```kotlin
internal class MyMenu(
    val viewer: Player,
) {
    // 1. 读取菜单配置 (BasicMenuSettings)
    private val settings: BasicMenuSettings = MyMenuSettings.getMenuSettings("main")

    // 2. 构建 Gui (使用 Structure 定义布局)
    private val primaryGui: PagedGui<Item> = PagedGui.itemsBuilder()
        .setStructure(*settings.structure)
        .addIngredient('.', settings.getIcon("background").resolveToItemWrapper())
        .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
        .addIngredient('<', /* 翻页按钮 */)
        .addIngredient('>', /* 翻页按钮 */)
        .setContent(/* 内容列表 */)
        .build()

    // 3. 构建 Window
    private val primaryWindow: Window = Window.builder()
        .setUpperGui(primaryGui)
        .setViewer(viewer)
        .setTitle(settings.title)
        .build()

    // 4. open() 方法
    fun open() {
        primaryWindow.open()
    }
}
```

## Structure 字符串布局

Structure 使用字符矩阵定义 GUI 布局，每个字符代表一个槽位的角色。字符之间用空格分隔。常用字符约定:

| 字符 | 含义 |
|---|---|
| `.` | 背景 (装饰性玻璃板等) |
| `x` | 内容槽位 (配合 `Markers.CONTENT_LIST_SLOT_HORIZONTAL` 或 `VERTICAL`) |
| `<` | 上一页/向上滚动 |
| `>` | 下一页/向下滚动 |
| `b` | 返回按钮 |
| `s` | 搜索按钮 / 切换按钮 |
| `i` | 输入槽位 (VirtualInventory) |
| `o` | 输出槽位 (VirtualInventory) |
| `*` | 装饰 / Tab 内容区域 |

Structure 字符串示例:
```kotlin
.setStructure(
    ". . . . . . . . .",
    ". x x x x x x x .",
    ". x x x x x x x .",
    ". x x x x x x x .",
    ". . . < . > . . .",
)
```

## 使用 BasicMenuSettings 读取配置

项目使用 `BasicMenuSettings` 从 YAML 配置文件加载菜单布局和图标:

```kotlin
@ConfigSerializable
data class BasicMenuSettings(
    val title: Component,
    val structure: Array<String>,
    val icons: HashMap<String, KoishKey>,
) {
    fun getIcon(id: String): SlotDisplay
}
```

- `title`: 菜单标题 (Component)
- `structure`: 布局字符串数组
- `icons`: 图标映射 (配置节点名 → 萌芽物品 ID)

使用方式:
```kotlin
// 用 settings.structure 设置布局
.setStructure(*settings.structure)

// 用 settings.getIcon("xxx") 获取图标
.addIngredient('.', settings.getIcon("background").resolveToItemWrapper())
```

## 翻页按钮标准写法

翻页按钮使用 `BoundItem.pagedBuilder()`，根据页码状态动态显示不同图标:

```kotlin
// 上一页按钮
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

// 下一页按钮
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
```

## 滚动按钮标准写法

```kotlin
.addIngredient(
    '<', BoundItem.scrollBuilder()
        .setItemProvider { _, _ ->
            settings.getIcon("prev_page").resolveToItemWrapper()
        }
        .addClickHandler { _, gui, _ ->
            gui.line -= 1
        }
)
.addIngredient(
    '>', BoundItem.scrollBuilder()
        .setItemProvider { _, _ ->
            settings.getIcon("next_page").resolveToItemWrapper()
        }
        .addClickHandler { _, gui, _ ->
            gui.line += 1
        }
)
```

## Tab 切换标准写法

```kotlin
private val switchItem = BoundItem.tabBuilder()
    .setItemProvider { _, gui ->
        val currentTab = TabType.entries[gui.tab]
        val itemStack = if (currentTab == TabType.TAB_A) {
            settings.getIcon("select_tab_b").resolveToItemStack()
        } else {
            settings.getIcon("select_tab_a").resolveToItemStack()
        }
        ItemWrapper(itemStack)
    }
    .addClickHandler { _, gui, _ ->
        val currentTab = TabType.entries[gui.tab]
        gui.tab = if (currentTab == TabType.TAB_A) 1 else 0
    }
    .build()

private val primaryUpperGui: TabGui = TabGui.builder()
    .setStructure(*settings.structure)
    .addIngredient('.', settings.getIcon("background").resolveToItemStack())
    .addIngredient('*', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
    .addIngredient('s', switchItem)
    .setTabs(listOf(tabAGui, tabBGui))
    .build()
```

## VirtualInventory (输入/输出槽位)

需要玩家放入物品的场景使用 `VirtualInventory`:

```kotlin
// 创建 1 格输入槽位
private val inputSlot: VirtualInventory = VirtualInventory(1).apply {
    addPreUpdateHandler(::onInputInventoryPreUpdate)
    addPostUpdateHandler { e ->
        if (e.isAdd) {
            // v2: newItem 不可变, 在 post-update handler 中重新渲染放入的物品
            val rendered = renderInputItem() ?: return@addPostUpdateHandler
            setItem(UpdateReason.SUPPRESSED, e.slot, rendered)
        }
    }
}

// 嵌入 Gui
.addIngredient('i', inputSlot)
// 带背景的嵌入
.addIngredient('o', outputSlot, settings.getIcon("output_empty").resolveToItemWrapper())

// PreUpdateEvent 处理模板
private fun onInputInventoryPreUpdate(event: ItemPreUpdateEvent) {
    when {
        event.isSwap -> {
            event.isCancelled = true
        }
        event.isAdd -> {
            // 处理玩家放入物品
        }
        event.isRemove -> {
            event.isCancelled = true
            // 处理玩家取出物品, 通常需要归还原始物品
        }
    }
}

// 程序更新槽位内容 (绕过事件)
private fun setInputSlot(stack: ItemStack?) {
    inputSlot.setItem(UpdateReason.SUPPRESSED, 0, stack)
}
```

## Window 生命周期钩子

```kotlin
private val primaryWindow: Window = Window.builder()
    .setUpperGui(primaryGui)
    .setTitle(settings.title)
    .setViewer(viewer)
    .addOpenHandler(::onWindowOpen)
    .addCloseHandler { onWindowClose() }
    .build()

private fun onWindowOpen() {
    // 注册事件监听器 (如 PlayerInventorySuppressor)
    playerInventorySuppressor.startListening()
}

private fun onWindowClose() {
    // 取消事件监听器
    playerInventorySuppressor.stopListening()
    // 归还玩家物品
    viewer.inventory.addItem(*session.getAllInputs())
    // 清理会话
    session.reset()
}
```

## PlayerInventorySuppressor

当菜单需要限制玩家操作自己的背包时 (如定制台、重造台), 使用 `PlayerInventorySuppressor`:

```kotlin
private val playerInventorySuppressor = PlayerInventorySuppressor(viewer)

// Window 打开时启用
private fun onWindowOpen() {
    playerInventorySuppressor.startListening()
}

// Window 关闭时禁用
private fun onWindowClose() {
    playerInventorySuppressor.stopListening()
}
```

## 菜单栈 (多层级菜单导航)

对于图鉴类多层级菜单, 使用 `CatalogItemMenuStacks` 模式管理菜单栈:

```kotlin
// 进入子菜单 (压栈并打开)
CatalogItemMenuStacks.push(viewer, SubMenu(viewer))

// 返回上级菜单 (弹栈并打开上一个)
CatalogItemMenuStacks.pop(viewer)

// 返回按钮的标准写法
.addIngredient(
    'b', Item.builder()
        .setItemProvider { _ ->
            settings.getIcon("back").resolveToItemWrapper()
        }
        .addClickHandler { _, click ->
            CatalogItemMenuStacks.pop(click.player)
        }
)
```
