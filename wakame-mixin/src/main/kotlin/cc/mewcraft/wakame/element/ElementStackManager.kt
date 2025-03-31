package cc.mewcraft.wakame.element

import cc.mewcraft.wakame.ability2.component.CastBy
import cc.mewcraft.wakame.ability2.component.TargetTo
import cc.mewcraft.wakame.ecs.Fleks
import cc.mewcraft.wakame.ecs.bridge.KoishEntity
import cc.mewcraft.wakame.ecs.component.TickCountComponent
import cc.mewcraft.wakame.element.component.ElementComponent
import cc.mewcraft.wakame.element.component.ElementStackComponent
import cc.mewcraft.wakame.element.component.ElementStackContainer
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap

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
     */
    fun applyElementStack(element: RegistryEntry<ElementType>, amount: Int, target: KoishEntity) {
        require(amount > 0) { "Amount must be greater than 0" }
        val stackEffect = element.value.stackEffect
        if (stackEffect == null)
            return
        if (containsElementStack(target, element)) {
            addElementStack(target, element, amount)
            return
        }

        val elementStackEntity = Fleks.INSTANCE.createEntity {
            it += CastBy(target.unwrap())
            it += TargetTo(target.unwrap())
            it += ElementComponent(element)
            it += TickCountComponent(0)
            it += ElementStackComponent(
                effects = stackEffect.stages.associate { it.amount to it.abilities }.toMap(Int2ObjectOpenHashMap()),
                maxAmount = stackEffect.maxAmount,
                disappearTime = stackEffect.disappearTime,
            )
        }
        target[ElementStackContainer][element] = elementStackEntity
    }

    fun containsElementStack(entity: KoishEntity, element: RegistryEntry<ElementType>): Boolean {
        if (!entity.contains(ElementStackContainer))
            return false
        val elementEntity = entity[ElementStackContainer][element]
        return elementEntity != null
    }

    private fun addElementStack(entity: KoishEntity, element: RegistryEntry<ElementType>, amount: Int) = with(Fleks.INSTANCE.world) {
        require(amount > 0) { "Amount must be greater than 0" }
        val stack = entity[ElementStackContainer][element] ?: return@with
        stack[ElementStackComponent].amount += amount
        stack[TickCountComponent].tick = 0
    }
}