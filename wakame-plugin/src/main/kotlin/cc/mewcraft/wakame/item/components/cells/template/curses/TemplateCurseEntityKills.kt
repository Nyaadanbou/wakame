package cc.mewcraft.wakame.item.components.cells.template.curses

import cc.mewcraft.wakame.entity.EntityTypeHolder
import cc.mewcraft.wakame.item.CurseConstants
import cc.mewcraft.wakame.item.components.cells.Curse
import cc.mewcraft.wakame.item.components.cells.curses.CurseEntityKills
import cc.mewcraft.wakame.item.components.cells.template.TemplateCurse
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.util.RandomizedValue
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.toStableInt
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode

/**
 * 构建一个 [TemplateCurseEntityKills].
 */
fun TemplateCurseEntityKills(node: ConfigurationNode): TemplateCurseEntityKills {
    val count = node.node("count").krequire<RandomizedValue>()
    val index = node.node("index").krequire<EntityTypeHolder>()
    return TemplateCurseEntityKills(index, count)
}

/**
 * `实体击杀数` 的诅咒模板.
 *
 * @property count 击杀数量
 * @property index 实体种类
 */
data class TemplateCurseEntityKills(
    private val index: EntityTypeHolder,
    private val count: RandomizedValue,
) : TemplateCurse {
    override val key: Key = CurseConstants.createKey { ENTITY_KILLS }
    override fun generate(context: GenerationContext): Curse {
        val level = context.level ?: 0
        val count = this.count.calculate(level).toStableInt()
        return CurseEntityKills(index, count)
    }
}