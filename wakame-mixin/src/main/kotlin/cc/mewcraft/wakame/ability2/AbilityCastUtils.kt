package cc.mewcraft.wakame.ability2

import cc.mewcraft.wakame.ability2.meta.AbilityMeta
import cc.mewcraft.wakame.ecs.bridge.KoishEntity
import cc.mewcraft.wakame.ecs.bridge.koishify
import cc.mewcraft.wakame.item2.config.property.impl.AbilityOnItem
import cc.mewcraft.wakame.item2.config.property.impl.ItemSlot
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

/**
 * 释放技能的辅助方法.
 */
object AbilityCastUtils {

    /**
     * 立马释放技能. 技能的初始状态为 [StatePhase.CAST_POINT].
     *
     * 例如, 立刻发射一个火球, 而不是先使技能进入 [StatePhase.IDLE] 状态再等待玩家输入才进行发射.
     */
    fun castPoint(
        ability: AbilityMeta,
        caster: Entity,
        target: Entity,
    ) {
        val caster = getKoishfiedEntity(caster)
        val target = getKoishfiedEntity(target)
        AbilityEcsBridge.createEcsEntities(ability, caster, target, StatePhase.CAST_POINT)
    }

    /**
     * 为玩家"激活"技能. 技能的初始状态为 [StatePhase.IDLE].
     *
     * 例如, 玩家手持传送法杖 (进入 [StatePhase.IDLE] 状态), 然后点击右键, 技能就会进入 [StatePhase.CAST_POINT] 状态.
     */
    fun idle(
        ability: AbilityOnItem,
        caster: Player,
        target: Entity,
        slot: ItemSlot,
    ) {
        val target = getKoishfiedEntity(target)
        AbilityEcsBridge.createEcsEntities(ability, caster.koishify(), target, StatePhase.IDLE, slot)
    }

    private fun getKoishfiedEntity(entity: Entity): KoishEntity {
        return if (entity is Player) {
            entity.koishify()
        } else {
            entity.koishify()
        }
    }

}