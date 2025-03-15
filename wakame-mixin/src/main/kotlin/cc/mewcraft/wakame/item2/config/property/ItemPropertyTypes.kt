package cc.mewcraft.wakame.item2.config.property

import cc.mewcraft.wakame.item2.config.property.impl.Arrow
import cc.mewcraft.wakame.item2.config.property.impl.ItemBase2
import cc.mewcraft.wakame.item2.config.property.impl.ItemSlot2
import cc.mewcraft.wakame.util.typeTokenOf

// 作用有几点:
// 1. 充当一个使泛型变安全的单例, 用来获取对应的
object ItemPropertyTypes {

    @JvmField
    val BASE: ItemPropertyType<ItemBase2> = TODO("#350")

    @JvmField
    val SLOT: ItemPropertyType<ItemSlot2> = TODO("#350")

    @JvmField
    val HIDDEN: ItemPropertyType<Boolean> = register("hidden")

    @JvmField
    val ARROW: ItemPropertyType<Arrow> = TODO("#350")

    /**
     * @param id 将作为 Registry 中的 id
     * @param block 用于配置 [ItemPropertyType]
     */
    private inline fun <reified T> register(id: String, block: ItemPropertyType.Builder<T>.() -> Unit = {}): ItemPropertyType<T> {
        // FIXME #350: 在 KoishRegistry 中注册以支持 type dispatching
        return ItemPropertyType.builder(typeTokenOf<T>()).apply(block).build()
    }
}