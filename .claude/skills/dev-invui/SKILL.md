---
name: dev-invui
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

## 核心概念

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

## 项目 GUI 架构（菜单范式）

菜单的基本结构: `class` (接收 `viewer: Player`) → 读取 `BasicMenuSettings` → 构建 `Gui`（用 Structure 字符串定义布局）→ 构建 `Window` → `open()`。

Structure 字符串使用字符矩阵定义 GUI 布局，每个字符代表一个槽位的角色（空格分隔），常用字符: `.` 背景, `x` 内容槽, `<` `>` 翻页, `b` 返回, `s` 切换, `i`/`o` 输入/输出。

**详细范式（翻页按钮、滚动按钮、Tab 切换、VirtualInventory、生命周期、菜单栈等）见 `references/menu-patterns.md`。**

---

## InvUI 初始化

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

## 注意事项

1. **InvUI 不是线程安全的**: 只能在主线程使用。
2. **Window 只能有一个 viewer**: 不要在多个玩家之间共享 Window 实例。
3. **BoundItem 不能复用**: 每个 BoundItem 实例只能绑定到第一个 Gui。当注册为全局 ingredient 或 preset 时，传入 `BoundItem.Builder` 而非 `build()` 后的实例。
4. **UpdateReason.SUPPRESSED**: 程序内部更新 VirtualInventory 时使用此 reason，避免触发事件处理。
5. **本项目的 ItemProvider**: 优先使用 `settings.getIcon("id").resolveToItemWrapper()` 而非直接创建 `ItemBuilder`，以保持图标可配置化。
6. **v2 中 newItem 不可变**: 在 `ItemPreUpdateEvent` 中不能修改 `newItem`。如需渲染放入的物品，应在 `addPostUpdateHandler` 中使用 `setItem(UpdateReason.SUPPRESSED, slot, rendered)`。
7. **菜单类使用 `internal` 可见性**: GUI 菜单类应标记为 `internal class`。
8. **注释语言**: 所有代码注释使用中文。

---

## 子文件索引

- `references/menu-patterns.md` — 菜单类基本结构、翻页/滚动/Tab 按钮、VirtualInventory、Window 生命周期、PlayerInventorySuppressor、菜单栈
- `references/api-reference.md` — InvUI2 API 快速参考 (Item/Gui/Window/VirtualInventory/Markers)
- `assets/paged-browser-menu.kt` — 完整示例: 分页物品浏览菜单
- `assets/workbench-menu.kt` — 完整示例: 带输入/输出的工作台菜单
