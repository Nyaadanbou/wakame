package cc.mewcraft.wakame.item2.config.datagen

import cc.mewcraft.wakame.item2.config.datagen.impl.MetaItemLevel

object ItemMetaTypes {

    @JvmField
    val LEVEL: ItemMetaType<MetaItemLevel> = register("level")

    private inline fun <reified T> register(id: String, block: ItemMetaTypes.() -> Unit = {}): ItemMetaType<T> {

    }

}