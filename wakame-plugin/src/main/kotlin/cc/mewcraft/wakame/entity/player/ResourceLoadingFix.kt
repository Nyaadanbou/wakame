package cc.mewcraft.wakame.entity.player

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.integration.HooksLoader
import cc.mewcraft.wakame.integration.playerlevel.PlayerLevelManager
import cc.mewcraft.wakame.integration.playerlevel.PlayerLevelType
import cc.mewcraft.wakame.lifecycle.initializer.DisableFun
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.event
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent


/** 该接口允许外部代码实现一个 [ResourceLoadingFixBootstrap] 的具体逻辑, 以此来避免 CNF 异常. */
fun interface ResourceLoadingFixHandler {

    /** 实现该函数来执行必要的逻辑, 例如注册 Event Handler. */
    operator fun invoke()

    companion object {
        // 当前所使用的实例
        internal var CURRENT_HANDLER: ResourceLoadingFixHandler = ResourceLoadingFixHandler {
            LOGGER.error("The current PlayerResourceFixHandler is a no-op but it's being called. This is a bug!", Error())
        }
    }

}

/** 该 object 用于解决玩家资源(当前血量/魔法值)在进出服务器时无法正确加载的问题. */
@Init(
    stage = InitStage.POST_WORLD, runAfter = [
        HooksLoader::class, // 依赖于 PlayerLevelManager
    ]
)
internal object ResourceLoadingFixBootstrap {

    @InitFun
    fun init() {

        // 在玩家退出服务器时, 保存玩家的资源数据
        event<PlayerQuitEvent>(
            priority = EventPriority.LOWEST, // 尽可能早的储存 PDC, 让 HuskSync 能够同步到
        ) { event ->
            val player = event.player
            ResourceSynchronizer.save(player)
        }

        // 在玩家的冒险等级加载完毕后, 加载玩家的资源数据 (这里根据运行时的冒险等级系统加载对应的监听器)
        when (PlayerLevelManager.integration.type) {

            // 这两冒险等级系统完全依赖原版游戏自身, 没有额外的数据储存,
            // 所以可以直接在进入游戏时读取玩家的等级信息并且加载资源数据.
            PlayerLevelType.ZERO, PlayerLevelType.VANILLA -> event<PlayerJoinEvent> { event ->
                val player = event.player
                player.isInventoryListenable = true
                ResourceSynchronizer.load(player)
            }

            // 其余的情况则使用 Hook 的具体实现.
            else -> ResourceLoadingFixHandler.CURRENT_HANDLER.invoke()
        }
    }

    @DisableFun
    fun close() {
        // 关闭服务器时服务端不会触发任何事件,
        // 需要我们手动执行保存玩家资源的逻辑.
        // 如果服务器有使用 HuskSync, 我们的插件必须在 HuskSync 之前关闭,
        // 否则 PDC 无法保存到 HuskSync 的数据库, 导致玩家资源数据丢失.
        ResourceSynchronizer.saveAll()
    }

}