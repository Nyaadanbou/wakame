---
name: invui-gui
description: >
  Guide for creating GUI menus using InvUI2 in the Koish (wakame) project.
  Use this skill when asked to create, modify, or debug GUI/menu code,
  or when the task involves InvUI windows, guis, items, inventories,
  or any Minecraft inventory-based UI in the wakame-plugin module.
---

# 使用 InvUI2 创建 GUI 菜单

本技能指导你在 Koish (wakame) 项目中使用 InvUI2 库创建 GUI 菜单。
所有 GUI 代码位于 `wakame-plugin/src/main/kotlin/cc/mewcraft/wakame/gui/` 下。

---

## 1. InvUI2 核心概念

InvUI2 有四个核心概念:

| 概念 | 说明 |
|---|---|
| **Window** | 玩家看到的实际菜单 (Minecraft inventory)。每个 Window 只有一个 viewer。Window 包含上方 GUI 和下方 (玩家背包) GUI。 |
| **Gui** | 一个矩形的槽位排列，可以包含 Item、VirtualInventory、或嵌套的 Gui。 |
| **Item** | UI 元素 (按钮)，由 ItemProvider 提供视觉表示，并可注册 click handler。 |
| **ItemProvider** | 物品的视觉表示。可以使用 `ItemBuilder`、`ItemWrapper` 或本项目的 `SlotDisplay.resolveToItemWrapper()`。 |

### Gui 类型

| 类型 | 用途 | Builder |
|---|---|---|
| `Gui` | 普通静态 GUI | `Gui.builder()` |
| `PagedGui<Item>` | 分页显示 Item 列表 | `PagedGui.itemsBuilder()` |
| `PagedGui<Gui>` | 分页显示 Gui 列表 | `PagedGui.guisBuilder()` |
| `ScrollGui<Item>` | 滚动显示 Item 列表 | `ScrollGui.itemsBuilder()` |
| `ScrollGui<Gui>` | 滚动显示 Gui 列表 | `ScrollGui.guisBuilder()` |
| `TabGui` | 标签页切换 | `TabGui.builder()` |

### Window 类型

| 类型 | 用途 | Builder |
|---|---|---|
| `Window` | 普通窗口 (chest/hopper/dropper) | `Window.builder()` |
| `AnvilWindow` | 铁砧窗口 (带文本输入) | `AnvilWindow.builder()` |
| `MerchantWindow` | 商人交易窗口 | `MerchantWindow.builder()` |
| 其他 | `BrewingWindow`, `CartographyWindow`, `CrafterWindow`, `CraftingWindow`, `FurnaceWindow`, `GrindstoneWindow`, `SmithingWindow`, `StonecutterWindow` | 对应的 builder |

### BoundItem (绑定到 Gui 的 Item)

当 Item 需要感知所属 Gui 的状态 (如翻页按钮) 时，使用 BoundItem:

| Builder | 绑定 Gui 类型 |
|---|---|
| `BoundItem.pagedBuilder()` | `PagedGui` |
| `BoundItem.scrollBuilder()` | `ScrollGui` |
| `BoundItem.tabBuilder()` | `TabGui` |

---

## 2. 本项目的 GUI 架构范式

### 2.1 菜单类基本结构

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

### 2.2 Structure 字符串布局

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

### 2.3 使用 BasicMenuSettings 读取配置

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

### 2.4 翻页按钮标准写法

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

### 2.5 滚动按钮标准写法

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

### 2.6 Tab 切换标准写法

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

### 2.7 VirtualInventory (输入/输出槽位)

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

### 2.8 Window 生命周期钩子

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

### 2.9 PlayerInventorySuppressor

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

### 2.10 菜单栈 (多层级菜单导航)

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

---

## 3. InvUI 初始化

项目通过 `BasicGuiInitializer` 在 `POST_WORLD` 阶段初始化 InvUI:

```kotlin
@Init(InitStage.POST_WORLD)
internal object BasicGuiInitializer : Listener {
    @InitFun
    fun init() {
        InvUI.getInstance().setPlugin(KoishPlugin)
        WindowManager.getInstance()
        registerEvents()
    }

    @DisableFun
    fun close() {
        WindowManager.getInstance().windows.forEach(Window::close)
    }
}
```

**新菜单不需要修改此初始化代码。**

