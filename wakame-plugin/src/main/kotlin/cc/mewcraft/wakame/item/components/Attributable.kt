package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.display.TooltipsProvider
import cc.mewcraft.wakame.item.ItemComponentConstants
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import net.kyori.examination.Examinable

interface Attributable : Examinable, TooltipsProvider {

    // 开发日记: 2024/6/25
    // Attributable 属于 NonValued 组件类型, 没有“值”一说, 只有是否存在一说.
    // 因此这里没有像 Arrow 一样有一个 Codec 类.
    object Value : Attributable, TooltipsProvider, ItemComponentConfig(ItemComponentConstants.ATTRIBUTABLE) {
        override fun provideDisplayLore(): LoreLine {
            return LoreLine.noop()
        }
    }

    // FIXME NonValued 因为没有值, 所以没有 class Value.
    //  但是我们又需要能够提供该组件的提示框...
    //  因此提示框的创建逻辑可能得放在 Codec 里, 通过 ItemComponentMap 里的函数直接提供提示框.

    // 开发日记: 2024/6/25
    // Attributable 属于 NonValued + NBT 组件类型,
    // 这里的实现实际上根本不会运行.
    // FIXME 也许统一为 NonValued + NBT 类型的组件类型创建一个通用的实现比较好?
    class Codec(
        override val id: String,
    ) : ItemComponentType.NonValued<ItemComponentHolder.NBT> {
        override val holder: ItemComponentType.Holder = ItemComponentType.Holder.NBT

        // true/false 无所谓, 因为实际逻辑不在这控制
        override fun read(holder: ItemComponentHolder.NBT): Boolean = false

        // 这个执行什么无所谓, 因为实际逻辑不在这控制
        override fun write(holder: ItemComponentHolder.NBT, value: Boolean) = Unit

        // 这个执行什么无所谓, 因为实际逻辑不在这控制
        override fun remove(holder: ItemComponentHolder.NBT) = Unit
    }

    // 开发日记: 2024/6/25
    // Attributable 既然是一个 NonValued 组件类型,
    // 那么似乎也不需要为其创建一个 Template 的类型.
    // 在构建 NekoItem 的阶段只需要看配置文件里有没有这个 node 存在就行了.
    // class Template() {}
}
