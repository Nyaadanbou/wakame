package cc.mewcraft.wakame.monetization

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.adventure.translator.TranslatableMessages
import cc.mewcraft.wakame.config.PermanentStorage
import cc.mewcraft.wakame.monetization.QRCodeMapDisplay.sharedMapView
import cc.mewcraft.wakame.util.coroutine.minecraft
import cc.mewcraft.wakame.util.registerEvents
import cc.mewcraft.wakame.util.runTaskLater
import cc.mewcraft.wakame.util.unregisterEvents
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.MapId
import kotlinx.coroutines.*
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.player.*
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.map.MapCanvas
import org.bukkit.map.MapRenderer
import org.bukkit.map.MapView
import java.awt.Color
import java.awt.image.BufferedImage
import java.net.URI
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.imageio.ImageIO
import kotlin.time.Duration.Companion.seconds

/**
 * 在游戏内通过主手地图发包向玩家展示二维码图片.
 *
 * 实现原理: 利用 [Player.sendEquipmentChange] 发送虚假主手物品 (地图),
 * 再通过 [Player.sendMap] 推送像素数据, 等待玩家按下潜行取消或超时后还原真实主手.
 * 全程不修改玩家真实背包.
 *
 * 展示期间, 玩家的所有操作 (指令、物品栏切换、丢弃物品、交互等) 均被禁止,
 * 玩家可通过按下潜行来取消展示 (进而取消订单).
 * 超时、断开连接、或支付完成也会结束展示.
 */
object QRCodeMapDisplay {

    /**
     * 二维码展示结果.
     */
    enum class DisplayResult {
        /** 展示超时 (玩家未在规定时间内扫码或取消). */
        TIMEOUT,

        /** 玩家按下潜行主动取消. */
        CANCELLED_BY_SNEAK,

        /** 玩家断开连接. */
        DISCONNECTED,

        /** 支付已完成 (收到回调通知). */
        PAID,
    }

    /** 当前正在展示二维码的玩家 → 取消信号. */
    private val activeDisplays = ConcurrentHashMap<UUID, CompletableDeferred<DisplayResult>>()

    /**
     * Per-Player 上下文渲染器.
     *
     * `contextual = true` 使每个玩家拥有独立的 [MapCanvas],
     * 从而允许同一个 [MapView] 同时向不同玩家展示不同的二维码图像.
     */
    private val playerRenderer = PerPlayerImageRenderer()

    /** 共享的 [MapView], 延迟初始化 (首次展示时在主线程创建). */
    private var sharedMapView: MapView? = null

    /** 共享的假地图物品, 与 [sharedMapView] 一同初始化. */
    private var sharedFakeMap: ItemStack? = null

    /** [PermanentStorage] 中保存 map ID 的 key. */
    private const val MAP_ID_STORAGE_KEY = "qrcode_map_id"

    /**
     * 获取或创建共享的 [MapView] 和假地图物品.
     *
     * 首次调用时:
     * 1. 尝试从 [PermanentStorage] 读取上次保存的 map ID, 通过 [Bukkit.getMap] 复用已有 MapView
     * 2. 若不存在或 MapView 已失效, 则调用一次 [Bukkit.createMap] 并将 ID 写入 [PermanentStorage]
     *
     * 后续调用直接返回缓存的实例. 整个服务器生命周期 (含多次重启) 只占用一个 map_X.dat.
     *
     * 必须在主线程调用.
     */
    private fun ensureSharedMap(): Pair<MapView, ItemStack> {
        sharedMapView?.let { return it to sharedFakeMap!! }

        // 尝试复用上次保存的 MapView
        val mapView = PermanentStorage.retrieveOrNull<Int>(MAP_ID_STORAGE_KEY)?.let { savedId ->
            Bukkit.getMap(savedId)?.also { mv ->
                mv.renderers.toList().forEach(mv::removeRenderer)
                mv.addRenderer(playerRenderer)
                LOGGER.info("[Monetization] Reusing existing MapView (id=$savedId) for QR code display.")
            }
        } ?: run {
            // 无法复用, 创建新的 MapView 并持久化 ID
            val newMap = Bukkit.createMap(Bukkit.getWorlds().first())
            newMap.renderers.toList().forEach(newMap::removeRenderer)
            newMap.addRenderer(playerRenderer)
            PermanentStorage.store(MAP_ID_STORAGE_KEY, newMap.id)
            LOGGER.info("[Monetization] Created new MapView (id=${newMap.id}) for QR code display.")
            newMap
        }

        val fakeMap = ItemStack.of(Material.FILLED_MAP).apply {
            setData(DataComponentTypes.MAP_ID, MapId.mapId(mapView.id))
        }

        sharedMapView = mapView
        sharedFakeMap = fakeMap
        return mapView to fakeMap
    }

