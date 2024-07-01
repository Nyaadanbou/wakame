package cc.mewcraft.wakame.item.components.cell.template.cores.noop

import cc.mewcraft.wakame.GenericKeys
import cc.mewcraft.wakame.item.components.cell.Core
import cc.mewcraft.wakame.item.components.cell.cores.noop.CoreNoop
import cc.mewcraft.wakame.item.components.cell.template.TemplateCore
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.examination.ExaminableProperty
import java.util.stream.Stream

/**
 * 代表一个无操作的核心模板.
 *
 * 设计上, 无操作核心不会写入到物品上.
 */
data object TemplateCoreNoop : TemplateCore {
    override val key: Key = GenericKeys.NOOP

    override fun generate(context: GenerationContext): Core {
        return CoreNoop // 无操作核心应该不需要写入上下文吧?
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("key", key)
    )

    override fun toString(): String {
        return toSimpleString()
    }
}