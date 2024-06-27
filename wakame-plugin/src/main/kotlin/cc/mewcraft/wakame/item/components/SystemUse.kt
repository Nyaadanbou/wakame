package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import net.kyori.examination.Examinable

interface SystemUse : Examinable {

    // 开发日记 2024/6/27
    // SystemUse 组件只用于内部代码,
    // 因此只有一个 Codec.
    // 它既不需要一个特定的 Value, 因为它只有存在与否;
    // 它也不需要一个特定的 Template, 因为配置文件暂时没有用处.

    class Codec(
        override val id: String,
    ) : ItemComponentType<Unit, ItemComponentHolder.NBT> {
        override val holder: ItemComponentType.Holder = ItemComponentType.Holder.NBT
        override fun read(holder: ItemComponentHolder.NBT): Unit = Unit
        override fun write(holder: ItemComponentHolder.NBT, value: Unit) = Unit
        override fun remove(holder: ItemComponentHolder.NBT) = Unit
    }
}