    /**
     * 检查指定玩家是否正在查看二维码.
     */
    fun displaying(playerId: UUID): Boolean {
        return activeDisplays.containsKey(playerId)
    }

    /**
     * 通知指定玩家的二维码展示: 支付已完成.
     *
     * 由支付回调处理器调用, 使展示提前结束并返回 [DisplayResult.PAID].
     */
    fun notifyPaid(playerId: UUID) {
        activeDisplays[playerId]?.complete(DisplayResult.PAID)
    }

    /**
     * 下载二维码图片并向玩家展示.
     *
     * 展示期间:
     * - 禁止玩家一切操作 (指令、切换物品栏、丢弃物品、交互、移动等)
     * - 通过聊天框周期性提示玩家 (请扫码支付 / 按下潜行取消)
     * - 按下潜行、超时、断开连接、支付完成均会结束展示
     *
     * 内部自动切换线程: IO 下载图片 → 主线程发包展示 → 挂起等待 → 主线程还原.
     * 调用方只需处于协程环境即可, 無需關心線程調度.
     *
     * @param player 目标玩家
     * @param imageUrl 图片直链 (例如 Z-PAY 返回的 `img` 字段)
     * @param paymentType 支付方式 (用于在聊天提示中告知玩家使用支付宝还是微信扫码)
     * @return 展示结果; 若无法开始展示 (例如图片下载失败或玩家正在展示中) 则返回 `null`
     */
    suspend fun show(player: Player, imageUrl: String, paymentType: PaymentType): DisplayResult? {
        // 防止重复展示
        if (activeDisplays.containsKey(player.uniqueId)) {
            LOGGER.warn("[Monetization] Player ${player.name} is already viewing a QR code, skipping.")
            return null
        }

        // 1. IO 线程下载图片
        val image = withContext(Dispatchers.IO) {
            try {
                ImageIO.read(URI(imageUrl).toURL())
            } catch (e: Exception) {
                LOGGER.error("[Monetization] Failed to download QR code image from: $imageUrl", e)
                null
            }
        } ?: return null

        val mapImage = prepareMapImage(image)

        // 2. 准备取消信号
        val deferred = CompletableDeferred<DisplayResult>()
        activeDisplays[player.uniqueId] = deferred

        // 用于保存并还原玩家的原始视角
        var originalYaw = 0f
        var originalPitch = 0f

        // listener 需要引用 fakeMap, mapView 和 originalSlot, 在主线程中初始化后设置
        var listener: QRCodeFreezeListener? = null

        try {
            // 3. 主线程: 获取共享 MapView, 设置 per-player 图像, 发包展示, 冻结玩家
            withContext(Dispatchers.minecraft) {
                val (mapView, fakeMap) = ensureSharedMap()
                playerRenderer.setImage(player.uniqueId, mapImage)

                // 记录发起订单时的物品栏位置, 后续所有发包都固定更新此位置
                val originalSlot = player.inventory.heldItemSlot

                // 保存原始视角, 强制向下看 (pitch=90) 使地图平整无畸变
                originalYaw = player.location.yaw
                originalPitch = player.location.pitch
                player.setRotation(originalYaw, 90f)

                player.sendEquipmentChange(player, EquipmentSlot.HAND, fakeMap)
                player.sendMap(mapView)

                // 注册监听器, 冻结玩家操作; 传入 originalSlot 以确保重发时固定更新原始位置
                val freezeListener = QRCodeFreezeListener(player.uniqueId, deferred, fakeMap, mapView, originalSlot)
                    .also { listener = it }
                freezeListener.registerEvents()
            }

            // 4. 挂起等待 + 周期性聊天提醒
            val displayDuration = MonetizationConfig.qrcodeDisplay.displayDuration.seconds
            val reminderInterval = MonetizationConfig.qrcodeDisplay.reminderInterval.seconds
            val result = coroutineScope {
                // 启动周期性提醒协程 (立即发送第一条, 然后按配置间隔重复)
                val reminderJob = launch {
                    while (isActive) {
                        withContext(Dispatchers.minecraft) {
                            if (player.isOnline) {
                                remind(player, paymentType)
                            }
                        }
                        delay(reminderInterval)
                    }
                }

                val displayResult = withTimeoutOrNull(displayDuration) {
                    deferred.await()
                } ?: DisplayResult.TIMEOUT

                reminderJob.cancelAndJoin()
                displayResult
            }

            return result
        } finally {
            // 5. 清理: 无论正常返回、异常还是协程取消, 都保证资源释放
            activeDisplays.remove(player.uniqueId)
            playerRenderer.removeImage(player.uniqueId)

            // listener 可能尚未初始化 (如果 withContext(minecraft) 在赋值前就抛异常)
            val freezeListener = listener
            if (freezeListener != null) {
                withContext(Dispatchers.minecraft + NonCancellable) {
                    freezeListener.unregisterEvents()
                    if (player.isOnline) {
                        player.updateInventory()
                        player.setRotation(originalYaw, originalPitch)
                    }
                }
            }
        }
    }

