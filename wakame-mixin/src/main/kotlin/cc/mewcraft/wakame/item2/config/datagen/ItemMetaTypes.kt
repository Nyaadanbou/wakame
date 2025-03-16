package cc.mewcraft.wakame.item2.config.datagen

import cc.mewcraft.wakame.item2.config.datagen.impl.MetaItemLevel
import cc.mewcraft.wakame.registry2.KoishRegistries2
import cc.mewcraft.wakame.util.register
import cc.mewcraft.wakame.util.typeTokenOf

data object ItemMetaTypes {

    // ------------
    // 注册表
    // ------------

    @JvmField
    val LEVEL: ItemMetaType<MetaItemLevel> = register("level") {
        serializers {
            register(MetaItemLevel.Serializer)
        }
    }

    // ------------
    // 方便函数
    // ------------

    /**
     * @param id 将作为注册表中的 ID
     * @param block 用于配置 [ItemMetaType]
     */
    private inline fun <reified T> register(id: String, block: ItemMetaType.Builder<T>.() -> Unit = {}): ItemMetaType<T> {
        val type = ItemMetaType.builder<T>(typeTokenOf()).apply(block).build()
        return type.also { KoishRegistries2.ITEM_META_TYPE.add(id, it) }
    }

}