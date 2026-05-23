# Plan: 魔咒图鉴 (Enchantment Catalog) 实现

基于现有物品图鉴 (`catalog/item` + `gui/catalog/item`) 的前后端分离范式，为魔咒图鉴创建数据层 (`catalog/enchantment`) 和展示层 (`gui/catalog/enchantment`)。入口菜单提供 3 个模式按钮，各模式均以 `PagedGui<Item>` 分页列表展示魔咒。每个魔咒在列表中是一个 Item（附魔书），lore 里显示名字、最大等级、支持物品、主要物品、权重、效果描述。

## 核心设计：`CatalogEnchantmentEntry`

数据层的核心是 `CatalogEnchantmentEntry` 类——它封装了一个魔咒在图鉴中所需的**全部展示数据**。展示层只需从该类获取已处理好的数据，无需自行做数据转换或查询多个数据源。

```kotlin
class CatalogEnchantmentEntry(
    /** 原始 Bukkit Enchantment 引用. */
    val enchantment: Enchantment,
    /** 效果描述 (从配置文件读取, 可为空). */
    val description: Component?,
) {
    /** 魔咒的显示名称. */
    val displayName: Component get() = enchantment.displayName()

    /** 最大附魔等级. */
    val maxLevel: Int get() = enchantment.maxLevel

    /** 支持的物品 Tag (Enchantment.getSupportedItems). */
    val supportedItems: Tag<ItemType> get() = enchantment.supportedItems

    /** 主要的物品 Tag (Enchantment.getPrimaryItems), 可能为空. */
    val primaryItems: Tag<ItemType>? get() = enchantment.primaryItems

    /** 权重 (影响附魔台随机出现概率). */
    val weight: Int get() = enchantment.weight

    /** 魔咒的 NamespacedKey. */
    val key: Key get() = enchantment.key()

    /** 该魔咒是否可以附在指定物品上. */
    fun canEnchant(itemStack: ItemStack): Boolean = enchantment.canEnchantItem(itemStack)
}
```

**设计原则**：展示层拿到 `CatalogEnchantmentEntry` 后，直接读取属性即可构建 GUI Item，不需要知道 description 从哪个配置文件来、也不需要自己调 Bukkit API。

## 文件清单

### 数据层 `catalog/enchantment/`

|文件|职责|
|---|---|
|`CatalogEnchantmentEntry.kt`|图鉴中一个魔咒的数据封装类，提供展示所需的全部属性和方法|
|`CatalogEnchantmentMenuSettings.kt`|`@Init(POST_WORLD)` object，从 `configs/catalog/enchantment/layout/` 加载各菜单的 `BasicMenuSettings`|
|`CatalogEnchantmentTagIcons.kt`|从 `configs/catalog/enchantment/tag_icons.yml` 加载 `tag value → KoishKey(图标)` 映射，为模式 c 的每个 `enchantable/*` Tag 提供展示图标|
|`CatalogEnchantmentInitializer.kt`|`@Init(POST_WORLD, runAfter=[...])` object，加载 descriptions 配置，构建全部 `CatalogEnchantmentEntry`，提供查询方法供展示层调用|

### 展示层 `gui/catalog/enchantment/`

|文件|职责|
|---|---|
|`CatalogEnchantmentMenu.kt`|接口，声明 `open()` / `close()`|
|`CatalogEnchantmentMenuStacks.kt`|菜单栈管理（参照 `CatalogItemMenuStacks`）|
|`CatalogEnchantmentMainMenu.kt`|入口菜单：3 个按钮（全部 / 按物品 / 按类型）|
|`CatalogEnchantmentAllMenu.kt`|模式 a：分页展示全部魔咒|
|`CatalogEnchantmentByItemMenu.kt`|模式 b：`VirtualInventory`(1格) 输入物品 → 展示该物品支持的魔咒|
|`CatalogEnchantmentByTypeMenu.kt`|模式 c 入口：分页展示动态发现的 `enchantable/*` Tag 列表，点击进入子菜单|
|`CatalogEnchantmentByTypeResultMenu.kt`|模式 c 结果：展示某个 Tag 下所有支持的魔咒|
|`EnchantmentDisplayItem.kt`|通用工具：从 `CatalogEnchantmentEntry` 构建 GUI Item（附魔书 + lore）|

