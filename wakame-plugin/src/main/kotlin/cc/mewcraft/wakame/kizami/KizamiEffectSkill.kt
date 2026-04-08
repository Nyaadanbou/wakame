package cc.mewcraft.wakame.kizami

import cc.mewcraft.wakame.integration.skill.SkillWrapper
import org.bukkit.entity.Player
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting

@ConfigSerializable
data class KizamiEffectSkill(
    @Setting("on_apply")
    val skillOnApply: SkillWrapper,
    @Setting("on_remove")
    val skillOnRemove: SkillWrapper,
) : KizamiEffect {

    override fun apply(player: Player) {
        skillOnApply.cast(player)
    }

    override fun remove(player: Player) {
        skillOnRemove.cast(player)
    }
}