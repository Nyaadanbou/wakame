package cc.mewcraft.wakame.item2.config.property

import cc.mewcraft.wakame.item2.config.property.impl.Arrow
import cc.mewcraft.wakame.item2.config.property.impl.ItemBase2
import cc.mewcraft.wakame.item2.config.property.impl.ItemSlot2
import cc.mewcraft.wakame.item2.config.property.impl.Lore
import cc.mewcraft.wakame.registry2.KoishRegistries2
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.typeTokenOf

data object ItemPropertyTypes {

    // ------------
    // 注册表
    // ------------

    @JvmField
    val ID: ItemPropertyType<Identifier> = register("id")

    @JvmField
    val BASE: ItemPropertyType<ItemBase2> = register("base")

    @JvmField
    val SLOT: ItemPropertyType<ItemSlot2> = register("slot")

    @JvmField
    val HIDDEN: ItemPropertyType<Unit> = register("hidden")

    @JvmField
    val ARROW: ItemPropertyType<Arrow> = register("arrow")

    @JvmField
    val CASTABLE: ItemPropertyType<Unit> = register("castable")

    @JvmField
    val GLOWABLE: ItemPropertyType<Unit> = register("glowable")

    @JvmField
    val LORE: ItemPropertyType<Lore> = register("lore")

    // ------------
    // 方便函数
    // ------------

    /**
     * @param id 将作为注册表中的 ID
     * @param block 用于配置 [ItemPropertyType]
     */
    private inline fun <reified T> register(id: String, block: ItemPropertyType.Builder<T>.() -> Unit = {}): ItemPropertyType<T> {
        val type = ItemPropertyType.builder(typeTokenOf<T>()).apply(block).build()
        return type.also { KoishRegistries2.ITEM_PROPERTY_TYPE.add(id, it) }
    }

}