# InvUI2 API 快速参考

本文档列出 InvUI2 常用 API 的简明清单。菜单构建范式见 `references/menu-patterns.md`。

---

## Item 创建

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

## Gui 创建

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

## Window 创建与打开

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

## VirtualInventory 事件

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

## Markers

```kotlin
Markers.CONTENT_LIST_SLOT_HORIZONTAL // 水平填充内容 (从左到右, 逐行)
Markers.CONTENT_LIST_SLOT_VERTICAL   // 垂直填充内容 (从上到下, 逐列)
```