---

## 4. InvUI2 API 快速参考

### Item 创建

```kotlin
// 简单装饰物品 (不可点击)
Item.simple(ItemBuilder(Material.DIAMOND))
Item.simple(ItemWrapper(itemStack))

// 可点击物品
Item.builder()
    .setItemProvider { player -> ItemBuilder(Material.DIAMOND) }
    .addClickHandler { item, click -> /* 处理点击 */ }
    .build()

// 动态物品 (lambda 提供 ItemProvider)
Item.builder()
    .setItemProvider { player -> ItemBuilder(Material.DIAMOND).setName("Count: $count") }
    .addClickHandler { item, click ->
        count++
        item.notifyWindows() // 触发刷新
    }
    .build()
```

### Gui 创建

```kotlin
// 普通 Gui
Gui.builder()
    .setStructure("# # # # # # # # #", "# x x x x x x x #", ...)
    .addIngredient('#', Item.simple(ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)))
    .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
    .build()

// 空 Gui
Gui.empty(9, 6)

// 单物品 Gui
Gui.single(someItem)

// 嵌入 VirtualInventory
Gui.of(9, 4, virtualInventory)
```

### Window 创建与打开

```kotlin
// 使用 builder
Window.builder()
    .setUpperGui(gui)
    .setViewer(player)
    .setTitle("标题")
    .addOpenHandler { /* 打开回调 */ }
    .addCloseHandler { /* 关闭回调 */ }
    .build()
    .also { it.open() }

// 直接 open
Window.builder()
    .setUpperGui(guiBuilder) // 传入 builder 而非 build() 结果, 每次 open 都新建
    .open(player)
```

### VirtualInventory 事件

```kotlin
val inv = VirtualInventory(size)

// 物品变更前 (可取消)
inv.addPreUpdateHandler { event ->
    event.isAdd     // 玩家添加物品
    event.isRemove  // 玩家移除物品
    event.isSwap    // 玩家交换物品
    event.slot      // 变更的槽位
    event.newItem   // 变更后的物品 (不可变)
    event.previousItem // 变更前的物品
    event.isCancelled = true // 取消变更
}

// 物品变更后 (不可取消)
inv.addPostUpdateHandler { event ->
    // 用于后续处理, 如重新渲染
}

// 点击事件 (可拦截特殊点击)
inv.addClickHandler { event ->
    event.clickType
    event.slot
    event.isCancelled = true
}

// 程序更新 (不触发事件)
inv.setItem(UpdateReason.SUPPRESSED, slot, itemStack)

// 批量操作后通知刷新
inv.unsafeItems.fill(null) // 直接操作内部数组
inv.notifyWindows()        // 手动通知
```

### Markers

```kotlin
Markers.CONTENT_LIST_SLOT_HORIZONTAL // 水平填充内容 (从左到右, 逐行)
Markers.CONTENT_LIST_SLOT_VERTICAL   // 垂直填充内容 (从上到下, 逐列)
```

---

## 5. 完整示例: 分页物品浏览菜单

```kotlin
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
```

## 6. 完整示例: 带输入/输出的工作台菜单

```kotlin
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
```

---

## 7. 注意事项

1. **InvUI 不是线程安全的**: 只能在主线程使用。
2. **Window 只能有一个 viewer**: 不要在多个玩家之间共享 Window 实例。
3. **BoundItem 不能复用**: 每个 BoundItem 实例只能绑定到第一个 Gui。当注册为全局 ingredient 或 preset 时，传入 `BoundItem.Builder` 而非 `build()` 后的实例。
4. **UpdateReason.SUPPRESSED**: 程序内部更新 VirtualInventory 时使用此 reason，避免触发事件处理。
5. **本项目的 ItemProvider**: 优先使用 `settings.getIcon("id").resolveToItemWrapper()` 而非直接创建 `ItemBuilder`，以保持图标可配置化。
6. **v2 中 newItem 不可变**: 在 `ItemPreUpdateEvent` 中不能修改 `newItem`。如需渲染放入的物品，应在 `addPostUpdateHandler` 中使用 `setItem(UpdateReason.SUPPRESSED, slot, rendered)`。
7. **菜单类使用 `internal` 可见性**: GUI 菜单类应标记为 `internal class`。
8. **注释语言**: 所有代码注释使用中文。

