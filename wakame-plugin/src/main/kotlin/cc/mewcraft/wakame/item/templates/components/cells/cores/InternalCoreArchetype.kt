package cc.mewcraft.wakame.item.templates.components.cells.cores

import cc.mewcraft.wakame.GenericKeys
import cc.mewcraft.wakame.item.components.cells.Core
import cc.mewcraft.wakame.item.template.ItemGenerationContext
import cc.mewcraft.wakame.item.templates.components.cells.CoreArchetype
import net.kyori.adventure.key.Key


/**
 * 代表一个无操作核心 [cc.mewcraft.wakame.item.components.cells.VirtualCore] 的模板.
 *
 * 设计上, 无操作核心不会写入到物品上.
 */
data object VirtualCoreArchetype : CoreArchetype {
    override val id: Key
        get() = GenericKeys.NOOP

    // 无操作核心应该不需要写入上下文
    override fun generate(context: ItemGenerationContext): Core =
        Core.virtual()
}

/**
 * 代表一个空核心 [cc.mewcraft.wakame.item.components.cells.EmptyCore] 的模板.
 *
 * “核孔里有空核心” 在玩家看来就是 “核孔里没有核心”.
 * 如果核孔没有核心, 那也就意味着可以被替换成其他的.
 */
data object EmptyCoreArchetype : CoreArchetype {
    override val id: Key
        get() = GenericKeys.EMPTY

    // 空核孔应该不需要写入上下文
    override fun generate(context: ItemGenerationContext): Core =
        Core.empty()
}
