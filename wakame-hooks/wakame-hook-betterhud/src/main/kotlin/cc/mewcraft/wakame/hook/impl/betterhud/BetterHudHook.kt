package cc.mewcraft.wakame.hook.impl.betterhud

import cc.mewcraft.wakame.entity.resource.ResourceTypeRegistry
import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.user.PlayerAdapters
import kr.toxicity.hud.api.BetterHud
import kr.toxicity.hud.api.BetterHudAPI
import kr.toxicity.hud.api.manager.PlaceholderManager
import kr.toxicity.hud.api.placeholder.HudPlaceholder
import org.bukkit.entity.Player

@Hook(plugins = ["BetterHud"])
object BetterHudHook {
    val betterHud: BetterHud = BetterHudAPI.inst()
    val placeholderManager: PlaceholderManager = betterHud.placeholderManager

    init {
        placeholderManager.numberContainer.addPlaceholder(
            "koish_mana", HudPlaceholder.of(
                HudPlaceholder.PlaceholderFunction.of { player ->
                    val user = PlayerAdapters.get<Player>().adapt(player.uuid())
                    user.resourceMap.current(ResourceTypeRegistry.MANA)
                }
            )
        )
        placeholderManager.numberContainer.addPlaceholder(
            "koish_max_mana", HudPlaceholder.of(
                HudPlaceholder.PlaceholderFunction.of { player ->
                    val user = PlayerAdapters.get<Player>().adapt(player.uuid())
                    user.resourceMap.maximum(ResourceTypeRegistry.MANA)
                }
            )
        )
        placeholderManager.numberContainer.addPlaceholder(
            "koish_mana_percentage_complement", HudPlaceholder.of(
                HudPlaceholder.PlaceholderFunction.of { player ->
                    val user = PlayerAdapters.get<Player>().adapt(player.uuid())
                    val current = user.resourceMap.current(ResourceTypeRegistry.MANA)
                    val maximum = user.resourceMap.maximum(ResourceTypeRegistry.MANA)
                    101 - if (maximum > 0) { // 计算百分比的 Complement, 这里 +1 是因为 BetterHud无法显示 0
                        (current.toDouble() / maximum * 100)
                    } else {
                        .0
                    }
                }
            )
        )
    }
}