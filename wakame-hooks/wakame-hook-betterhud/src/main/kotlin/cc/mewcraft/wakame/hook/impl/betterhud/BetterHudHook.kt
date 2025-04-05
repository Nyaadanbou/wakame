package cc.mewcraft.wakame.hook.impl.betterhud

import cc.mewcraft.wakame.SERVER
import cc.mewcraft.wakame.ecs.bridge.koishify
import cc.mewcraft.wakame.ecs.component.Mana
import cc.mewcraft.wakame.integration.Hook
import kr.toxicity.hud.api.BetterHud
import kr.toxicity.hud.api.BetterHudAPI
import kr.toxicity.hud.api.manager.PlaceholderManager
import kr.toxicity.hud.api.placeholder.HudPlaceholder

@Hook(plugins = ["BetterHud"])
object BetterHudHook {
    val betterHud: BetterHud = BetterHudAPI.inst()
    val placeholderManager: PlaceholderManager = betterHud.placeholderManager

    init {
        placeholderManager.numberContainer.addPlaceholder(
            "koish_mana", HudPlaceholder.of(
                HudPlaceholder.PlaceholderFunction.of { player ->
                    val bukkitPlayer = requireNotNull(SERVER.getPlayer(player.uuid())) { "Player ${player.uuid()} not found" }
                    bukkitPlayer.koishify()[Mana].current
                }
            )
        )
        placeholderManager.numberContainer.addPlaceholder(
            "koish_max_mana", HudPlaceholder.of(
                HudPlaceholder.PlaceholderFunction.of { player ->
                    val bukkitPlayer = requireNotNull(SERVER.getPlayer(player.uuid())) { "Player ${player.uuid()} not found" }
                    bukkitPlayer.koishify()[Mana].maximum
                }
            )
        )
        placeholderManager.numberContainer.addPlaceholder(
            "koish_mana_percentage_complement", HudPlaceholder.of(
                HudPlaceholder.PlaceholderFunction.of { player ->
                    val bukkitPlayer = requireNotNull(SERVER.getPlayer(player.uuid())) { "Player ${player.uuid()} not found" }
                    val current = bukkitPlayer.koishify()[Mana].current
                    val maximum = bukkitPlayer.koishify()[Mana].maximum
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