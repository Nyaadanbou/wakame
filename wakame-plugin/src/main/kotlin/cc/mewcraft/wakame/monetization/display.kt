package cc.mewcraft.wakame.monetization

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.adventure.translator.TranslatableMessages
import cc.mewcraft.wakame.util.coroutine.minecraft
import cc.mewcraft.wakame.util.registerEvents
import cc.mewcraft.wakame.util.unregisterEvents
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.MapId
import kotlinx.coroutines.*
import net.kyori.adventure.text.Component
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

    /** 二维码在地图上显示的时长 (毫秒). */
    private const val DISPLAY_DURATION_MS = 120_000L // 120 秒

    /** 聊天提示的周期间隔 (毫秒). */
    private const val REMINDER_INTERVAL_MS = 10_000L // 10 秒

    /** 当前正在展示二维码的玩家 → 取消信号. */
    private val activeDisplays = ConcurrentHashMap<UUID, CompletableDeferred<DisplayResult>>()

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

        val listener = QRCodeFreezeListener(player.uniqueId, deferred)

        // 3. 主线程: 创建 MapView, 发包展示, 冻结玩家
        withContext(Dispatchers.minecraft) {
            val mapView = Bukkit.createMap(player.world)
            mapView.renderers.toList().forEach(mapView::removeRenderer)
            mapView.addRenderer(StaticImageRenderer(mapImage))

            val fakeMap = ItemStack.of(Material.FILLED_MAP).apply {
                setData(DataComponentTypes.MAP_ID, MapId.mapId(mapView.id))
            }

            player.sendEquipmentChange(player, EquipmentSlot.HAND, fakeMap)
            player.sendMap(mapView)

            // 注册监听器, 冻结玩家操作
            listener.registerEvents()
        }

        // 4. 挂起等待 + 周期性聊天提醒
        val result = coroutineScope {
            // 启动周期性提醒协程 (立即发送第一条, 然后每隔 REMINDER_INTERVAL_MS 重复)
            val reminderJob = launch {
                while (isActive) {
                    withContext(Dispatchers.minecraft) {
                        if (player.isOnline) {
                            remind(player, paymentType)
                        }
                    }
                    delay(REMINDER_INTERVAL_MS)
                }
            }

            val displayResult = withTimeoutOrNull(DISPLAY_DURATION_MS) {
                deferred.await()
            } ?: DisplayResult.TIMEOUT

            reminderJob.cancelAndJoin()
            displayResult
        }

        // 5. 清理: 取消注册监听器, 还原主手
        activeDisplays.remove(player.uniqueId)
        withContext(Dispatchers.minecraft) {
            listener.unregisterEvents()
            if (player.isOnline) {
                val realMainHand = player.inventory.itemInMainHand
                player.sendEquipmentChange(player, EquipmentSlot.HAND, realMainHand)
            }
        }

        return result
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
        player.sendMessage(Component.empty())
        player.sendMessage(hint.build())
        player.sendMessage(Component.empty())
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

private class StaticImageRenderer(private val image: BufferedImage) : MapRenderer() {
    private var rendered = false

    override fun render(map: MapView, canvas: MapCanvas, player: Player) {
        if (!rendered) {
            canvas.drawImage(0, 0, image)
            rendered = true
        }
    }
}

/**
 * 在二维码展示期间冻结玩家的所有操作.
 *
 * - 监听 [PlayerToggleSneakEvent] 来触发取消
 * - 监听 [PlayerQuitEvent] 来处理断线
 * - 拦截一切可能影响游戏状态的玩家事件
 */
private class QRCodeFreezeListener(
    private val playerId: UUID,
    private val deferred: CompletableDeferred<QRCodeMapDisplay.DisplayResult>,
) : Listener {

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

    // ---- 禁止一切操作 ----

    @EventHandler(priority = EventPriority.LOWEST)
    fun onCommand(event: PlayerCommandPreprocessEvent) {
        if (event.player.uniqueId != playerId) return
        event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onHeldChange(event: PlayerItemHeldEvent) {
        if (event.player.uniqueId != playerId) return
        event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onDrop(event: PlayerDropItemEvent) {
        if (event.player.uniqueId != playerId) return
        event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onInventoryClick(event: InventoryClickEvent) {
        if (event.whoClicked.uniqueId != playerId) return
        event.isCancelled = true
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
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onInteract(event: PlayerInteractEvent) {
        if (event.player.uniqueId != playerId) return
        event.isCancelled = true
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
        if (event.hasChangedPosition()) {
            // 允许转头, 但禁止移动位置
            val to = event.from.clone()
            to.yaw = event.to.yaw
            to.pitch = event.to.pitch
            event.to = to
        }
    }
}