### 配置文件 `configs/catalog/enchantment/`

|文件|内容|
|---|---|
|`layout/main.yml`|入口菜单 `BasicMenuSettings`（3 个按钮 + 背景）|
|`layout/all.yml`|模式 a 菜单 `BasicMenuSettings`（分页列表 + 翻页 + 返回）|
|`layout/by_item.yml`|模式 b 菜单 `BasicMenuSettings`（输入槽 + 分页列表 + 翻页 + 返回）|
|`layout/by_type.yml`|模式 c 入口菜单 `BasicMenuSettings`（分页列表 + 翻页 + 返回）|
|`layout/by_type_result.yml`|模式 c 结果菜单 `BasicMenuSettings`（分页列表 + 翻页 + 返回）|
|`descriptions.yml`|`enchantment NamespacedKey → Component (MiniMessage 格式)` 映射|
|`tag_icons.yml`|`enchantable tag value (如 sword, mining) → KoishKey (萌芽物品 ID)` 映射|

## Steps

### Step 1: 数据层 — `CatalogEnchantmentEntry`

新建 `catalog/enchantment/CatalogEnchantmentEntry.kt`。

```kotlin
/**
 * 魔咒图鉴中一个魔咒的数据封装.
 *
 * 展示层通过该类获取魔咒在图鉴中所需的全部展示数据,
 * 无需自行调用 Bukkit API 或查询配置文件.
 */
class CatalogEnchantmentEntry(
    /** 原始 Bukkit Enchantment 引用. */
    val enchantment: Enchantment,
    /** 效果描述 (从配置文件读取, 可为空). */
    val description: Component?,
) {
    /** 魔咒的显示名称. */
    val displayName: Component get() = enchantment.displayName()

    /** 最大附魔等级. */
    val maxLevel: Int get() = enchantment.maxLevel

    /** 支持的物品 Tag. */
    val supportedItems: Tag<ItemType> get() = enchantment.supportedItems

    /** 主要的物品 Tag, 可能为空. */
    val primaryItems: Tag<ItemType>? get() = enchantment.primaryItems

    /** 权重. */
    val weight: Int get() = enchantment.weight

    /** 魔咒的 NamespacedKey. */
    val key: Key get() = enchantment.key()

    /** 该魔咒是否可以附在指定物品上. */
    fun canEnchant(itemStack: ItemStack): Boolean = enchantment.canEnchantItem(itemStack)
}
```

### Step 2: 数据层 — `CatalogEnchantmentMenuSettings`

新建 `catalog/enchantment/CatalogEnchantmentMenuSettings.kt`，参照 [`CatalogItemMenuSettings`](wakame-plugin/src/main/kotlin/cc/mewcraft/wakame/catalog/item/CatalogItemMenuSettings.kt)。

- `@Init(InitStage.POST_WORLD)` object
- 持有 `HashMap<String, BasicMenuSettings>`
- `@InitFun` 中扫描 `KoishDataPaths.CONFIGS.resolve("catalog/enchantment/layout/")` 下所有 `*.yml`，以文件名(不含后缀)为 key 加载
- 提供 `getMenuSettings(configKey: String): BasicMenuSettings`

### Step 3: 数据层 — `CatalogEnchantmentTagIcons`

新建 `catalog/enchantment/CatalogEnchantmentTagIcons.kt`。

- `@Init(InitStage.POST_WORLD)` object
- 持有 `HashMap<String, KoishKey>`（key 为 tag value 去掉 `enchantable/` 前缀后的部分，如 `sword`；value 为萌芽物品 ID）
- `@InitFun` 加载 `configs/catalog/enchantment/tag_icons.yml`，YAML 格式：
  ```yaml
  sword: "internal/menu/catalog/enchantment/icon_sword"
  mining: "internal/menu/catalog/enchantment/icon_mining"
  armor: "internal/menu/catalog/enchantment/icon_armor"
  ```
