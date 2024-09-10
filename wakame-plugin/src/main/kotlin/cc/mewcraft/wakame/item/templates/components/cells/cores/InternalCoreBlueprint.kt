package cc.mewcraft.wakame.item.templates.components.cells.cores

import cc.mewcraft.wakame.GenericKeys
import cc.mewcraft.wakame.item.components.cells.Core
import cc.mewcraft.wakame.item.components.cells.CoreFactory
import cc.mewcraft.wakame.item.template.ItemGenerationContext
import cc.mewcraft.wakame.item.templates.components.cells.CoreBlueprint
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.examination.ExaminableProperty
import java.util.stream.Stream


/**
 * 代表一个无操作的核心模板.
 *
 * 设计上, 无操作核心不会写入到物品上.
 */
data object VirtualCoreBlueprint : CoreBlueprint {
    override val id: Key = GenericKeys.NOOP

    override fun generate(context: ItemGenerationContext): Core {
        // 无操作核心应该不需要写入上下文
        return CoreFactory.virtual()
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(
            ExaminableProperty.of("id", id)
        )
    }

    override fun toString(): String {
        return toSimpleString()
    }
}

/**
 * 代表一个空的核心模板.
 *
 * “词条栏里有空核心” 在玩家看来就是 “词条栏里没有核心”.
 * 如果词条栏没有核心, 那也就意味着可以被替换成其他的.
 */
data object EmptyCoreBlueprint : CoreBlueprint {
    override val id: Key = GenericKeys.EMPTY

    override fun generate(context: ItemGenerationContext): Core {
        // 空词条栏应该不需要写入上下文
        return CoreFactory.empty()
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("id", id)
    )

    override fun toString(): String {
        return toSimpleString()
    }
}
