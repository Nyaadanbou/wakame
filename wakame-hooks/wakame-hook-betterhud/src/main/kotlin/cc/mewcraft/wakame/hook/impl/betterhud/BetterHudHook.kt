package cc.mewcraft.wakame.hook.impl.betterhud

import cc.mewcraft.wakame.entity.attribute.Attributes
import cc.mewcraft.wakame.entity.player.attributeContainer
import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.integration.playerlevel.PlayerLevelManager
import cc.mewcraft.wakame.integration.playermana.PlayerManaIntegration
import kr.toxicity.hud.api.BetterHud
import kr.toxicity.hud.api.BetterHudAPI
import kr.toxicity.hud.api.placeholder.HudPlaceholder
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.update.UpdateEvent
import org.bukkit.entity.Player
import java.util.function.Function

@Hook(plugins = ["BetterHud"])
object BetterHudHook {
    val betterHud: BetterHud = BetterHudAPI.inst()

    init {
        registerPlaceholders()
    }

    private fun registerPlaceholders() {
        val placeholderManager = betterHud.placeholderManager
        placeholderManager.numberContainer.addPlaceholder(
            "koish_mana", HudPlaceholder.of(
                HudPlaceholder.PlaceholderFunction.of { playerx ->
                    val player = playerx.toBukkitPlayer()
                    PlayerManaIntegration.getMana(player)
                }
            )
        )
        placeholderManager.numberContainer.addPlaceholder(
            "koish_max_mana", HudPlaceholder.of(
                HudPlaceholder.PlaceholderFunction.of { playerx ->
                    val player = playerx.toBukkitPlayer()
                    PlayerManaIntegration.getMaxMana(player)
                }
            )
        )
        placeholderManager.numberContainer.addPlaceholder(
            "koish_mana_percentage_complement", HudPlaceholder.of(
                HudPlaceholder.PlaceholderFunction.of { playerx ->
                    val player = playerx.toBukkitPlayer()
                    val current = PlayerManaIntegration.getMana(player)
                    val maximum = PlayerManaIntegration.getMaxMana(player)
                    (100 + 1) - if (maximum > 0) { // 计算百分比的 Complement, 这里 +1 是因为 BetterHud 无法显示 0
                        (current / maximum * 100)
                    } else {
                        .0
                    }
                }
            )
        )
        placeholderManager.numberContainer.addPlaceholder(
            "koish_player_level", HudPlaceholder.of(
                HudPlaceholder.PlaceholderFunction.of { playerx ->
                    val player = playerx.toBukkitPlayer()
                    PlayerLevelManager.getOrDefault(player.uniqueId, 0)
                }
            )
        )
        placeholderManager.numberContainer.addPlaceholder(
            "koish_attribute", HudPlaceholder.builder<Number>()
                .requiredArgsLength(1)
                .function { args: List<String>, _: UpdateEvent ->
                    Function func@{ playerx ->
                        val player = playerx.toBukkitPlayer()
                        val attributeMap = player.attributeContainer
                        val attribute = Attributes.get(args[0]) ?: return@func .0
                        attributeMap.getValue(attribute)
                    }
                }
                .build()
        )
    }
}

private fun HudPlayer.toBukkitPlayer(): Player {
    return handle() as Player
}