- 提供 `fun getIcon(tagValue: String): KoishKey?`
- 提供 `fun getIconOrDefault(tagValue: String): SlotDisplay`（无配置时使用默认图标）

### Step 4: 数据层 — `CatalogEnchantmentInitializer`

改造现有的空 [`CatalogEnchantmentInitializer`](wakame-plugin/src/main/kotlin/cc/mewcraft/wakame/catalog/enchantment/CatalogEnchantmentInitializer.kt)。

- `@Init(InitStage.POST_WORLD, runAfter = [CatalogEnchantmentMenuSettings::class, CatalogEnchantmentTagIcons::class])`
- `@InitFun` 中：
  1. 加载 `configs/catalog/enchantment/descriptions.yml`，得到 `Map<String, Component>`（enchantment key → description）
  2. 遍历 `RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT)` 中所有 `Enchantment`，为每个创建 `CatalogEnchantmentEntry(enchantment, descriptions[key])`
  3. 缓存到内部 `List<CatalogEnchantmentEntry>` 和 `Map<Key, CatalogEnchantmentEntry>`
  4. 扫描 `RegistryAccess.registryAccess().getRegistry(RegistryKey.ITEM).tags`，发现所有 `enchantable/*` tags，缓存为 `List<EnchantableTagEntry>`
- 提供以下方法供展示层调用：
  - `fun allEntries(): List<CatalogEnchantmentEntry>` — 全部魔咒条目
  - `fun enchantableItemTags(): List<EnchantableTagEntry>` — 全部 `enchantable/*` 物品标签
  - `fun entriesForTag(tagEntry: EnchantableTagEntry): List<CatalogEnchantmentEntry>` — 筛选 `supportedItems` 与该 tag 有交集的魔咒条目
  - `fun entriesForItem(itemStack: ItemStack): List<CatalogEnchantmentEntry>` — 筛选 `canEnchant(itemStack)` 为 true 的魔咒条目

`EnchantableTagEntry` 为简单 data class：
```kotlin
data class EnchantableTagEntry(
    val tagValue: String,  // 如 "sword", "mining" (去掉 enchantable/ 前缀)
    val fullTagKey: TagKey<ItemType>, // 原始 TagKey，用于查询
)
```

> descriptions 的加载逻辑内聚在 `CatalogEnchantmentInitializer` 中（私有方法），不再单独拆成一个 `@Init` object。descriptions.yml 是 `CatalogEnchantmentEntry` 构建过程的一部分，由 Initializer 在构建 Entry 时注入。

### Step 5: 展示层 — 接口 & 菜单栈

1. 完善 [`CatalogEnchantmentMenu`](wakame-plugin/src/main/kotlin/cc/mewcraft/wakame/gui/catalog/enchantment/CatalogEnchantmentMenu.kt)：
   ```kotlin
   interface CatalogEnchantmentMenu {
       fun open()
       fun close()
   }
   ```

2. 新建 `CatalogEnchantmentMenuStacks.kt`，完全参照 [`CatalogItemMenuStacks`](wakame-plugin/src/main/kotlin/cc/mewcraft/wakame/gui/catalog/item/CatalogItemMenuStacks.kt) 的实现，将类型参数从 `CatalogItemMenu` 替换为 `CatalogEnchantmentMenu`。

### Step 6: 展示层 — 魔咒 Item 构建工具

新建 `gui/catalog/enchantment/EnchantmentDisplayItem.kt`，提供：

```kotlin
internal fun buildEnchantmentItem(entry: CatalogEnchantmentEntry): Item
```

- 使用附魔书 (`Material.ENCHANTED_BOOK`) 作为展示物品
- display name 使用 `entry.displayName`
- lore 依次展示：
  - 最大等级：`entry.maxLevel`
  - 支持的物品：`entry.supportedItems`
  - 主要的物品：`entry.primaryItems`（为空则不展示）
  - 权重：`entry.weight`
  - 效果描述：`entry.description`（为空则不展示）

