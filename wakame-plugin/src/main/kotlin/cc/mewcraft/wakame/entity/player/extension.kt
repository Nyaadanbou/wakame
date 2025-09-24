package cc.mewcraft.wakame.entity.player

import cc.mewcraft.wakame.ability.combo.PlayerCombo
import cc.mewcraft.wakame.ecs.bridge.koishify
import org.bukkit.entity.Player

/**
 * 玩家的连招状态.
 */
val Player.combo: PlayerCombo
    get() = koishify()[PlayerCombo]
