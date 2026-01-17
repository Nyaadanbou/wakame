package cc.mewcraft.wakame.entity.player

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.api.event.player.PlayerResourceLoadEvent
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.registerEvents
import cc.mewcraft.wakame.util.runTask
import cc.mewcraft.wakame.util.runTaskLater
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

// 不知道给这个文件取什么名字...
// 总之这个文件里的代码用来让玩家的各种数据能够在进入服务器时正确加载
//
// Why? 届时再补充, 先开服吧
//
// 涉及到三个系统:
// 1) 跨服同步
// 2) 玩家等级
// 3) Koish

@Init(InitStage.POST_WORLD)
object PlayerDataLoadingCoordinator : Listener {

    private const val LOG_PREFIX = "[PlayerDataLoadingCoordinator]"

    init {
        registerEvents()
    }

    /**
     * 记录是否有自定义的 Stage 1 逻辑.
     */
    var hasExternalStage1Handler: Boolean = false
        private set

    /**
     * 记录是否有自定义的 Stage 2 逻辑.
     */
    var hasExternalStage2Handler: Boolean = false
        private set

    /**
     * 注册一个外部的 Stage 1 处理器.
     */
    fun registerExternalStage1Handler(name: String) {
        hasExternalStage1Handler = true
        LOGGER.info(Component.text("$LOG_PREFIX Registered external stage 1 handler: $name").color(NamedTextColor.AQUA))
    }

    /**
     * 注册一个外部的 Stage 2 处理器.
     */
    fun registerExternalStage2Handler(name: String) {
        hasExternalStage2Handler = true
        LOGGER.info(Component.text("$LOG_PREFIX Registered external stage 2 handler: $name").color(NamedTextColor.AQUA))
    }

    private val sessions: ConcurrentHashMap<Player, Session> = ConcurrentHashMap()

    /**
     * 监听玩家加入事件:
     *
     * 如果没有外部 stage1/2 的逻辑, 则在玩家加入服务器后直接调用 [Session.completeStage1] / [Session.completeStage2].
     * 如果有的话, 则由外部代码来调用 [Session.completeStage1] / [Session.completeStage2] (这一步务必准确无误的完成).
     */
    @EventHandler(priority = EventPriority.LOWEST)
    private fun on(event: PlayerJoinEvent) {
        val player = event.player

        if (!hasExternalStage1Handler) {
            getOrCreateSession(player).completeStage1()
        }

        if (!hasExternalStage2Handler) {
            getOrCreateSession(player).completeStage2()
        }
    }

    /**
     * 监听玩家退出事件:
     *
     * 在玩家退出时销毁对应的 Session.
     */
    @EventHandler
    private fun on(event: PlayerQuitEvent) {
        invalidateSession(event.player)
    }

    fun invalidateSession(player: Player) {
        sessions.remove(player)
    }

    fun getOrCreateSession(player: Player): Session {
        return sessions.computeIfAbsent(player, ::Session)
    }

    class Session(
        private val player: Player, // Caution: 注意内存泄漏
    ) {

        /**
         * Stage 1: Minecraft 原本的数据已完成加载.
         *
         * 在实现上, 一般分为两种情况:
         * 1) 不涉及到跨服同步 -> 完全加入游戏世界后, 算完成
         * 2) 涉及到跨服同步 -> 完成跨服数据同步后, 算完成
         *
         * 因此实现上应该根据这两种情况, 在合适的时候由合适的代码完成该信号.
         */
        private val stage1Signal: CompletableFuture<Void> = CompletableFuture<Void>()

        /**
         * Stage 2: 玩家冒险等级已完成加载.
         *
         * 在实现上, 取决于玩家冒险等级由哪个系统提供的:
         * 1) 由第三方插件提供 -> 由该插件在玩家等级加载完毕后完成该信号
         * 2) 由游戏原版提供 -> 在玩家完全加入游戏世界后完成该信号
         */
        private val stage2Signal: CompletableFuture<Void> = CompletableFuture<Void>()

        /**
         * Stage 3: Koish 内部数据已完成加载.
         */
        // 当:
        // 1) MC原版数据 (stage 1), 和
        // 2) 玩家冒险等级 (stage 2)
        // 都加载完毕后, 再执行 Koish 的逻辑
        private val stage3Signal: CompletableFuture<Void> = CompletableFuture.allOf(stage1Signal, stage2Signal).thenRun {
            // 当 stage 1 和 stage 2 都完成后运行 Koish 的逻辑
            runTask { ->
                if (player.isConnected) {
                    player.isInventoryListenable = true
                }

                runTaskLater(1) { -> // 疑问: 除了延迟 1t 外还有更好维护的解决方式吗?
                    if (player.isConnected) {
                        // 加载
                        ResourceSynchronizer.load(player)
                        // 触发 PlayerResourceLoadEvent
                        PlayerResourceLoadEvent(player).callEvent()
                    }
                }
            }

            // Session 的任务已经完成, 将其销毁
            invalidateSession(player)
        }

        fun completeStage1() {
            stage1Signal.complete(null)
        }

        fun completeStage2() {
            stage2Signal.complete(null)
        }
    }
}