**注意**：展示层只从 `CatalogEnchantmentEntry` 读取数据，不直接调用 Bukkit `Enchantment` API，也不直接访问任何数据层配置 object。

### Step 7: 展示层 — `CatalogEnchantmentMainMenu`

新建 `gui/catalog/enchantment/CatalogEnchantmentMainMenu.kt`。

- `internal class CatalogEnchantmentMainMenu(val viewer: Player) : CatalogEnchantmentMenu`
- 读取 `CatalogEnchantmentMenuSettings.getMenuSettings("main")`
- 使用 `Gui.builder()`（普通 Gui，非 Paged），Structure 定义 3 个按钮位 + 背景
- 按钮 `a`（查看所有）→ `CatalogEnchantmentMenuStacks.push(viewer, CatalogEnchantmentAllMenu(viewer))`
- 按钮 `b`（按物品查询）→ `CatalogEnchantmentMenuStacks.push(viewer, CatalogEnchantmentByItemMenu(viewer))`
- 按钮 `c`（按类型查询）→ `CatalogEnchantmentMenuStacks.push(viewer, CatalogEnchantmentByTypeMenu(viewer))`

### Step 8: 展示层 — `CatalogEnchantmentAllMenu` (模式 a)

新建 `gui/catalog/enchantment/CatalogEnchantmentAllMenu.kt`。

- `internal class CatalogEnchantmentAllMenu(val viewer: Player) : CatalogEnchantmentMenu`
- 读取 `CatalogEnchantmentMenuSettings.getMenuSettings("all")`
- 使用 `PagedGui.itemsBuilder()`
- Structure: 分页列表 `x` + 背景 `.` + 翻页 `<` `>` + 返回 `b`
- 内容: `CatalogEnchantmentInitializer.allEntries().map { buildEnchantmentItem(it) }`
- 返回按钮: `CatalogEnchantmentMenuStacks.pop(viewer)`

### Step 9: 展示层 — `CatalogEnchantmentByItemMenu` (模式 b)

新建 `gui/catalog/enchantment/CatalogEnchantmentByItemMenu.kt`。

- `internal class CatalogEnchantmentByItemMenu(val viewer: Player) : CatalogEnchantmentMenu`
- 读取 `CatalogEnchantmentMenuSettings.getMenuSettings("by_item")`
- 创建 `VirtualInventory(1)` 作为输入槽 `i`
- 创建 `PagedGui.itemsBuilder()` 作为结果展示
- Structure: 输入槽 `i` + 分页列表 `x` + 背景 `.` + 翻页 `<` `>` + 返回 `b`
- `VirtualInventory` 的 `addPreUpdateHandler`：
  - `isSwap` → 取消
  - `isAdd` → 允许放入
  - `isRemove` → 取消事件，归还玩家原始物品
- `addPostUpdateHandler`：物品放入/移除后，调用 `CatalogEnchantmentInitializer.entriesForItem(itemStack)` 获取条目列表，使用 `primaryGui.setContent(entries.map { buildEnchantmentItem(it) })` 刷新展示
- Window closeHandler 中归还输入槽物品

### Step 10: 展示层 — `CatalogEnchantmentByTypeMenu` (模式 c 入口)

新建 `gui/catalog/enchantment/CatalogEnchantmentByTypeMenu.kt`。

- `internal class CatalogEnchantmentByTypeMenu(val viewer: Player) : CatalogEnchantmentMenu`
- 读取 `CatalogEnchantmentMenuSettings.getMenuSettings("by_type")`
- 使用 `PagedGui.itemsBuilder()`
- 内容: `CatalogEnchantmentInitializer.enchantableItemTags().map { entry -> Item 按钮 }`
  - 每个 Item 按钮使用 `CatalogEnchantmentTagIcons.getIconOrDefault(entry.tagValue)` 的图标
  - 点击后 `CatalogEnchantmentMenuStacks.push(viewer, CatalogEnchantmentByTypeResultMenu(viewer, entry))`
