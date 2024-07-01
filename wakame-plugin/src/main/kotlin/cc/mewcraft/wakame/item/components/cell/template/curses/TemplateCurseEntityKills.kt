package cc.mewcraft.wakame.item.components.cell.template.curses

import cc.mewcraft.wakame.entity.EntityTypeHolder
import cc.mewcraft.wakame.item.CurseConstants
import cc.mewcraft.wakame.item.components.cell.Curse
import cc.mewcraft.wakame.item.components.cell.curses.CurseEntityKills
import cc.mewcraft.wakame.item.components.cell.template.TemplateCurse
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
 * 代表一个实体击杀的蓝图诅咒。
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
        val level = context.levelOrThrow
        val count = this.count.calculate(level).toStableInt()
        return CurseEntityKills(index, count)
    }
}