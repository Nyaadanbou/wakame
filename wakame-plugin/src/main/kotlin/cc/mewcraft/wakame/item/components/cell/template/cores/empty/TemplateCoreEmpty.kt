package cc.mewcraft.wakame.item.components.cell.template.cores.empty

import cc.mewcraft.wakame.GenericKeys
import cc.mewcraft.wakame.item.components.cell.Core
import cc.mewcraft.wakame.item.components.cell.cores.empty.CoreEmpty
import cc.mewcraft.wakame.item.components.cell.template.TemplateCore
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.examination.ExaminableProperty
import java.util.stream.Stream

/**
 * 代表一个空的核心模板.
 *
 * “词条栏里有空核心” 在玩家看来就是 “词条栏里没有核心”.
 * 如果词条栏没有核心, 那也就意味着可以被替换成其他的.
 */
data object TemplateCoreEmpty : TemplateCore {
    override val key: Key = GenericKeys.EMPTY

    override fun generate(context: GenerationContext): Core {
        return CoreEmpty // 空词条栏应该不需要写入上下文吧?
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("key", key)
    )

    override fun toString(): String {
        return toSimpleString()
    }
}