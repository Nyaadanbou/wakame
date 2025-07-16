package cc.mewcraft.wakame.hook.impl.betterhud

import cc.mewcraft.wakame.ecs.bridge.koishify
import cc.mewcraft.wakame.ecs.component.Mana
import cc.mewcraft.wakame.entity.attribute.Attributes
import cc.mewcraft.wakame.entity.player.attributeContainer
import cc.mewcraft.wakame.integration.Hook
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
                HudPlaceholder.PlaceholderFunction.of { playerx: HudPlayer ->
                    val player = playerx.player()
                    player.koishify()[Mana].current
                }
            )
        )
        placeholderManager.numberContainer.addPlaceholder(
            "koish_max_mana", HudPlaceholder.of(
                HudPlaceholder.PlaceholderFunction.of { playerx: HudPlayer ->
                    val player = playerx.player()
                    player.koishify()[Mana].maximum
                }
            )
        )
        placeholderManager.numberContainer.addPlaceholder(
            "koish_mana_percentage_complement", HudPlaceholder.of(
                HudPlaceholder.PlaceholderFunction.of { playerx: HudPlayer ->
                    val player = playerx.player()
                    val current = player.koishify()[Mana].current
                    val maximum = player.koishify()[Mana].maximum
                    (100 + 1) - if (maximum > 0) { // 计算百分比的 Complement, 这里 +1 是因为 BetterHud 无法显示 0
                        (current.toDouble() / maximum * 100)
                    } else {
                        .0
                    }
                }
            )
        )
        placeholderManager.numberContainer.addPlaceholder(
            "koish_attribute", HudPlaceholder.builder<Number>()
                .requiredArgsLength(1)
                .function(::resolveAttributeValue)
                .build()
        )
    }

    private fun HudPlayer.player(): Player {
        return handle() as Player
    }

    private fun resolveAttributeValue(args: List<String>, event: UpdateEvent): Function<HudPlayer, Number> = Function func@{ playerx: HudPlayer ->
        val player = playerx.player()
        val attributeMap = player.attributeContainer
        val attribute = Attributes.get(args[0]) ?: return@func .0
        attributeMap.getValue(attribute)
    }
}