    /**
     * 向玩家发送带空行强调的聊天提醒.
     *
     * 必须在主线程调用.
     */
    private fun remind(player: Player, paymentType: PaymentType) {
        val hint = when (paymentType) {
            PaymentType.ALIPAY -> TranslatableMessages.MSG_MONETIZATION_QR_HINT_ALIPAY
            PaymentType.WXPAY -> TranslatableMessages.MSG_MONETIZATION_QR_HINT_WXPAY
        }
        player.sendMessage(hint.build())
    }

    /**
     * 将二维码图片缩放到配置的尺寸, 并居中放置在 128x128 的白底画布上.
     *
     * 地图固定为 128x128 像素, 若配置的 [MonetizationConfig.qrcodeDisplay.mapSize] 小于 128,
     * 则二维码居中显示, 周围留白, 避免被其他 UI 遮挡.
     */
    private fun prepareMapImage(image: BufferedImage): BufferedImage {
        val targetSize = MonetizationConfig.qrcodeDisplay.mapSize.coerceIn(1, 128)

        // 创建 128x128 白底画布
        val canvas = BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB)
        val g = canvas.createGraphics()
        g.color = Color.WHITE
        g.fillRect(0, 0, 128, 128)

        // 将二维码缩放到目标尺寸, 居中绘制
        val offset = (128 - targetSize) / 2
        g.drawImage(image, offset, offset, targetSize, targetSize, null)
        g.dispose()

        return canvas
    }


}

/**
 * Per-player 上下文地图渲染器.
 *
 * 使用 `contextual = true` 使每个玩家拥有独立的 [MapCanvas],
 * 从而允许同一个 [MapView] 同时向不同玩家展示不同的二维码图像.
 * 整个插件生命周期只需一个实例, 配合共享 [MapView] 使用.
 */
private class PerPlayerImageRenderer : MapRenderer(true) {
    private val images = ConcurrentHashMap<UUID, BufferedImage>()

    fun setImage(playerId: UUID, image: BufferedImage) {
        images[playerId] = image
    }

    fun removeImage(playerId: UUID) {
        images.remove(playerId)
    }

    override fun render(map: MapView, canvas: MapCanvas, player: Player) {
        val image = images[player.uniqueId] ?: return
        canvas.drawImage(0, 0, image)
    }
}

/**
 * 在二维码展示期间冻结玩家的所有操作.
 *
 * - 监听 [PlayerToggleSneakEvent] 来触发取消
 * - 监听 [PlayerQuitEvent] 来处理断线
 * - 拦截一切可能影响游戏状态的玩家事件
 * - 当玩家交互导致假物品被服务器纠正时, 延迟 1 tick 重新发送假物品
 *
 * @param originalSlot 发起展示时玩家的物品栏位置 (0-8),
 *   所有重发操作都固定更新此位置, 避免因切换物品栏导致假物品发到错误位置.
 */