- Structure: 分页列表 `x` + 背景 `.` + 翻页 `<` `>` + 返回 `b`

### Step 11: 展示层 — `CatalogEnchantmentByTypeResultMenu` (模式 c 结果)

新建 `gui/catalog/enchantment/CatalogEnchantmentByTypeResultMenu.kt`。

- `internal class CatalogEnchantmentByTypeResultMenu(val viewer: Player, val tagEntry: EnchantableTagEntry) : CatalogEnchantmentMenu`
- 读取 `CatalogEnchantmentMenuSettings.getMenuSettings("by_type_result")`
- 使用 `PagedGui.itemsBuilder()`
- 内容: `CatalogEnchantmentInitializer.entriesForTag(tagEntry).map { buildEnchantmentItem(it) }`
- Structure: 分页列表 `x` + 背景 `.` + 翻页 `<` `>` + 返回 `b`

### Step 12: 布局 YAML 配置文件

在 `src/main/resources/configs/catalog/enchantment/` 下创建：

**`layout/main.yml`** — 入口菜单
```yaml
title: "魔咒图鉴"
structure:
  - ". . . . . . . . ."
  - ". . . . . . . . ."
  - ". . a . b . c . ."
  - ". . . . . . . . ."
icons:
  background: "internal/menu/common/default/background"
  view_all: "internal/menu/catalog/enchantment/view_all"
  by_item: "internal/menu/catalog/enchantment/by_item"
  by_type: "internal/menu/catalog/enchantment/by_type"
```

**`layout/all.yml`** — 模式 a
```yaml
title: "魔咒图鉴 - 所有魔咒"
structure:
  - ". . . . . . . . ."
  - ". x x x x x x x ."
  - ". x x x x x x x ."
  - ". x x x x x x x ."
  - ". x x x x x x x ."
  - "b . . < . > . . ."
icons:
  background: "internal/menu/common/default/background"
  prev_page: "internal/menu/catalog/default/layout/base/prev_page"
  next_page: "internal/menu/catalog/default/layout/base/next_page"
  back: "internal/menu/catalog/default/layout/base/back"
```

**`layout/by_item.yml`** — 模式 b
```yaml
title: "魔咒图鉴 - 按物品查询"
structure:
  - ". . . . i . . . ."
  - ". x x x x x x x ."
  - ". x x x x x x x ."
  - ". x x x x x x x ."
  - ". x x x x x x x ."
  - "b . . < . > . . ."
icons:
  background: "internal/menu/common/default/background"
  prev_page: "internal/menu/catalog/default/layout/base/prev_page"
  next_page: "internal/menu/catalog/default/layout/base/next_page"
  back: "internal/menu/catalog/default/layout/base/back"
```

**`layout/by_type.yml`** — 模式 c 入口（同 all.yml 布局）

**`layout/by_type_result.yml`** — 模式 c 结果（同 all.yml 布局）

**`descriptions.yml`** — 魔咒效果描述
```yaml
"koish:veinminer": "<gray>连锁采掘同种矿物方块"
"koish:smelter": "<gray>挖掘方块后自动熔炼掉落物"
```

**`tag_icons.yml`** — Tag 图标
```yaml
sword: "internal/menu/catalog/enchantment/icon_sword"
mining: "internal/menu/catalog/enchantment/icon_mining"
armor: "internal/menu/catalog/enchantment/icon_armor"
```

### Step 13: 入口注册

在 [`OpenCatalogImpl`](wakame-plugin/src/main/kotlin/cc/mewcraft/wakame/catalog/OpenCatalogImpl.kt) 的 `handleSimpleUse` 中添加 `"enchantment"` 分支：

```kotlin
"enchantment" -> return handleOpenEnchantmentCatalog(player)
```

新增 `handleOpenEnchantmentCatalog` 方法：
- 尝试 `CatalogEnchantmentMenuStacks.peek(player)` 恢复上次菜单
- 否则创建 `CatalogEnchantmentMainMenu(player)` 并 `rewrite`
