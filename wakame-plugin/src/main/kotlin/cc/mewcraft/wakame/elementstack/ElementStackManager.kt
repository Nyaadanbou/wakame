package cc.mewcraft.wakame.elementstack

import cc.mewcraft.wakame.ability.component.CastBy
import cc.mewcraft.wakame.ability.component.TargetTo
import cc.mewcraft.wakame.ecs.Fleks
import cc.mewcraft.wakame.ecs.bridge.BukkitEntity
import cc.mewcraft.wakame.ecs.bridge.KoishEntity
import cc.mewcraft.wakame.ecs.bridge.koishify
import cc.mewcraft.wakame.ecs.component.TickCountComponent
import cc.mewcraft.wakame.element.ElementType
import cc.mewcraft.wakame.element.component.ElementComponent
import cc.mewcraft.wakame.elementstack.component.ElementStackComponent
import cc.mewcraft.wakame.elementstack.component.ElementStackContainer
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import org.bukkit.entity.Player

fun BukkitEntity.applyElementStack(
    element: RegistryEntry<ElementType>,
    count: Int,
    caster: BukkitEntity? = null,
) {
    val caster = if (caster is Player) {
        caster.koishify()
    } else {
        caster?.koishify()
    }
    if (this is Player) {
        ElementStackManager.applyElementStack(element, count, this.koishify(), caster)
    } else {
        ElementStackManager.applyElementStack(element, count, this.koishify(), caster)
    }
}

/**
 * 元素效果管理.
 *
 * 元素效果是一个元素对生物的异常状态.
 * 该效果会在生物上叠加, 叠加的次数会决定元素效果对于生物的影响.
 * 一个生物上可叠加多个元素效果. 但是一些元素效果无法同时存在, 当冲突时旧元素效果会被新的覆盖.
 *
 * 例如: 配置中 1 层火元素效果会着火, 2 层就会走路带火焰, 最大层数 2 层, 持续时间为 5 秒.
 *
 * 当玩家对一只生物施加了 1 层火元素状态, 那么该生物就会着火.
 * 如果在 5 秒内没有再次应用火元素效果, 那么该生物的元素效果所有火元素层数清空, 随即停止着火.
 * 但当玩家对生物施加了第 2 层火元素效果, 那么该生物就会走路带火焰. 即使在 5 秒内再次应用火元素效果, 层数也不会增加,
 * 最后所有火元素层数清空, 该生物会停止走路带火焰.
 */
object ElementStackManager {
    /**
     * 对一个目标实体应用元素层数.
     *
     * @param target 目标实体
     * @param element 元素效果的元素
     * @param amount 应用层数
     * @param caster 造成伤害的实体, 如果为空则表示无造成伤害实体.
     */
    fun applyElementStack(element: RegistryEntry<ElementType>, amount: Int, target: KoishEntity, caster: KoishEntity?) {
        require(amount > 0) { "Amount must be greater than 0" }
        if (containsElementStack(target, element)) {
            addElementStack(target, element, amount)
            return
        }

        val elementStackEntity = Fleks.createEntity {
            it += if (caster == null) {
                CastBy(target.entity)
            } else {
                CastBy(caster.entity)
            }
            it += TargetTo(target.entity)
            it += ElementComponent(element)
            it += TickCountComponent(0)
            it += ElementStackComponent(Int2ObjectOpenHashMap())
        }
        target[ElementStackContainer][element] = elementStackEntity
    }

    fun containsElementStack(entity: KoishEntity, element: RegistryEntry<ElementType>): Boolean {
        return element in entity[ElementStackContainer]
    }

    private fun addElementStack(entity: KoishEntity, element: RegistryEntry<ElementType>, amount: Int) = with(Fleks.world) {
        require(amount > 0) { "Amount must be greater than 0" }
        val stack = entity[ElementStackContainer][element] ?: return@with
        stack[ElementStackComponent].amount += amount
        stack[TickCountComponent].tick = 0
    }
}