private class QRCodeFreezeListener(
    private val playerId: UUID,
    private val deferred: CompletableDeferred<QRCodeMapDisplay.DisplayResult>,
    private val fakeMap: ItemStack,
    private val mapView: MapView,
    private val originalSlot: Int,
) : Listener {

    /**
     * 延迟 1 tick 重新发送假地图物品和地图数据.
     *
     * 当玩家进行交互 (丢弃、切换物品栏等) 时, 即使事件被取消,
     * 服务器仍可能发送库存纠正包覆盖我们的假物品.
     * 延迟 1 tick 可确保在服务器纠正之后再次发送假物品.
     *
     * 发送前先强制将客户端的选中物品栏位设回 [originalSlot],
     * 再通过 [EquipmentSlot.HAND] 更新该位置的显示,
     * 保证假物品始终出现在发起订单时的那个物品栏位上.
     */
    private fun resendFakeMap(delay: Long = 1L) {
        runTaskLater(delay) {
            val player = Bukkit.getPlayer(playerId) ?: return@runTaskLater
            if (QRCodeMapDisplay.displaying(playerId)) {
                // 强制选中原始物品栏位, 确保 HAND 指向正确位置
                player.inventory.heldItemSlot = originalSlot
                player.sendEquipmentChange(player, EquipmentSlot.HAND, fakeMap)
                player.sendMap(mapView)
            }
        }
    }

    // ---- 取消信号检测 ----

    @EventHandler(priority = EventPriority.LOWEST)
    fun onSneak(event: PlayerToggleSneakEvent) {
        if (event.player.uniqueId != playerId) return
        if (event.isSneaking) {
            deferred.complete(QRCodeMapDisplay.DisplayResult.CANCELLED_BY_SNEAK)
        }
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        if (event.player.uniqueId != playerId) return
        deferred.complete(QRCodeMapDisplay.DisplayResult.DISCONNECTED)
    }

    // ---- 禁止一切操作 (交互后重发假物品) ----

    @EventHandler(priority = EventPriority.LOWEST)
    fun onCommand(event: PlayerCommandPreprocessEvent) {
        if (event.player.uniqueId != playerId) return
        event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onHeldChange(event: PlayerItemHeldEvent) {
        if (event.player.uniqueId != playerId) return
        event.isCancelled = true
        // 立即将客户端选中栏位强制回原位, 防止短暂闪烁
        event.player.inventory.heldItemSlot = originalSlot
        resendFakeMap()
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onDrop(event: PlayerDropItemEvent) {
        if (event.player.uniqueId != playerId) return
        event.isCancelled = true
        resendFakeMap(20)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onInventoryClick(event: InventoryClickEvent) {
        if (event.whoClicked.uniqueId != playerId) return
        event.isCancelled = true
        resendFakeMap()
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onInventoryOpen(event: InventoryOpenEvent) {
        if (event.player.uniqueId != playerId) return
        event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onSwapHand(event: PlayerSwapHandItemsEvent) {
        if (event.player.uniqueId != playerId) return
        event.isCancelled = true
        resendFakeMap()
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onInteract(event: PlayerInteractEvent) {
        if (event.player.uniqueId != playerId) return
        event.isCancelled = true
        resendFakeMap()
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onInteractEntity(event: PlayerInteractEntityEvent) {
        if (event.player.uniqueId != playerId) return
        event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onPickup(event: EntityPickupItemEvent) {
        if (event.entity.uniqueId != playerId) return
        event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onDamage(event: EntityDamageEvent) {
        if (event.entity.uniqueId != playerId) return
        event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onMove(event: PlayerMoveEvent) {
        if (event.player.uniqueId != playerId) return
        // 禁止移动位置, 允许水平转头, 但强制向下看 (pitch=90)
        val to = event.from.clone()
        to.yaw = event.to.yaw
        to.pitch = 90f
        event.to = to
    }
}
