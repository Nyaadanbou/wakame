package cc.mewcraft.wakame.element.effect

import cc.mewcraft.wakame.ability.character.CasterAdapter
import cc.mewcraft.wakame.ability.character.TargetAdapter
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import org.bukkit.entity.LivingEntity

/**
 * 表示一个元素效果. 元素效果是一个元素对生物的异常状态.
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
interface ElementEffect {
    /**
     * 元素效果所属的元素.
     */
    val element: RegistryEntry<out Element>

    fun apply(damager: LivingEntity?, entity: LivingEntity)
}

fun ElementEffect(element: RegistryEntry<out Element>): ElementEffect {
    return ElementEffectImpl(element)
}

private data class ElementEffectImpl(
    override val element: RegistryEntry<out Element>,
) : ElementEffect {

    override fun apply(damager: LivingEntity?, entity: LivingEntity) = elementEffectWorldInteraction {
        if (entity.containsElementEffect(element)) {
            entity.addElementEffect(this@ElementEffectImpl)
            return@elementEffectWorldInteraction
        }
        val caster = damager?.let { CasterAdapter.adapt(it) }
        val target = TargetAdapter.adapt(entity)
        putIntoWorld(caster, target)
    }

    fun isSimilarTo(other: ElementEffect): Boolean {
        return other.element == element
    }
}