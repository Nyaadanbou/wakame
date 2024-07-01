package cc.mewcraft.wakame.item.components.cell.template.curses

import cc.mewcraft.wakame.GenericKeys
import cc.mewcraft.wakame.item.components.cell.Curse
import cc.mewcraft.wakame.item.components.cell.curses.CurseEmpty
import cc.mewcraft.wakame.item.components.cell.template.TemplateCurse
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.examination.ExaminableProperty
import java.util.stream.Stream

/**
 * 代表一个空[诅咒][Curse]的模板.
 */
data object TemplateCurseEmpty : TemplateCurse {
    override val key: Key = GenericKeys.EMPTY

    override fun generate(context: GenerationContext): Curse {
        return CurseEmpty // 空的诅咒应该不需要写入上下文吧?
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("key", key)
    )

    override fun toString(): String {
        return toSimpleString()
    }
}