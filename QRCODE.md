### 简介

如何在游戏内向玩家展示一张任意的二维码图片。

基于“发包欺骗（虚假副手物品）+ `sendMap`” 的方案，你可以直接把以下**核心原理与具体执行步骤**发给它看。它就能完全理解并写出符合 Paper 1.21.11 标准的代码。

### 核心实现原理

利用 Paper API 原生提供的**装备状态同步方法** (`sendEquipmentChange`)，向客户端发送一个“虚假的副手物品更新包”。这会让玩家屏幕上举起一张地图，但服务端的真实背包数据完全不受影响。随后利用 `sendMap` 强推像素数据，最后再发包还原玩家真实的副手物品。

### 具体实现步骤 (共 5 步)

#### 第一步：注册并绘制自定义画布 (`MapView` & `MapRenderer`)

1. 调用 `Bukkit.createMap(world)` 创建一个新的 `MapView`（系统会分配一个唯一的地图 ID）。
2. 清除该 `MapView` 默认的所有渲染器 (`getRenderers()` 然后遍历 `removeRenderer()`)。
3. 编写一个自定义的 `MapRenderer`，将准备好的二维码图片（128x128 像素的 `BufferedImage`）通过 `MapCanvas.drawImage()` 绘制上去。
4. 将自定义渲染器添加到这个 `MapView` 中。

#### 第二步：构建虚假的地图物品 (`ItemStack` & `MapMeta`)

1. 创建一个新的物品栈：`new ItemStack(Material.FILLED_MAP)`。
2. 获取它的 `ItemMeta` 并强转为 `MapMeta`。
3. 调用 `mapMeta.setMapView(之前创建的MapView)`，将地图视图与这个物品绑定。
4. 将设定好的 `MapMeta` 应用回这个 `ItemStack`。**（注意：这个物品千万不要放进玩家真实的背包里）**。

#### 第三步：发送虚假装备包 (核心障眼法)

1. 调用 Paper 的原生发包 API：`player.sendEquipmentChange(player, EquipmentSlot.OFF_HAND, 虚假地图ItemStack)`。
2. **原理**：这仅仅是告诉客户端“你现在副手拿着这张图”，此时玩家屏幕上会立刻做出举起左手看地图的动作。

#### 第四步：强制推送像素数据

1. 紧接着第三步，立即调用 `player.sendMap(之前创建的MapView)`。
2. **原理**：防止客户端因为没有缓存该地图 ID 的数据而显示空白或“探索中”的画面。这会强制把二维码的像素包发给客户端，地图瞬间显形。

#### 第五步：收回地图与状态还原 (Cleanup)

1. 这是一个临时展示，必须在适当时机（例如几秒后的延迟任务 `BukkitRunnable`，或者监听玩家移动/切换物品事件时）还原状态。
2. 获取玩家服务端真实的副手物品：`player.getInventory().getItemInOffHand()`。
3. 再次调用：`player.sendEquipmentChange(player, EquipmentSlot.OFF_HAND, 真实的副手物品)`。
4. **原理**：用真实的副手状态覆盖掉之前的虚假发包，玩家屏幕上的地图就会消失，左手恢复成原本拿着的盾牌或空手。

---

在这个流程中，为了防止内存泄漏，通常还需要考虑**废弃 `MapView` 的回收问题**（如果不回收，生成的地图 ID 会一直增加）。