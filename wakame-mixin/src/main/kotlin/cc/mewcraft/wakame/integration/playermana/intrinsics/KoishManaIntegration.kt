package cc.mewcraft.wakame.integration.playermana.intrinsics

import cc.mewcraft.wakame.ecs.bridge.koishify
import cc.mewcraft.wakame.ecs.component.Mana
import cc.mewcraft.wakame.integration.playermana.PlayerManaIntegration
import cc.mewcraft.wakame.integration.playermana.PlayerManaType
import org.bukkit.entity.Player

// FIXME 实际没有使用, 考虑移除此 object, 也移除整个 Koish 内置的基于 ECS 的魔法值系统
object KoishManaIntegration : PlayerManaIntegration {

    override val manaType: PlayerManaType = PlayerManaType.KOISH

    override fun getMana(player: Player): Double {
        return player.koishify().getOrNull(Mana)?.current?.toDouble() ?: .0
    }

    override fun setMana(player: Player, amount: Double) {
        player.koishify().getOrNull(Mana)?.current = amount.toInt()
    }

    override fun getMaxMana(player: Player): Double {
        return player.koishify().getOrNull(Mana)?.maximum?.toDouble() ?: .0
    }

    override fun consumeMana(player: Player, amount: Double): Boolean {
        return player.koishify().getOrNull(Mana)?.costMana(amount.toInt()) ?: false
    }
}